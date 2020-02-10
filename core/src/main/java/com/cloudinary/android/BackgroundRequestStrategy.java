package com.cloudinary.android;

import android.content.Context;

/**
 * Strategy interface to be implemented by frameworks that handle background worker thread, rescheduling and queueing of jobs.
 */
interface BackgroundRequestStrategy {
    int IMMEDIATE_THRESHOLD = 60 * 1000;
    int SOON_THRESHOLD = 30 * 60 * 1000;

    /**
     * Run all initialization code for the framework.
     * @param context Android context for initialization purposes. DO NOT store.
     */
    void init(Context context);

    /**
     * Do the actual dispatching of the request, according to the inner working of the framework.
     * @param request The request to dispatch.
     */
    void doDispatch(UploadRequest request);

    /**
     * Take pending requests from the near future and start them immediately.
     * @param howMany How many requests to start.
     */
    void executeRequestsNow(int howMany);

    /**
     * Cancel an upload request.
     * @param requestId The request id to cancel.
     * @return True if the request was found and cancelled successfully.
     */
    boolean cancelRequest(String requestId);

    /**
     * Cancels all upload requests.
     * @return The count of canceled requests and running jobs.
     */
    int cancelAllRequests();

    /**
     * Get the count of the pending jobs that are about to start (=start within one minute).
     */
    int getPendingImmediateJobsCount();

    /**
     * Get the count of the currently running jobs.
     */
    int getRunningJobsCount();
}
