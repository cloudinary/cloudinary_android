package com.cloudinary.android.download.picasso;

import android.content.Context;
import android.widget.ImageView;

import com.cloudinary.android.download.DownloadRequestBuilderStrategy;
import com.cloudinary.android.download.DownloadRequestCallback;
import com.cloudinary.android.download.DownloadRequestStrategy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class PicassoDownloadRequestBuilderStrategy implements DownloadRequestBuilderStrategy {

    private final Picasso picasso;
    private RequestCreator requestCreator;
    private DownloadRequestCallback callback;

    PicassoDownloadRequestBuilderStrategy(Context context) {
        picasso = new Picasso.Builder(context).build();
    }

    @Override
    public DownloadRequestBuilderStrategy load(String url) {
        requestCreator = picasso.load(url);
        return this;
    }

    @Override
    public DownloadRequestBuilderStrategy load(int resourceId) {
        requestCreator = picasso.load(resourceId);
        return this;
    }

    @Override
    public DownloadRequestBuilderStrategy placeholder(int resourceId) {
        requestCreator.placeholder(resourceId);
        return this;
    }

    @Override
    public DownloadRequestBuilderStrategy callback(DownloadRequestCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public DownloadRequestStrategy into(ImageView imageView) {
        if (callback != null) {
            requestCreator.into(imageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    callback.onSuccess();
                }

                @Override
                public void onError(Exception e) {
                    callback.onFailure(e);
                }
            });
        } else {
            requestCreator.into(imageView);
        }

        return new PicassoDownloadRequestStrategy(picasso, imageView);
    }
}
