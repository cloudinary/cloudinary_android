package com.cloudinary.android.download.glide;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.cloudinary.android.download.DownloadRequestStrategy;

class GlideDownloadRequestStrategy implements DownloadRequestStrategy {

    private final ViewTarget<ImageView, Drawable> target;

    GlideDownloadRequestStrategy(ViewTarget<ImageView, Drawable> target) {
        this.target = target;
    }

    @Override
    public void cancel() {
        ImageView view = target.getView();
        Glide.with(view).clear(view);
    }
}
