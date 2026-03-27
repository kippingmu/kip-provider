package xyz.kip.provider.shutdown;

/**
 * Immutable snapshot of in-flight work counts.
 */
public record InFlightSnapshot(
        int httpInFlight,
        int mqInFlight,
        int schedulerInFlight,
        int executorInFlight,
        int totalInFlight
) {
}
