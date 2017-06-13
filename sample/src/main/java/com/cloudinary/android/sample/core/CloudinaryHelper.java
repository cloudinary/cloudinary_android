package com.cloudinary.android.sample.core;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.Transformation;
import com.cloudinary.android.CldAndroid;
import com.cloudinary.android.Utils;

public class CloudinaryHelper {
    public static String uploadImage(String uri) {
        return CldAndroid.get().upload(Uri.parse(uri))
                .unsigned("sample_app_preset")
                .policy(CldAndroid.get().getGlobalUploadPolicy().newBuilder().maxRetries(10).build())
                .dispatch();
    }

    public static String getCroppedThumbnailUrl(int size, String imageId) {
        return CldAndroid.get().getCloudinary().url()
                .transformation(new Transformation().crop("thumb").gravity("auto").width(size).height(size))
                .generate(imageId);
    }

    public static String getOriginalSizeImage(String imageId) {
        return CldAndroid.get().getCloudinary().url().generate(imageId);
    }

    public static String getUrlForMaxWidth(Context context, String imageId) {
        int width = Utils.getScreenWidth(context);
        return CldAndroid.get().getCloudinary().url().transformation(new Transformation().width(width)).generate(imageId);
    }
}
