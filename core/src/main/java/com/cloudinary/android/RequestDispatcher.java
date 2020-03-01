package com.cloudinary.android;

import android.content.Context;

/**
 * Entry point to dispatch new upload requests.
 */
public interface RequestDispatcher {
    /**
     * Dispatch a new upload request. Will get queued and handled based on network policy, phone policy and time constraints.
     * For immediate requests or preprocessing, a context needs to be provided.
     */
    String dispatch(UploadRequest request);

    /**
     * Run an upload request immediately ignoring all constraints. Note: Requests that run through
     * here are never rescheduled.
     *
     * @param request The request to run
     * @param context Android context, required for immediate requests.
     * @return The request id.
     */
    String startNow(Context context, UploadRequest request);

    /**
     * Cancel an upload request.
     *
     * @param requestId Id of the request to cancel.
     * @return True if the request was found and cancelled successfully.
     */
    boolean cancelRequest(String requestId);

    /**
     * Called every time a request finishes, meaning there's room for new requests.
     */
    void queueRoomFreed();

    /**
     * Cancels all upload requests.
     *
     * @return The count of canceled requests and running jobs.
     */
    int cancelAllRequests();
}
