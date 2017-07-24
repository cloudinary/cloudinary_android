package com.cloudinary.android.policy;

/**
 * Global configuration and parameters for all upload requests. Use {@link Builder} to configure and get an instance.
 */
public class GlobalUploadPolicy extends UploadPolicy {
    private static final int DEFAULT_MAX_CONCURRENT_REQUESTS = 5;
    private final int maxConcurrentRequests;

    private GlobalUploadPolicy(NetworkType networkType, boolean requiresCharging, boolean requiresIdle, int maxErrorRetries, long backoffMillis, BackoffPolicy backoffPolicy, int maxConcurrentRequests) {
        super(networkType, requiresCharging, requiresIdle, maxErrorRetries, backoffMillis, backoffPolicy);
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    /**
     * @return An instance of {@link GlobalUploadPolicy} get the default configuration.
     */
    public static GlobalUploadPolicy defaultPolicy() {
        return new Builder().build();
    }

    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    /**
     * Builder to construct an instance of {@link GlobalUploadPolicy}.
     */
    public final static class Builder extends UploadPolicy.BaseBuilder<Builder> {
        private int maxConcurrentRequests = DEFAULT_MAX_CONCURRENT_REQUESTS;

        /**
         * Set maximum simultaneous upload requests.
         */
        public Builder maxConcurrentRequests (int maxConcurrentRequests){
            this.maxConcurrentRequests = maxConcurrentRequests;
            return this;
        }

        /**
         * @return An instance of {@link GlobalUploadPolicy} with the requested configuration.
         */
        public GlobalUploadPolicy build() {
            return new GlobalUploadPolicy(networkPolicy, requiresCharging, requiresIdle, maxRetries, backoffMillis, backoffPolicy, maxConcurrentRequests);
        }
    }
}
