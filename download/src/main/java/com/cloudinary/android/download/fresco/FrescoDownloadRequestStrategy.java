package com.cloudinary.android.download.fresco;

import com.cloudinary.android.download.DownloadRequestStrategy;

class FrescoDownloadRequestStrategy implements DownloadRequestStrategy {

    @Override
    public void cancel() {
        throw new UnsupportedOperationException("Fresco doesn't support cancellation of download requests.");
    }
}
