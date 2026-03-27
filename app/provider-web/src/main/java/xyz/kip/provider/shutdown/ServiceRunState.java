package xyz.kip.provider.shutdown;

/**
 * Represents the service runtime state during graceful shutdown.
 */
public enum ServiceRunState {
    RUNNING,
    DRAINING,
    STOPPING
}
