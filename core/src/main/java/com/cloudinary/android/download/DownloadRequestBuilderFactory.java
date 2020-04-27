package com.cloudinary.android.download;

import android.content.Context;

/**
 * Constructs a {@link DownloadRequestBuilder} instance to be used for creating download requests.
 */
public interface DownloadRequestBuilderFactory {

    /**
     * Create a {@link DownloadRequestBuilder} that will create the download requests.
     * @param context Android context.
     * @return The created {@link DownloadRequestBuilder}
     */
    DownloadRequestBuilder createDownloadRequestBuilder(Context context);
}
