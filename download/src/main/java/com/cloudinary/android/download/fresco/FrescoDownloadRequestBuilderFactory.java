package com.cloudinary.android.download.fresco;

import android.content.Context;

import com.cloudinary.android.download.DownloadRequestBuilder;
import com.cloudinary.android.download.DownloadRequestBuilderFactory;
import com.cloudinary.android.download.DownloadRequestBuilderImpl;

public class FrescoDownloadRequestBuilderFactory implements DownloadRequestBuilderFactory {

    @Override
    public DownloadRequestBuilder createDownloadRequestBuilder(Context context) {
        return new DownloadRequestBuilderImpl(context, new FrescoDownloadRequestBuilderStrategy(context));
    }
}
