package com.cloudinary.android.download;

import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * A wrapper for the {@link DownloadRequestStrategy} which basically doesn't start when a request does not have a url prior starting.
 * Once the url is set, the download will start if {@link #start()} was called beforehand.
 *
 * <p>Note: This class is intended for a one time use. Therefore, subsequent calls to {@link #setSource(Object)} and {@link #start()}
 * will not alter the request's state nor it will not be restarted.</p>
 */
public class DownloadRequestImpl implements DownloadRequest {

    private DownloadRequestBuilderStrategy downloadRequestBuilderStrategy;
    private DownloadRequestStrategy downloadRequestStrategy;
    private WeakReference<ImageView> imageViewRef;
    private Object source;
    private boolean shouldStart;
    private boolean isCancelled;

    DownloadRequestImpl(DownloadRequestBuilderStrategy downloadRequestBuilderStrategy, ImageView imageView) {
        this.downloadRequestBuilderStrategy = downloadRequestBuilderStrategy;
        imageViewRef = new WeakReference<>(imageView);
    }

    synchronized void setSource(Object source) {
        if (this.source == null) {
            if (source instanceof String) {
                downloadRequestBuilderStrategy.load((String) source);
            } else if (source instanceof Integer) {
                downloadRequestBuilderStrategy.load((Integer) source);
            }
            this.source = source;

            if (shouldStart && !isCancelled) {
                doStart();
            }
        }
    }

    synchronized DownloadRequestImpl start() {
        if (!isCancelled && downloadRequestStrategy == null) {
            shouldStart = true;

            if (source != null) {
                doStart();
            }
        }

        return this;
    }

    private void doStart() {
        downloadRequestStrategy = downloadRequestBuilderStrategy.into(imageViewRef.get());
    }

    @Override
    public synchronized void cancel() {
        if (downloadRequestStrategy != null) {
            downloadRequestStrategy.cancel();
        }
        isCancelled = true;
    }
}
