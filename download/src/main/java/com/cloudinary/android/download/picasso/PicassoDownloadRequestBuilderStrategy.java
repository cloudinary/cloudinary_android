package com.cloudinary.android.download.picasso;

import android.content.Context;
import android.widget.ImageView;

import com.cloudinary.android.download.DownloadRequestBuilderStrategy;
import com.cloudinary.android.download.DownloadRequestStrategy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class PicassoDownloadRequestBuilderStrategy implements DownloadRequestBuilderStrategy {

    private final Picasso picasso;
    private RequestCreator requestCreator;

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
    public DownloadRequestStrategy into(ImageView imageView) {
        requestCreator.into(imageView);

        return new PicassoDownloadRequestStrategy(picasso, imageView);
    }
}
