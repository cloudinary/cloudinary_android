package com.cloudinary.android.download;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.cloudinary.Transformation;
import com.cloudinary.android.ResponsiveUrl;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;

/**
 * Builds a download request.
 */
public interface DownloadRequestBuilder {

    /**
     * Load the request with a resource id.
     * @return Itself for chaining.
     */
    DownloadRequestBuilder load(@IdRes int resource);

    /**
     * Load the request with a String source. The source can either be a remote url, or a cloudinary publicId.
     * In the case of a remote url, all cloudinary related builder options will not take place.
     * @return Itself for chaining.
     */
    DownloadRequestBuilder load(String source);

    /**
     * Set a {@link Transformation} that will be used to generate the url with.
     * Only applies if {@link #load(String)} was called with a cloudinary publicId.
     * @return Itself for chaining.
     */
    DownloadRequestBuilder transformation(Transformation transformation);

    /**
     * Set a {@link ResponsiveUrl} that will be used to generate the url with.
     * Only applies if {@link #load(String)} was called with a cloudinary publicId.
     * @return Itself for chaining
     */
    DownloadRequestBuilder responsive(ResponsiveUrl responsiveUrl);

    /**
     * Set a {@link ResponsiveUrl.Preset} that will be used to generate the url with.
     * Only applies if {@link #load(String)} was called with a cloudinary publicId.
     * @return Itself for chaining
     */
    DownloadRequestBuilder responsive(ResponsiveUrl.Preset responsivePreset);

    /**
     * Sets an Android resource id for a {@link Drawable} resource to display while a resource is
     * loading.
     * @param resourceId The id of the resource to use as a placeholder
     * @return Itself for chaining.
     */
    DownloadRequestBuilder placeholder(@DrawableRes int resourceId);

    /**
     * Set a callback to be called for the result of the download request.
     * @param callback The callback to be called for the result of the download request.
     * @return Itself for chaining.
     */
    DownloadRequestBuilder callback(DownloadRequestCallback callback);

    /**
     * Set the target {@link ImageView} to load the resource into and start the operation.
     * @param imageView The {@link ImageView} the resource will be loaded into.
     * @return The dispatched request.
     */
    DownloadRequest into(ImageView imageView);
}
