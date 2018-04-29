package com.cloudinary.android.policy;

/**
 * Represents the set of conditions that must be met for a request to execute. Note: A request will be executed regardless of policy once the {@link TimeWindow} expires.
 */
public class UploadPolicy {
    private static final int DEFAULT_MAX_ERROR_RETRIES = 5;
    private static final long DEFAULT_BACKOFF_MILLIS = 120_000;
    private static final BackoffPolicy DEFAULT_BACKOFF_POLICY = BackoffPolicy.EXPONENTIAL;
    private static String TAG = "UploadPolicy";
    private final NetworkType networkType;
    private final boolean requiresCharging;
    private final boolean requiresIdle;
    private final int maxErrorRetries;
    private final long backoffMillis;
    private final BackoffPolicy backoffPolicy;

    /**
     * Use {@link Builder} to configure and get an instance of {@link UploadPolicy}.
     */
    protected UploadPolicy(NetworkType networkType, boolean requiresCharging, boolean requiresIdle, int maxErrorRetries, long backoffMillis, BackoffPolicy backoffPolicy) {
        this.networkType = networkType;
        this.requiresCharging = requiresCharging;
        this.requiresIdle = requiresIdle;
        this.maxErrorRetries = maxErrorRetries;
        this.backoffMillis = backoffMillis;
        this.backoffPolicy = backoffPolicy;
    }

    /**
     * @return An instance of {@link UploadPolicy} get the default configuration.
     */
    public static UploadPolicy defaultPolicy() {
        return new Builder().build();
    }

    /**
     * {@link NetworkType} required to execute the request.
     */
    public NetworkType getNetworkType() {
        return networkType;
    }

    /**
     * Whether charging is required to execute the request.
     */
    public boolean isRequiresCharging() {
        return requiresCharging;
    }

    /**
     * Whether the phone needs to be idle to execute the request.
     */
    public boolean isRequiresIdle() {
        return requiresIdle;
    }

    /**
     * Maximum retries before failing the request permanently.
     */
    public int getMaxErrorRetries() {
        return maxErrorRetries;
    }

    public BackoffPolicy getBackoffPolicy() {
        return backoffPolicy;
    }

    public long getBackoffMillis() {
        return backoffMillis;
    }

    public boolean hasRequirements() {
        return requiresCharging || requiresIdle || networkType == NetworkType.UNMETERED;
    }
    /**
     * Get a new builder with the configuration copied from this policy.
     */
    public Builder newBuilder() {
        return new Builder()
                .requiresCharging(requiresCharging)
                .requiresIdle(requiresIdle)
                .backoffCriteria(backoffMillis, backoffPolicy)
                .maxRetries(maxErrorRetries)
                .networkPolicy(networkType);
    }

    /**
     * Enum to define requirements for network.
     */
    public enum NetworkType {
        /**
         * No network is needed.
         */
        NONE,

        /**
         * Network is needed.
         */
        ANY,

        /**
         * Unmetered network needed (e.g. wifi).
         */
        UNMETERED
    }

    /**
     * Enum to define the backoff policy for request rescheduling.
     */
    public enum BackoffPolicy {
        /**
         * backoff = numFailures * initial_backoff.
         */
        LINEAR,
        /**
         * backoff = initial_backoff * 2 ^ (numFailures - 1).
         */
        EXPONENTIAL
    }

    /**
     * Base class for {@link UploadPolicy} builders.
     */
    @SuppressWarnings("unchecked")
    abstract static class BaseBuilder<T extends BaseBuilder> {
        NetworkType networkPolicy = NetworkType.ANY;
        boolean requiresCharging = false;
        boolean requiresIdle = false;
        int maxRetries = DEFAULT_MAX_ERROR_RETRIES;
        long backoffMillis = DEFAULT_BACKOFF_MILLIS;
        BackoffPolicy backoffPolicy = DEFAULT_BACKOFF_POLICY;

        /**
         * {@link NetworkType} required to execute the request.
         * @return Itself for chaining.
         */
        public T networkPolicy(NetworkType networkPolicy) {
            if (networkPolicy == NetworkType.NONE){
                throw new IllegalArgumentException("An upload request requires network");
            }
            this.networkPolicy = networkPolicy;
            return (T) this;
        }

        /**
         * Whether charging is required to execute the request.
         * @return Itself for chaining.
         */
        public T requiresCharging(boolean requiresCharging) {
            this.requiresCharging = requiresCharging;
            return (T) this;
        }

        /**
         * Whether the phone needs to be idle to execute the request.
         * @return Itself for chaining.
         */
        public T requiresIdle(boolean requiresIdle) {
            this.requiresIdle = requiresIdle;
            return (T) this;
        }

        /**
         * Maximum times to retry a request if it fails.
         * @return Itself for chaining.
         */
        public T maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return (T) this;
        }

        /**
         * Backoff behaviour for rescheduled requests.
         * @param backoffMs The initial interval to wait when the job has been rescheduled.
         * @param backoffPolicy Is either linear or exponential.
         * @return Itself for chaining.
         */
        public T backoffCriteria(long backoffMs, BackoffPolicy backoffPolicy) {
            this.backoffMillis = backoffMs;
            this.backoffPolicy = backoffPolicy;
            return (T) this;
        }

        /**
         * @return An instance of {@link UploadPolicy} get the requested configuration.
         */
        public UploadPolicy build() {
            return new UploadPolicy(networkPolicy, requiresCharging, requiresIdle, maxRetries, backoffMillis, backoffPolicy);
        }
    }

    /**
     * A utility class to construct an instance of {@link UploadPolicy}.
     */
    public static class Builder extends BaseBuilder<Builder>{
    }
}
