package com.cloudinary.android.download;

/**
 * Represents an active download request (in progress).
 */
public interface DownloadRequest {

    /**
     * Cancel the download request.
     */
    void cancel();
}
