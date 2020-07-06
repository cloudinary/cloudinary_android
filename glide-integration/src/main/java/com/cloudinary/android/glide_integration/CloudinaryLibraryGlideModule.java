package com.cloudinary.android.glide_integration;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.LibraryGlideModule;
import com.cloudinary.android.CloudinaryRequest;

import java.io.InputStream;

import androidx.annotation.NonNull;

@GlideModule
public class CloudinaryLibraryGlideModule extends LibraryGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.prepend(CloudinaryRequest.class, InputStream.class, new CloudinaryRequestModelLoader.Factory());
    }
}
