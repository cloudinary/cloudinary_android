package com.cloudinary.android.uploadwidget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.uploadwidget.model.CropPoints;
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

    /**
     * The key used to pass the uris to the upload widget.
     */
    public static final String URIS_EXTRA = "uris_extra";

    /**
     * Start the {@link UploadWidgetActivity}. Please make sure that you have declared it your manifest.
     *
     * @param activity    The activity which requested the upload widget.
     * @param requestCode A request code to start the upload widget with.
     * @param uris   Uris of the selected media files.
     */
    public static void startActivity(@NonNull Activity activity, int requestCode, @NonNull ArrayList<Uri> uris) {
        Intent intent = new Intent(activity, UploadWidgetActivity.class);
        intent.putParcelableArrayListExtra(URIS_EXTRA, uris);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Create a preprocessed list of {@link UploadRequest}s from the UploadWidget's results data.
     *
     * @param data Results data from the upload widget.
     * @return Preprocessed {@link UploadRequest}s.
     */
    public static ArrayList<UploadRequest> preprocessResults(Context context, Intent data) {
        checkDataNotNull(data);
        ArrayList<UploadWidget.Result> results = data.getParcelableArrayListExtra(UploadWidget.RESULT_EXTRA);
        ArrayList<UploadRequest> uploadRequests = new ArrayList<>(results.size());

        for (Result result : results) {
            UploadRequest uploadRequest = UploadWidgetResultProcessor.process(context, result);
            uploadRequests.add(uploadRequest);
        }

        return uploadRequests;
    }

    /**
     * Create a new {@link UploadRequest} with the upload widget's preprocess results.
     *
     * @param result Result data from the upload widget.
     * @return Newly created {@link UploadRequest}.
     */
    public static UploadRequest preprocessResult(Context context, Result result) {
        return UploadWidgetResultProcessor.process(context, result);
    }

    /**
     * Preprocess the {@code uploadRequest}'s with the upload widget results.
     *
     * @param uploadRequest Already constructed upload request.
     * @param result Result data from the upload widget.
     * @return Preprocessed {@link UploadRequest}
     * @throws IllegalStateException    if {@code uploadRequest} was already dispatched.
     */
    public static UploadRequest preprocessResult(Context context, @NonNull UploadRequest uploadRequest, Result result) {
        return UploadWidgetResultProcessor.process(context, uploadRequest, result);
    }

    /**
     * Open the native android picker to choose a media file.
     *
     * @param activity    The activity that the native android picker was initiated from.
     * @param requestCode A request code to start the native android picker with.
     */
    public static void openMediaChooser(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/jpg", "image/png", "video/*"});
            intent.setType("(*/*");
        } else {
            intent.setType("image/*|video/*");
        }

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

        /**
         * Source uri.
         */
        public Uri uri;

        /**
         * Pair of cropping points.
         */
        public CropPoints cropPoints;

        /**
         * Angle to rotate.
         */
        public int rotationAngle;

        public Result(Uri uri) {
            this.uri = uri;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(uri, flags);
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
            uri = in.readParcelable(Uri.class.getClassLoader());
            cropPoints = in.readParcelable(CropPoints.class.getClassLoader());
            rotationAngle = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }

}
