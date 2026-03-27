package xyz.kip.provider.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Coordinates graceful shutdown steps in a single ordered flow.
 */
@Component
public class ShutdownOrchestrator implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ShutdownOrchestrator.class);
    private static final Duration WAIT_INTERVAL = Duration.ofMillis(200);
    private static final Comparator<GracefulShutdownParticipant> PHASE_ASC_COMPARATOR =
            Comparator.comparingInt(GracefulShutdownParticipant::getPhase);
    private static final Comparator<GracefulShutdownParticipant> PHASE_DESC_COMPARATOR =
            PHASE_ASC_COMPARATOR.reversed();

    private final ApplicationContext applicationContext;
    private final ApplicationShutdownStateManager shutdownStateManager;
    private final InFlightWorkTracker inflightWorkTracker;
    private final NacosInstanceDrainer nacosInstanceDrainer;
    private final List<GracefulShutdownParticipant> participants;
    private final ShutdownProperties shutdownProperties;
    private final AtomicBoolean stopTriggered = new AtomicBoolean();

    private volatile boolean running;

    public ShutdownOrchestrator(ApplicationContext applicationContext,
                                ApplicationShutdownStateManager shutdownStateManager,
                                InFlightWorkTracker inflightWorkTracker,
                                NacosInstanceDrainer nacosInstanceDrainer,
                                List<GracefulShutdownParticipant> participants,
                                ShutdownProperties shutdownProperties) {
        this.applicationContext = applicationContext;
        this.shutdownStateManager = shutdownStateManager;
        this.inflightWorkTracker = inflightWorkTracker;
        this.nacosInstanceDrainer = nacosInstanceDrainer;
        this.participants = participants.stream().sorted(PHASE_ASC_COMPARATOR).toList();
        this.shutdownProperties = shutdownProperties;
    }

    @Override
    public void start() {
        this.running = true;
    }

    @Override
    public void stop() {
        stop(() -> {
        });
    }

    @Override
    public void stop(Runnable callback) {
        if (!running) {
            callback.run();
            return;
        }
        if (!stopTriggered.compareAndSet(false, true)) {
            callback.run();
            return;
        }

        log.info("graceful shutdown started");
        if (shutdownStateManager.moveToDraining()) {
            log.info("shutdown state changed: RUNNING -> DRAINING");
        }

        AvailabilityChangeEvent.publish(applicationContext, ReadinessState.REFUSING_TRAFFIC);
        log.info("readiness changed to refusing traffic");

        nacosInstanceDrainer.deregisterCurrentInstance();
        pause(shutdownProperties.getPropagationWait());

        prepareParticipants();
        if (shutdownStateManager.moveToStopping()) {
            log.info("shutdown state changed: DRAINING -> STOPPING");
        }

        boolean drained = waitForInFlightCompletion(shutdownProperties.getInFlightWait());
        if (!drained) {
            log.warn("graceful shutdown wait timed out, forcing remaining participants to stop");
            forceStopParticipants();
            pause(shutdownProperties.getForceStopWait());
        }

        InFlightSnapshot snapshot = inflightWorkTracker.snapshot();
        log.info("graceful shutdown finished, httpInFlight={}, mqInFlight={}, schedulerInFlight={}, executorInFlight={}, totalInFlight={}",
                snapshot.httpInFlight(),
                snapshot.mqInFlight(),
                snapshot.schedulerInFlight(),
                snapshot.executorInFlight(),
                snapshot.totalInFlight());

        running = false;
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    private void prepareParticipants() {
        for (GracefulShutdownParticipant participant : participants) {
            log.info("preparing shutdown participant, name={}, phase={}", participant.getName(), participant.getPhase());
            participant.prepareDrain();
        }
    }

    private void forceStopParticipants() {
        participants.stream()
                .sorted(PHASE_DESC_COMPARATOR)
                .forEach(participant -> {
                    if (!participant.isIdle()) {
                        log.warn("forcing shutdown participant, name={}, phase={}", participant.getName(), participant.getPhase());
                        participant.forceStop();
                    }
                });
    }

    private boolean waitForInFlightCompletion(Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        long nextLogTime = System.nanoTime();
        while (System.nanoTime() < deadline) {
            if (allWorkCompleted()) {
                return true;
            }
            if (System.nanoTime() >= nextLogTime) {
                InFlightSnapshot snapshot = inflightWorkTracker.snapshot();
                log.info("waiting for in-flight work, httpInFlight={}, mqInFlight={}, schedulerInFlight={}, executorInFlight={}, totalInFlight={}",
                        snapshot.httpInFlight(),
                        snapshot.mqInFlight(),
                        snapshot.schedulerInFlight(),
                        snapshot.executorInFlight(),
                        snapshot.totalInFlight());
                nextLogTime = System.nanoTime() + Duration.ofSeconds(1).toNanos();
            }
            pause(WAIT_INTERVAL);
        }
        return allWorkCompleted();
    }

    private boolean allWorkCompleted() {
        return inflightWorkTracker.totalInFlight() == 0
                && participants.stream().allMatch(GracefulShutdownParticipant::isIdle);
    }

    private void pause(Duration duration) {
        long remainingNanos = duration.toNanos();
        while (remainingNanos > 0) {
            long parkNanos = Math.min(remainingNanos, WAIT_INTERVAL.toNanos());
            LockSupport.parkNanos(parkNanos);
            remainingNanos -= parkNanos;
        }
    }
}
