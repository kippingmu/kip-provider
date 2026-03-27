package xyz.kip.provider.shutdown;

/**
 * Defines the categories of work tracked during graceful shutdown.
 */
public enum WorkCategory {
    HTTP,
    MQ,
    SCHEDULER,
    EXECUTOR
}
