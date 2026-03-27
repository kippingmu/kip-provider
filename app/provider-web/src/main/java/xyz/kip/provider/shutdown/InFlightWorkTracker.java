package xyz.kip.provider.shutdown;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks in-flight work counts by category.
 */
public class InFlightWorkTracker {

    private final Map<WorkCategory, AtomicInteger> counters = new EnumMap<>(WorkCategory.class);

    public InFlightWorkTracker() {
        for (WorkCategory category : WorkCategory.values()) {
            counters.put(category, new AtomicInteger());
        }
    }

    public void increment(WorkCategory category) {
        counters.get(category).incrementAndGet();
    }

    public void decrement(WorkCategory category) {
        AtomicInteger counter = counters.get(category);
        counter.updateAndGet(current -> current > 0 ? current - 1 : 0);
    }

    public int totalInFlight() {
        return counters.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }

    public InFlightSnapshot snapshot() {
        int httpInFlight = counters.get(WorkCategory.HTTP).get();
        int mqInFlight = counters.get(WorkCategory.MQ).get();
        int schedulerInFlight = counters.get(WorkCategory.SCHEDULER).get();
        int executorInFlight = counters.get(WorkCategory.EXECUTOR).get();
        return new InFlightSnapshot(
                httpInFlight,
                mqInFlight,
                schedulerInFlight,
                executorInFlight,
                httpInFlight + mqInFlight + schedulerInFlight + executorInFlight
        );
    }
}
