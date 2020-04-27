package com.cloudinary.android.download.glide;

import android.content.Context;

import com.cloudinary.android.download.DownloadRequestBuilderImpl;
import com.cloudinary.android.download.DownloadRequestBuilderFactory;

public class GlideDownloadRequestBuilderFactory implements DownloadRequestBuilderFactory {

    @Override
    public DownloadRequestBuilderImpl createDownloadRequestBuilder(Context context) {
        return new DownloadRequestBuilderImpl(context, new GlideDownloadRequestBuilderStrategy(context));
    }
}
