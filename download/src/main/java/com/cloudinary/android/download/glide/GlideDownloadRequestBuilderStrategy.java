package com.cloudinary.android.download.glide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.cloudinary.android.download.DownloadRequestBuilderStrategy;
import com.cloudinary.android.download.DownloadRequestCallback;
import com.cloudinary.android.download.DownloadRequestStrategy;

import androidx.annotation.Nullable;

@SuppressLint("CheckResult")
class GlideDownloadRequestBuilderStrategy implements DownloadRequestBuilderStrategy {

    private RequestBuilder<Drawable> requestBuilder;

    GlideDownloadRequestBuilderStrategy(Context context) {
        requestBuilder = Glide.with(context).asDrawable();
    }

    @Override
    public DownloadRequestBuilderStrategy load(String url) {
        requestBuilder.load(url);
        return this;
    }

    @Override
    public DownloadRequestBuilderStrategy load(int resourceId) {
        requestBuilder.load(resourceId);
        return this;
    }

    @Override
    public DownloadRequestBuilderStrategy placeholder(int resourceId) {
        requestBuilder.placeholder(resourceId);
        return this;
    }

    @Override
    public DownloadRequestBuilderStrategy callback(final DownloadRequestCallback callback) {
        requestBuilder.listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                callback.onFailure(e);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                callback.onSuccess();
                return false;
            }
        });
        return this;
    }

    @Override
    public DownloadRequestStrategy into(ImageView imageView) {
        ViewTarget<ImageView, Drawable> target = requestBuilder.into(imageView);

        return new GlideDownloadRequestStrategy(target);
    }
}
