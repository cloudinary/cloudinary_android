package com.cloudinary.android.glide_integration;

import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.android.CloudinaryRequest;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A {@link ModelLoader} for translating a {@link CloudinaryRequest} into {@link java.io.InputStream} data.
 */
public class CloudinaryRequestModelLoader implements ModelLoader<CloudinaryRequest, InputStream> {

    static final Option<Transformation> TRANSFORMATION =
            Option.memory("com.cloudinary.android.glide_cloudinary.CloudinaryRequestModelLoader.Transformation");
    static final Option<ResponsiveUrl> RESPONSIVE =
            Option.memory("com.cloudinary.android.glide_cloudinary.CloudinaryRequestModelLoader.Responsive");;

    private ModelLoader<GlideUrl, InputStream> urlLoader;

    public CloudinaryRequestModelLoader(ModelLoader<GlideUrl, InputStream> urlLoader) {
        this.urlLoader = urlLoader;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull CloudinaryRequest model, int width, int height, @NonNull Options options) {
        Url url = MediaManager.get().url().publicId(model.getPublicId());

        Transformation transformation = model.getTransformation();
        if (transformation == null) {
            transformation = options.get(TRANSFORMATION);
        }
        if (transformation != null) {
            url.transformation(transformation);
        }

        ResponsiveUrl responsive = model.getResponsive();
        if (responsive == null) {
            responsive = options.get(RESPONSIVE);
        }
        if (responsive != null) {
            url = responsive.buildUrl(url, width, height);
        }

        return urlLoader.buildLoadData(new GlideUrl(url.generate()), width, height, options);
    }

    @Override
    public boolean handles(@NonNull CloudinaryRequest model) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<CloudinaryRequest, InputStream> {

        @NonNull
        @Override
        public ModelLoader<CloudinaryRequest, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new CloudinaryRequestModelLoader(multiFactory.build(GlideUrl.class, InputStream.class));
        }

        @Override
        public void teardown() {

        }
    }
}