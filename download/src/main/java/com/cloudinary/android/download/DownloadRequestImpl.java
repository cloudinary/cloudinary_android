package com.cloudinary.android.download;

import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * A wrapper for the {@link DownloadRequestStrategy} which basically doesn't start when a request does not have a url prior starting.
 * Once the url is set, the download will start if {@link #start()} was called beforehand.
 *
 * <p>Note: This class is intended for a one time use. Therefore, subsequent calls to {@link #setUrl(String)} and {@link #start()}
 * will not alter the request's state nor it will not be restarted.</p>
 */
public class DownloadRequestImpl implements DownloadRequest {

    private DownloadRequestBuilderStrategy downloadRequestBuilderStrategy;
    private DownloadRequestStrategy downloadRequestStrategy;
    private WeakReference<ImageView> imageViewRef;
    private String url;
    private boolean shouldStart;
    private boolean isCancelled;

    DownloadRequestImpl(DownloadRequestBuilderStrategy downloadRequestBuilderStrategy, ImageView imageView) {
        this.downloadRequestBuilderStrategy = downloadRequestBuilderStrategy;
        imageViewRef = new WeakReference<>(imageView);
    }

    synchronized void setUrl(String url) {
        if (this.url == null) {
            this.url = url;

            if (shouldStart) {
                start();
            }
        }
    }

    synchronized DownloadRequestImpl start() {
        if (!isCancelled && downloadRequestStrategy == null) {
            shouldStart = true;

            if (url != null) {
                downloadRequestStrategy = downloadRequestBuilderStrategy.into(imageViewRef.get());
            }
        }

        return this;
    }

    @Override
    public synchronized void cancel() {
        if (downloadRequestStrategy != null) {
            downloadRequestStrategy.cancel();
        }
        isCancelled = true;
    }
}
