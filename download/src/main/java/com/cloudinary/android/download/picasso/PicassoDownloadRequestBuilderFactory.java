package com.cloudinary.android.download.picasso;

import android.content.Context;

import com.cloudinary.android.download.DownloadRequestBuilder;
import com.cloudinary.android.download.DownloadRequestBuilderFactory;
import com.cloudinary.android.download.DownloadRequestBuilderImpl;

public class PicassoDownloadRequestBuilderFactory implements DownloadRequestBuilderFactory {

    @Override
    public DownloadRequestBuilder createDownloadRequestBuilder(Context context) {
        return new DownloadRequestBuilderImpl(context, new PicassoDownloadRequestBuilderStrategy(context));
    }
}
