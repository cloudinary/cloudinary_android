package com.cloudinary.android.callback;

import java.util.Map;

/**
 * Callback for progress and results of an upload operation.
 */
public interface UploadCallback {

    /**
     * Called when a request starts uploading.
     * @param requestId Id of the request sending this callback.
     */
    void onStart(String requestId);

    /**
     * Called on upload progress updates.
     * @param requestId Id of the request sending this callback.
     * @param bytes Total bytes uploaded so far.
     * @param totalBytes Total bytes to upload (if known).
     */
    void onProgress(String requestId, long bytes, long totalBytes);

    /**
     * Called when a requests completes successfully.
     * @param requestId Id of the request sending this callback.
     * @param resultData Result data about the newly uploaded resource.
     */
    void onSuccess(String requestId,Map resultData);

    /**
     * Called when a request encounters an error.
     * @param requestId Id of the request sending this callback.
     * @param error Error object containing description and code.
     */
    void onError(String requestId, ErrorInfo error);

    /**
     * Called when a request fails with a recoverable error and is rescheduled to a later time.
     * This is useful to update UI (e.g hide progress notifications), otherwise this callback can be ignored.
     * @param requestId Id of the request sending this callback.
     * @param error Error object containing technical description and code.
     */
    void onReschedule(String requestId, ErrorInfo error);
}

