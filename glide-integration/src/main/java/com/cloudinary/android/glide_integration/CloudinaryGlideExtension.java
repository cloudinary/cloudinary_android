package com.cloudinary.android.glide_integration;

import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.cloudinary.Transformation;
import com.cloudinary.android.ResponsiveUrl;

import androidx.annotation.NonNull;

/**
 * Extension class which adds custom cloudinary options when building a request.
 */
@GlideExtension
public class CloudinaryGlideExtension {

    private CloudinaryGlideExtension() { }

    @NonNull
    @GlideOption
    public static BaseRequestOptions<?> transformation(BaseRequestOptions<?> requestOptions, Transformation transformation) {
        RequestOptions options = new RequestOptions().set(CloudinaryRequestModelLoader.TRANSFORMATION, transformation);
        return requestOptions.apply(options);
    }

    @NonNull
    @GlideOption
    public static BaseRequestOptions<?> responsive(BaseRequestOptions<?> requestOptions, ResponsiveUrl responsive) {
        RequestOptions options = new RequestOptions().set(CloudinaryRequestModelLoader.RESPONSIVE, responsive);
        return requestOptions.apply(options);
    }
}
