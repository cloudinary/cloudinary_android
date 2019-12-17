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

import java.util.ArrayList;

/**
 * Helper class to start the UploadWidget and preprocess its results.
 */
public class UploadWidget {

    /**
     * The key used to pass upload widget result data back from {@link UploadWidgetActivity}.
     */
    public static final String RESULT_EXTRA = "upload_widget_result_extra";

    public static final String IMAGES_URI_EXTRA = "images_uri_extra";

    /**
     * Start the {@link UploadWidgetActivity}. Please make sure that you have declared it your manifest.
     *
     * @param activity    The activity which requested the upload widget.
     * @param requestCode A request code to start the upload widget with.
     * @param imageUris   The Uris of all selected images.
     */
    public static void startActivity(@NonNull Activity activity, int requestCode, @NonNull ArrayList<Uri> imageUris) {
        Intent intent = new Intent(activity, UploadWidgetActivity.class);
        intent.putParcelableArrayListExtra(IMAGES_URI_EXTRA, imageUris);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Create a new {@link UploadRequest} with the upload widget's preprocess results.
     *
     * @param result Result data from the upload widget.
     * @return Newly created {@link UploadRequest}.
     * @throws IllegalArgumentException if data does not contain an image uri or an {@link Result}.
     */
    public static UploadRequest preprocessResult(Result result) {
        return MediaManager.get().upload(result.getImageUri())
                .preprocess(ImagePreprocessChain.uploadWidgetChain(result));
    }

    /**
     * Preprocess the {@code uploadRequest}'s with the upload widget results.
     *
     * @param uploadRequest Already constructed upload request.
     * @param result Result data from the upload widget.
     * @return Preprocessed {@link UploadRequest}
     * @throws IllegalArgumentException if data does not contain an image uri or an {@link Result}.
     * @throws IllegalStateException    if {@code uploadRequest} was already dispatched.
     */
    public static UploadRequest preprocessResult(@NonNull UploadRequest uploadRequest, Result result) {
        return uploadRequest.preprocess(ImagePreprocessChain.uploadWidgetChain(result));
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
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    private static void checkDataNotNull(Intent data) {
        ArrayList<UploadWidget.Result> results = data.getParcelableArrayListExtra(UploadWidget.RESULT_EXTRA);

        if (results == null) {
            throw new IllegalArgumentException("Data must contain upload widget results");
        }
    }

    /**
     * Result data of the upload widget activity
     */
    public static final class Result implements Parcelable {

        private Uri imageUri;
        private final CropPoints cropPoints;
        private int rotationAngle;

        private Result(Uri imageUri, CropPoints cropPoints, int rotationAngle) {
            this.imageUri = imageUri;
            this.cropPoints = cropPoints;
            this.rotationAngle = rotationAngle;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(imageUri, flags);
            dest.writeParcelable(cropPoints, flags);
            dest.writeInt(rotationAngle);
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
            imageUri = in.readParcelable(Uri.class.getClassLoader());
            cropPoints = in.readParcelable(CropPoints.class.getClassLoader());
            rotationAngle = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public Uri getImageUri() { return imageUri; }

        public CropPoints getCropPoints() {
            return cropPoints;
        }

        public int getRotationAngle() {
            return rotationAngle;
        }

        /**
         * Construct an {@link UploadWidget.Result} instance
         */
        public static final class Builder {

            private Uri imageUri;
            private CropPoints cropPoints;
            private int rotationAngle;

            public Builder imageUri(Uri imageUri) {
                this.imageUri = imageUri;
                return this;
            }

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

            public Builder rotationAngle(int rotationAngle) {
                this.rotationAngle = rotationAngle;
                return this;
            }

            /**
             * @return Instance of {@link UploadWidget.Result} based on the requested parameters.
             */
            public UploadWidget.Result build() {
                return new UploadWidget.Result(imageUri, cropPoints, rotationAngle);
            }
        }
    }

}
