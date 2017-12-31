package com.cloudinary.android.sample.core;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.policy.TimeWindow;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.app.MainApplication;
import com.cloudinary.android.sample.app.Utils;
import com.cloudinary.android.sample.model.EffectData;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CloudinaryHelper {
    public static String uploadResource(Resource resource, boolean preprocess) {
        UploadRequest request = MediaManager.get().upload(Uri.parse(resource.getLocalUri()))
                .unsigned("sample_app_preset")
                .constrain(TimeWindow.immediate())
                .option("resource_type", "auto")
                .maxFileSize(100 * 1024 * 1024) // max 100mb
                .policy(MediaManager.get().getGlobalUploadPolicy().newBuilder().maxRetries(10).build());
        if (preprocess) {
            // scale down images above 2000 width/height, and re-encode as webp with 80 quality to save bandwidth
            request.preprocess(ImagePreprocessChain.limitDimensionsChain(2000, 2000)
                    .saveWith(new BitmapEncoder(BitmapEncoder.Format.WEBP, 80)));

        }

        return request.dispatch(MainApplication.get());
    }

    public static String getCroppedThumbnailUrl(int size, Resource resource) {

        return MediaManager.get().getCloudinary().url()
                .resourceType(resource.getResourceType())
                .transformation(new Transformation().crop("thumb").gravity("auto").width(size).height(size))
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

    public static List<EffectData> generateEffectsList(Context context, int screenWidth, int thumbHeight, Resource resource) {
        if (resource.getResourceType().equals("video")) {
            return generateVideoEffects(context, screenWidth, thumbHeight, resource);
        } else {
            return generateImageEffects(context, screenWidth, thumbHeight, resource);
        }
    }

    private static List<EffectData> generateImageEffects(Context context, int screenWidth, int thumbHeight, Resource resource) {
        List<EffectData> effects = new ArrayList<>();

        String thumbUrl;
        String imageUrl;

        thumbUrl = getUrlForTransformation(new Transformation().aspectRatio(40, 30).height(thumbHeight).crop("thumb").gravity("faces").effect("sharpen", 250), resource);
        imageUrl = getUrlForTransformation(new Transformation().aspectRatio(40, 30).width(screenWidth).crop("thumb").gravity("faces").effect("sharpen", 250), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_name_sharpen), context.getString(R.string.effect_desc_face_sharpen)));

        thumbUrl = getUrlForTransformation(new Transformation().aspectRatio(40, 30).height(thumbHeight).crop("thumb").gravity("faces").effect("oil_paint", 100), resource);
        imageUrl = getUrlForTransformation(new Transformation().aspectRatio(40, 30).width(screenWidth).crop("thumb").gravity("faces").effect("oil_paint", 100), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_name_oil_paint), context.getString(R.string.effect_desc_face_oilpaint)));

        thumbUrl = getUrlForTransformation(new Transformation().aspectRatio(50, 100).height(thumbHeight).crop("thumb").gravity("faces").effect("sepia"), resource);
        imageUrl = getUrlForTransformation(new Transformation().aspectRatio(50, 100).width(screenWidth).crop("thumb").gravity("faces").effect("sepia"), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_name_sepia), context.getString(R.string.effect_desc_narrow_sepia)));

        thumbUrl = getUrlForTransformation(new Transformation().crop("scale").radius(50).effect("saturation", 100).height(thumbHeight), resource);
        imageUrl = getUrlForTransformation(new Transformation().crop("scale").radius(50).effect("saturation", 100).width(screenWidth), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_name_round_corners), context.getString(R.string.effect_desc_face_sat_round)));

        thumbUrl = getUrlForTransformation(new Transformation().aspectRatio(100, 50).height(thumbHeight).crop("thumb").gravity("faces").effect("blue:100"), resource);
        imageUrl = getUrlForTransformation(new Transformation().aspectRatio(100, 50).width(screenWidth).crop("thumb").gravity("faces").effect("blue:100"), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_name_blue), context.getString(R.string.effect_desc_wide_blue)));

        return effects;
    }

    private static List<EffectData> generateVideoEffects(Context context, int screenWidth, int thumbHeight, Resource resource) {
        List<EffectData> effects = new ArrayList<>();
        String videoThumbFormat = "webp";

        String thumbUrl;
        String imageUrl;
        int width = screenWidth / 3;

        thumbUrl = getUrlForTransformation(new Transformation().angle(20).height(thumbHeight), resource, videoThumbFormat);
        imageUrl = getUrlForTransformation(new Transformation().angle(20).width(width), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_video_name_rotation), context.getString(R.string.effect_video_rotate)));

        thumbUrl = getUrlForTransformation(new Transformation().effect("fade", 1000).height(thumbHeight), resource, videoThumbFormat);
        imageUrl = getUrlForTransformation(new Transformation().effect("fade", 1000).width(width), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_video_name_fade_in), context.getString(R.string.effect_video_fade_in)));

        thumbUrl = getUrlForTransformation(new Transformation().crop("scale").height(thumbHeight), resource, videoThumbFormat);
        Object overlayWidth = width / 3;
        imageUrl = getUrlForTransformation(new Transformation().crop("scale").width(width).chain().overlay("video:" + resource.getCloudinaryPublicId()).width(overlayWidth).gravity("north_east"), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_video_name_overlay), context.getString(R.string.effect_desc_video_overlay)));

        thumbUrl = getUrlForTransformation(new Transformation().crop("scale").height(thumbHeight).effect("noise", 50), resource, videoThumbFormat);
        imageUrl = getUrlForTransformation(new Transformation().crop("scale").width(width).effect("noise", 50), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_video_name_noise), context.getString(R.string.effect_desc_video_noise)));

        thumbUrl = getUrlForTransformation(new Transformation().crop("scale").height(thumbHeight).effect("blur", 200), resource, videoThumbFormat);
        imageUrl = getUrlForTransformation(new Transformation().crop("scale").width(width).effect("blur", 200), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_video_name_blur), context.getString(R.string.effect_desc_video_blur)));

        thumbUrl = getUrlForTransformation(new Transformation().crop("scale").height(thumbHeight).effect("reverse"), resource, videoThumbFormat);
        imageUrl = getUrlForTransformation(new Transformation().crop("scale").width(width).effect("reverse"), resource);
        effects.add(new EffectData(thumbUrl, imageUrl, context.getString(R.string.effect_video_name_reverse), context.getString(R.string.effect_desc_video_reverse)));

        return effects;
    }

    private static String getUrlForTransformation(Transformation transformation, Resource resource) {
        return getUrlForTransformation(transformation, resource, null);
    }

    private static String getUrlForTransformation(Transformation transformation, Resource resource, String format) {

        if (StringUtils.isBlank(format) && resource.getResourceType().equals("image")) {
            format = "webp";
        }

        return MediaManager.get().url().resourceType(resource.getResourceType()).transformation(transformation).format(format).generate(resource.getCloudinaryPublicId());
    }

    public static String getCloudName() {
        return MediaManager.get().getCloudinary().config.cloudName;
    }

    public interface DeleteCallback {
        void onSuccess();

        void onError(String error);
    }
}
