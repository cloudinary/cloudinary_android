package com.cloudinary.android.download.picasso;

import android.widget.ImageView;

import com.cloudinary.android.download.DownloadRequestStrategy;
import com.squareup.picasso.Picasso;

class PicassoDownloadRequestStrategy implements DownloadRequestStrategy {

    private final Picasso picasso;
    private final ImageView imageView;

    PicassoDownloadRequestStrategy(Picasso picasso, ImageView imageView) {
        this.picasso = picasso;
        this.imageView = imageView;
    }

    @Override
    public void cancel() {
        picasso.cancelRequest(imageView);
    }
}
