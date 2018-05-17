package org.python.internal.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Supplier;

public class Timing {
    private boolean isEnabled;
    private TimeSupplier timeSupplier;

    public Timing() {
        isEnabled = true;
    }

    boolean isEnabled() {
        return isEnabled;
    }

    void ensureInitialized() {
        if (isEnabled() && timeSupplier == null) {
            timeSupplier = new TimeSupplier();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> System.err.println(timeSupplier.get())));
        }
    }

    public void accumulateTime(final String module, final long durationNano) {
        if (isEnabled()) {
            ensureInitialized();
            timeSupplier.accumulateTime(module, durationNano);
        }
    }

    private static String toMillis(long nano) {
        return Long.toString(TimeUnit.NANOSECONDS.toMillis(nano));
    }

    final class TimeSupplier implements Supplier<String> {
        private final Map<String, LongAdder> timings = new ConcurrentHashMap<>();
        private final LinkedBlockingDeque<String> orderedTimingNames = new LinkedBlockingDeque<>();

        private final Function<String, LongAdder> newTimingCreator = s -> {
            orderedTimingNames.add(s);
            return new LongAdder();
        };


        @Override
        public String get() {
            StringBuilder sb = new StringBuilder();
            sb.append("Accumulated timings:\n\n");
            long knownTime = 0;
            for (String timingName : orderedTimingNames) {
                final long duration = timings.get(timingName).longValue();
                sb.append(timingName).append(": ").append(toMillis(duration));
                sb.append(" ms\n");
                knownTime += duration;
            }
            sb.append("Total time: ").append(toMillis(knownTime)).append(" ms\n");
            return sb.toString();
        }

        private void accumulateTime(final String module, final long durationNano) {
            timings.computeIfAbsent(module, newTimingCreator).add(durationNano);
        }
    }
}
