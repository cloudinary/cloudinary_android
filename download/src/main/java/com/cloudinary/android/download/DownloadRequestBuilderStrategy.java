package com.cloudinary.android.download;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

/**
 * Strategy interface to be implemented by libraries to build a download request.
 */
public interface DownloadRequestBuilderStrategy {

    /**
     * Load the request with a url.
     * @return Itself for chaining.
     */
    DownloadRequestBuilderStrategy load(String url);

    /**
     * Load the request with a resource id.
     * @return Itself for chaining.
     */
    DownloadRequestBuilderStrategy load(int resourceId);

    /**
     * Set an Android resource id for a {@link Drawable} resource to display while a resource is
     * loading.
     * @param resourceId The id of the resource to use as a placeholder
     * @return Itself for chaining.
     */
    DownloadRequestBuilderStrategy placeholder(@DrawableRes int resourceId);

    /**
     * Set a callback to be called for the result of the download request.
     * @param callback The callback to be called for the result of the download request.
     * @return Itself for chaining.
     */
    DownloadRequestBuilderStrategy callback(DownloadRequestCallback callback);

    /**
     * Set the target {@link ImageView} to load the resource into and start the operation.
     * @param imageView The {@link ImageView} the resource will be loaded into.
     * @return The dispatched request.
     */
    DownloadRequestStrategy into(ImageView imageView);
}
