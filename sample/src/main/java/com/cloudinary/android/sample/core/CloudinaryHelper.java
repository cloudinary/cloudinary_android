package com.cloudinary.android.sample.core;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.app.MainApplication;
import com.cloudinary.android.sample.app.Utils;
import com.cloudinary.android.sample.model.EffectData;
import com.cloudinary.android.sample.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CloudinaryHelper {
    public static String uploadImage(String uri) {
        return MediaManager.get().upload(Uri.parse(uri))
                .unsigned("sample_app_preset")
                .policy(MediaManager.get().getGlobalUploadPolicy().newBuilder().maxRetries(10).build())
                .dispatch();
    }

    public static String getCroppedThumbnailUrl(int size, Resource resource) {

        return MediaManager.get().getCloudinary().url()
                .resourceType(resource.getResourceType())
                .transformation(new Transformation().crop("thumb").gravity("auto").width(size).height(size))
                .generate(resource.getCloudinaryPublicId());
    }

    public static String getOriginalSizeImage(String imageId) {
        return MediaManager.get().getCloudinary().url().generate(imageId);
    }

    public static String getUrlForMaxWidth(Context context, String imageId) {
        int width = Utils.getScreenWidth(context);
        return MediaManager.get().getCloudinary().url().transformation(new Transformation().width(width)).generate(imageId);
    }

    public static void deleteByToken(final String token, final DeleteCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map res = MediaManager.get().getCloudinary().uploader().deleteByToken(token);
                    if (res != null && res.containsKey("result") && res.get("result").equals("ok")) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Unknown error.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public static List<EffectData> generateEffectsList(Context context, int screenWidth, int thumbHeight, String publicId) {
        Cloudinary cloudinary = MediaManager.get().getCloudinary();
        List<EffectData> effects = new ArrayList<>();
        String thumbUrl;
        String imageUrl;

        thumbUrl = cloudinary.url().transformation(new Transformation().aspectRatio(40, 30).height(thumbHeight).crop("fill").gravity("faces").effect("sharpen:200")).format("webp").generate(publicId);
        imageUrl = cloudinary.url().transformation(new Transformation().aspectRatio(40, 30).width(screenWidth).crop("fill").gravity("faces").effect("sharpen:200")).format("webp").generate(publicId);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_desc_face_sharpen)));

        thumbUrl = cloudinary.url().transformation(new Transformation().crop("fill").gravity("faces").radius(50).effect("saturation:50").height(thumbHeight)).format("webp").generate(publicId);
        imageUrl = cloudinary.url().transformation(new Transformation().crop("fill").gravity("faces").radius(50).effect("saturation:50").width(screenWidth)).format("webp").generate(publicId);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_desc_face_sat_round)));

        thumbUrl = cloudinary.url().transformation(new Transformation().aspectRatio(100, 50).height(thumbHeight).crop("fill").gravity("faces").effect("blue:100")).format("webp").generate(publicId);
        imageUrl = cloudinary.url().transformation(new Transformation().aspectRatio(100, 50).width(screenWidth).crop("fill").gravity("faces").effect("blue:100")).format("webp").generate(publicId);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_desc_wide_blue)));

        thumbUrl = cloudinary.url().transformation(new Transformation().aspectRatio(50, 100).height(thumbHeight).crop("fill").gravity("faces").effect("sepia")).format("webp").generate(publicId);
        imageUrl = cloudinary.url().transformation(new Transformation().aspectRatio(50, 100).width(screenWidth).crop("fill").gravity("faces").effect("sepia")).format("webp").generate(publicId);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_desc_narrow_sepia)));

        return effects;
    }

    public static String getPrettyErrorMessage(int error) {
        int stringResId;
        switch (error) {
            case ErrorInfo.NO_ERROR:
                stringResId = R.string.no_error_message;
                break;
            case ErrorInfo.FILE_DOES_NOT_EXIST:
                stringResId = R.string.file_does_not_exist;
                break;
            case ErrorInfo.NETWORK_ERROR:
                stringResId = R.string.network_error;
                break;
            case ErrorInfo.OPTIONS_FAILURE:
                stringResId = R.string.options_failure;
                break;
            case ErrorInfo.PAYLOAD_EMPTY:
                stringResId = R.string.payload_empty;
                break;
            case ErrorInfo.PAYLOAD_LOAD_FAILURE:
                stringResId = R.string.payload_could_not_load;
                break;
            case ErrorInfo.RESOURCE_DOES_NOT_EXIST:
                stringResId = R.string.resource_does_not_exist;
                break;
            case ErrorInfo.SIGNATURE_FAILURE:
                stringResId = R.string.signature_failure;
                break;
            case ErrorInfo.URI_DOES_NOT_EXIST:
                stringResId = R.string.uri_does_not_exist;
                break;
            case ErrorInfo.UNKNOWN_ERROR:
            default:
                stringResId = R.string.unknown_error;
        }

        return MainApplication.get().getString(stringResId);
    }

    public interface DeleteCallback {
        void onSuccess();

        void onError(String error);
    }
}
