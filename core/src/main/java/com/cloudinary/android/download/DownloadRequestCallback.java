package com.cloudinary.android.download;

/**
 * A callback for the result of the download request.
 */
public interface DownloadRequestCallback {

    /**
     * Called when a request completes successfully.
     */
    void onSuccess();

    /**
     * Called when a request failed.
     * @param t The error containing the information about why the request failed.
     */
    void onFailure(Throwable t);
}
