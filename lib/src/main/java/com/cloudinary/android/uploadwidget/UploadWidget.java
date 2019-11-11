package com.cloudinary.android.uploadwidget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.uploadwidget.ui.UploadWidgetActivity;

/**
 * Helper class to start the UploadWidget and preprocess its results.
 */
public class UploadWidget {

    /**
     * The key used to pass upload widget result data back from {@link UploadWidgetActivity}.
     */
    public static final String RESULT_EXTRA = "upload_widget_result_extra";

    /**
     * Start the {@link UploadWidgetActivity}. Please make sure that you have declared it your manifest.
     *
     * @param activity    The activity which requested the upload widget.
     * @param requestCode A request code to start the upload widget with.
     * @param uri         The image uri to be displayed.
     */
    public static void startActivity(@NonNull Activity activity, int requestCode, @NonNull Uri uri) {
        Intent intent = new Intent(activity, UploadWidgetActivity.class);
        intent.setData(uri);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Create a new {@link UploadRequest} with the upload widget's preprocess results.
     *
     * @param data Result data from the upload widget.
     * @return Newly created {@link UploadRequest}.
     * @throws IllegalArgumentException if data does not contain an image uri or an {@link Result}.
     */
    public static UploadRequest preprocessResult(@NonNull Intent data) {
        checkDataNotNull(data);
        Uri uri = data.getData();
        Result result = data.getParcelableExtra(RESULT_EXTRA);
        CropPoints cropPoints = result.getCropPoints();

        return MediaManager.get().upload(uri)
                .preprocess(ImagePreprocessChain.cropChain(cropPoints.getPoint1(), cropPoints.getPoint2()));
    }

    /**
     * Preprocess the {@code uploadRequest}'s with the upload widget results.
     *
     * @param uploadRequest Already constructed upload request.
     * @param data          Result data from the upload widget.
     * @return Preprocessed {@link UploadRequest}
     * @throws IllegalArgumentException if data does not contain an image uri or an {@link Result}.
     * @throws IllegalStateException    if {@code uploadRequest} was already dispatched.
     */
    public static UploadRequest preprocessResult(@NonNull UploadRequest uploadRequest, @NonNull Intent data) {
        checkDataNotNull(data);
        Result result = data.getParcelableExtra(RESULT_EXTRA);
        CropPoints cropPoints = result.getCropPoints();

        return uploadRequest.preprocess(ImagePreprocessChain.cropChain(cropPoints.getPoint1(), cropPoints.getPoint2()));
    }

    /**
     * Open the native android picker to choose an image.
     *
     * @param activity    The activity that the native android picker was initiated from.
     * @param requestCode A request code to start the native android picker with.
     */
    public static void openMediaChooser(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    private static void checkDataNotNull(Intent data) {
        Uri uri = data.getData();
        Result result = data.getParcelableExtra(RESULT_EXTRA);

        if (uri == null || result == null) {
            throw new IllegalArgumentException("Data must contain an image uri and an upload widget result");
        }
    }

    /**
     * Result data of the upload widget activity
     */
    public static final class Result implements Parcelable {

        private final CropPoints cropPoints;

        private Result(CropPoints cropPoints) {
            this.cropPoints = cropPoints;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(cropPoints, flags);
        }

        public static final Creator<Result> CREATOR = new Creator<Result>() {
            @Override
            public Result createFromParcel(Parcel in) {
                return new Result(in);
            }

            @Override
            public Result[] newArray(int size) {
                return new Result[size];
            }
        };

        protected Result(Parcel in) {
            cropPoints = in.readParcelable(CropPoints.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public CropPoints getCropPoints() {
            return cropPoints;
        }

        /**
         * Construct an {@link UploadWidget.Result} instance
         */
        public static final class Builder {

            private CropPoints cropPoints;

            /**
             * Sets the cropping points to crop the image. If the points make the same diagonal size
             * as the original image, it will be returned unchanged.
             * @param cropPoints Pair of points that make a diagonal to crop the image.
             * @return Itself for chaining operation.
             */
            public Builder cropPoints(CropPoints cropPoints) {
                this.cropPoints = cropPoints;
                return this;
            }

            /**
             * @return Instance of {@link UploadWidget.Result} based on the requested parameters.
             */
            public UploadWidget.Result build() {
                return new UploadWidget.Result(cropPoints);
            }
        }
    }

}
