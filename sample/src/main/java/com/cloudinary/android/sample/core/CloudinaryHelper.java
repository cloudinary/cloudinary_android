package com.cloudinary.android.sample.core;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.policy.TimeWindow;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.preprocess.Parameters;
import com.cloudinary.android.preprocess.VideoPreprocessChain;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.app.MainApplication;
import com.cloudinary.android.sample.app.Utils;
import com.cloudinary.android.sample.model.EffectData;
import com.cloudinary.android.sample.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CloudinaryHelper {
    public static String uploadImageResource(Resource resource) {
        UploadRequest request = MediaManager.get().upload(Uri.parse(resource.getLocalUri()))
                .unsigned("android_sample")
                .constrain(TimeWindow.getDefault())
                .option("resource_type", "auto")
                .maxFileSize(100 * 1024 * 1024) // max 100mb
                .policy(MediaManager.get().getGlobalUploadPolicy().newBuilder().maxRetries(2).build());
        // scale down images above 2000 width/height, and re-encode as webp with 80 quality to save bandwidth
        request.preprocess(ImagePreprocessChain.limitDimensionsChain(2000, 2000)
                .saveWith(new BitmapEncoder(BitmapEncoder.Format.WEBP, 80)));

        return request.dispatch(MainApplication.get());
    }

    public static String uploadVideoResource(Resource resource) {
        UploadRequest request = MediaManager.get().upload(Uri.parse(resource.getLocalUri()))
                .option("resource_type", "auto");

        Parameters parameters = new Parameters();
        parameters.setRequestId(request.getRequestId());
        parameters.setFrameRate(30);
        parameters.setWidth(1280);
        parameters.setHeight(720);
        parameters.setKeyFramesInterval(3);
        parameters.setTargetAudioBitrateKbps(128);
        parameters.setTargetVideoBitrateKbps((int) (3.3 * 1024 * 1024));

        request.preprocess(VideoPreprocessChain.videoTranscodingChain(parameters));

        return request.dispatch(MainApplication.get());
    }

    public static String getCroppedThumbnailUrl(int size, Resource resource) {

        return MediaManager.get().getCloudinary().url()
                .resourceType(resource.getResourceType())
                .transformation(new Transformation().gravity("auto").width(size).height(size))
                .format("webp")
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
                    final Map res = MediaManager.get().getCloudinary().uploader().deleteByToken(token);
                    MainApplication.get().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (res != null && res.containsKey("result") && res.get("result").equals("ok")) {
                                callback.onSuccess();
                            } else {
                                callback.onError("Unknown error.");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public static List<EffectData> generateEffectsList(Context context, Resource resource) {
        if (resource.getResourceType().equals("video")) {
            return generateVideoEffects(context, resource);
        } else {
            return generateImageEffects(context, resource);
        }
    }

    private static List<EffectData> generateImageEffects(Context context, Resource resource) {
        List<EffectData> effects = new ArrayList<>();

        Transformation transformation;

        transformation = new Transformation().effect("sharpen", 250).fetchFormat("webp");
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_name_sharpen), context.getString(R.string.effect_desc_face_sharpen)));

        transformation = new Transformation().effect("oil_paint", 100).fetchFormat("webp");
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_name_oil_paint), context.getString(R.string.effect_desc_face_oilpaint)));

        transformation = new Transformation().effect("sepia").fetchFormat("webp");
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_name_sepia), context.getString(R.string.effect_desc_narrow_sepia)));

        transformation = new Transformation().radius(50).effect("saturation", 100);
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_name_round_corners), context.getString(R.string.effect_desc_face_sat_round)));

        transformation = new Transformation().effect("blue:100").fetchFormat("webp");
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_name_blue), context.getString(R.string.effect_desc_wide_blue)));

        return effects;
    }

    private static List<EffectData> generateVideoEffects(Context context, Resource resource) {
        List<EffectData> effects = new ArrayList<>();

        Transformation transformation;

        transformation = new Transformation().angle(20);
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_video_name_rotation), context.getString(R.string.effect_video_rotate)));

        transformation = new Transformation().effect("fade", 1000);
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_video_name_fade_in), context.getString(R.string.effect_video_fade_in)));

        transformation = new Transformation().chain().overlay("video:" + resource.getCloudinaryPublicId()).width(0.5).flags("relative").gravity("north_east");
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_video_name_overlay), context.getString(R.string.effect_desc_video_overlay)));

        transformation = new Transformation().effect("noise", 50);
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_video_name_noise), context.getString(R.string.effect_desc_video_noise)));

        transformation = new Transformation().effect("blur", 200);
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_video_name_blur), context.getString(R.string.effect_desc_video_blur)));

        transformation = new Transformation().effect("reverse");
        effects.add(new EffectData(resource.getCloudinaryPublicId(), transformation, context.getString(R.string.effect_video_name_reverse), context.getString(R.string.effect_desc_video_reverse)));

        return effects;
    }

    public static String getCloudName() {
        return MediaManager.get().getCloudinary().config.cloudName;
    }

    public interface DeleteCallback {
        void onSuccess();

        void onError(String error);
    }
}
