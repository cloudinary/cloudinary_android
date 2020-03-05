package com.cloudinary.android.policy;

/**
 * Represents the time window in which the request must be executed. See {@link Builder} for details.
 */
public class TimeWindow {
    // default 3 hours.
    private static final long DEFAULT_TIME_WINDOW = 3L * 60L * 60L * 1000L;
    private static String TAG = "TimeWindow";

    private final long minLatencyOffsetMillis;
    private final long maxExecutionDelayMillis;

    private TimeWindow(long minLatencyOffsetMillis, long maxExecutionDelayMillis) {
        this.minLatencyOffsetMillis = minLatencyOffsetMillis;
        this.maxExecutionDelayMillis = maxExecutionDelayMillis;
    }

    /**
     * @return An instance of {@link TimeWindow} configured to execute a request immediately, ignoring policy constraints.
     */
    public static TimeWindow immediate() {
        return new TimeWindow(1, 1000);
    }

    /**
     * @return An instance of {@link TimeWindow} with default configuration.
     */
    public static TimeWindow getDefault() {
        return new TimeWindow(1, DEFAULT_TIME_WINDOW);
    }

    /**
     * The minimum time to wait before executing a request. This is defined as offset to current time.
     */
    public long getMinLatencyOffsetMillis() {
        return minLatencyOffsetMillis;
    }

    /**
     * The maximum time to wait before a request must be executed regardless of the upload policy. This is defined as offset to current time.
     */
    public long getMaxExecutionDelayMillis() {
        return maxExecutionDelayMillis;
    }

    /**
     * Build a new deferred time window instance based on this instance.
     * @param minutes Minutes to defer by
     * @return The new instance.
     */
    public TimeWindow newDeferredWindow(int minutes) {
        long deferBy = minutes * 60 * 1000;
        return new TimeWindow(minLatencyOffsetMillis + deferBy, maxExecutionDelayMillis + deferBy);
    }

    /**
     * Returns true if the time window is scheduled to start within the next minute.
     */
    public boolean isStartNow() {
        return minLatencyOffsetMillis <= 60 * 1000;
    }

    /**
     * Returns true if the time window is forcing a run within the next minute (i.e. ignoring policy constraints).
     */
    public boolean isImmediate() {
        return maxExecutionDelayMillis <= 60 * 1000;
    }

    /**
     * Constructs a {@link TimeWindow} instance
     */
    public static final class Builder{
        private long minLatencyOffsetMillis = 0;
        private long maxExecutionDelayMillis = DEFAULT_TIME_WINDOW;

        /**
         * Sets the minimum time to wait before executing a request. This is defined as offset to current time.
         * @param minLatencyMillis The minimum time to wait before executing a request. This is defined as offset to current time.
         * @return Itself for chaining operations.
         */
        public Builder minLatencyMillis(long minLatencyMillis){
            this.minLatencyOffsetMillis = minLatencyMillis;
            return this;
        }

        /**
         * Sets the maximum time to wait before a request must be executed regardless of the upload policy. This is defined as offset to current time.
         * @param maxExecutionDelayMillis The maximum time to wait before a request must be executed regardless of the upload policy. This is defined as offset to current time.
         * @return Itself for chaining operations.
         */
        public Builder maxExecutionDelayMillis(long maxExecutionDelayMillis){
            this.maxExecutionDelayMillis = maxExecutionDelayMillis;
            return this;
        }

        /**
         * @return An instance of {@link TimeWindow} based on the requested parameters.
         */
        public TimeWindow build(){
            return new TimeWindow(minLatencyOffsetMillis, maxExecutionDelayMillis);
        }
    }
}
