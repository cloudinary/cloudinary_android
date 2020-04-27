package com.cloudinary.android.download;

/**
 * Strategy interface to be implemented by libraries to represent the in-progress
 * download request.
 */
public interface DownloadRequestStrategy {

    /**
     * Cancel the download request.
     */
    void cancel();
}
