package xyz.kip.provider.shutdown;

/**
 * Describes a component that participates in ordered graceful shutdown.
 */
public interface GracefulShutdownParticipant {

    String getName();

    int getPhase();

    void prepareDrain();

    boolean isIdle();

    void forceStop();
}
