package com.cloudinary.android.uploadwidget;

import android.content.Context;
import android.graphics.Point;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.preprocess.Crop;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.preprocess.Rotate;
import com.cloudinary.android.uploadwidget.utils.MediaType;
import com.cloudinary.android.uploadwidget.utils.UriUtils;

class UploadWidgetResultProcessor {

    static UploadRequest process(Context context, UploadWidget.Result result) {
        UploadRequest uploadRequest = MediaManager.get().upload(result.uri);

        return process(context, uploadRequest, result);
    }

    static UploadRequest process(Context context, UploadRequest uploadRequest, UploadWidget.Result result) {
        MediaType mediaType = UriUtils.getMediaType(context, result.uri);

        if (mediaType == MediaType.IMAGE) {
            ImagePreprocessChain imagePreprocessChain = new ImagePreprocessChain();

            if (result.rotationAngle != 0) {
                imagePreprocessChain.addStep(new Rotate(result.rotationAngle));
            }
            if (result.cropPoints != null) {
                imagePreprocessChain.addStep(new Crop(result.cropPoints.getPoint1(), result.cropPoints.getPoint2()));
            }

            uploadRequest.preprocess(imagePreprocessChain);
        } else if (mediaType == MediaType.VIDEO) {
            uploadRequest.option("resource_type", "video");

            if (result.cropPoints != null) {
                Point p1 = result.cropPoints.getPoint1();
                Point p2 = result.cropPoints.getPoint2();

                uploadRequest.option("transformation", new Transformation().crop("crop")
                        .x(p1.x).y(p1.y).width(p2.x - p1.x).height(p2.y - p1.y));
            }
        }

        return uploadRequest;
    }
}
