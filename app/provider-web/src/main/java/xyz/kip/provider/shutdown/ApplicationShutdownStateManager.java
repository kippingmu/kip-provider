package xyz.kip.provider.shutdown;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Stores and transitions application shutdown state.
 */
public class ApplicationShutdownStateManager {

    private final AtomicReference<ServiceRunState> currentState = new AtomicReference<>(ServiceRunState.RUNNING);

    public ServiceRunState currentState() {
        return currentState.get();
    }

    public boolean acceptNewWork() {
        return currentState() == ServiceRunState.RUNNING;
    }

    public boolean moveToDraining() {
        return currentState.compareAndSet(ServiceRunState.RUNNING, ServiceRunState.DRAINING);
    }

    public boolean moveToStopping() {
        ServiceRunState previousState = currentState.getAndSet(ServiceRunState.STOPPING);
        return previousState != ServiceRunState.STOPPING;
    }
}
