package com.cloudinary.android;

import android.content.Context;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.UploadResult;
import com.cloudinary.android.callback.UploadStatus;

import java.util.Map;

/**
 * Handle the events and callbacks for uploads
 */
interface CallbackDispatcher {

    /**
     * Register a callback for requests state changes and progress.
     * @param callback The callback to activate upon state changes and results.
     */
    void registerCallback(UploadCallback callback);

    /**
     * Register a callback for a specific request
     * @param requestId The id of the request to listen to.
     * @param callback The callback to activate upon state changes and results.
     */
    void registerCallback(String requestId, UploadCallback callback);

    /**
     * Unregister a callback
     * @param callback The callback to unregister.
     */
    void unregisterCallback(UploadCallback callback);

    /**
     * Send a broadcast when a request starts.
     * @param appContext Android Application context.
     * @param requestId The request id the send broadcast for.
     */
    void wakeListenerServiceWithRequestStart(Context appContext, String requestId);

    /**
     * Send a broadcast when a request finishes with any result
     * @param appContext Android Application context.
     * @param requestId The request id the send broadcast for.
     * @param uploadStatus The status of the finished request.
     */
    void wakeListenerServiceWithRequestFinished(Context appContext, String requestId, UploadStatus uploadStatus);

    /**
     * Dispatch event when a request starts.
     * @param requestId Id of the request to dispatch event for.
     */
    void dispatchStart(String requestId);

    /**
     * Dispatch progress
     * @param requestId Id of the request to dispatch progress for.
     * @param bytes Total bytes uploaded.
     * @param totalBytes Size of the entire resource (-1 if unknown).
     */
    void dispatchProgress(String requestId, long bytes, long totalBytes);

    /**
     * Dispatch the result of a successful upload operation.
     * @param context Android context.
     * @param requestId Id of the request to dispatch result for.
     * @param resultData The map containing all the returned parameters from the upload process. Will be cached even if no callbacks are currently registered.
     */
    void dispatchSuccess(Context context, String requestId, Map resultData);

    /**
     * Dispatch the error result of an upload operation.
     * @param context Android context.
     * @param requestId Id of the request to dispatch error for.
     * @param error Error object containing a technical description and an error code.
     */
    void dispatchError(Context context, String requestId, ErrorInfo error);

    /**
     * Dispatch callback when a request gets rescheduled. This is usually used to update ui (i.e. remove progress notification, if any). Otherwise
     * no action is required in response to this event.
     * @param context Android context.
     * @param requestId Id of the request getting rescheduled.
     * @param error Error object containing a technical description and an error code.
     */
    void dispatchReschedule(Context context, String requestId, ErrorInfo error);

    /**
     * Fetch a pending upload result (either successful or not), in case the app wasn't awake when the upload finished it can be fetched here.
     * @param requestId Id of the request to fetch results for.
     * @return The result originally returned from Cloudinary.
     */
    UploadResult popPendingResult(String requestId);
}
