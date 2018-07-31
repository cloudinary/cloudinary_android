package com.cloudinary.android;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * Entry point to dispatch new upload requests.
 */
interface RequestDispatcher {
    /**
     * Dispatch a new upload request. Will get queued and handled based on network policy, phone policy and time constraints.
     * For immediate requests or preprocessing, a context needs to be provided.
     */
    String dispatch(@Nullable Context context, UploadRequest request);

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
