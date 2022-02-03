package com.cloudinary.android.uploadwidget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.uploadwidget.model.CropPoints;
import com.cloudinary.android.uploadwidget.ui.UploadWidgetActivity;

import java.util.ArrayList;
import java.util.Collection;

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

    public static final String ACTION_EXTRA = "required_action_extra";

    /**
     * Start the {@link UploadWidgetActivity} with a pre-populated list of files to upload, and return
     * a list of upload request to dispatch. This is equivalent to RequiredAction.NONE.
     * Deprecated - please use {@link #startActivity(Activity, int, Options)} directly.
     *
     * @param activity    The activity which requested the upload widget.
     * @param requestCode A request code to start the upload widget with.
     * @param uris        Uris of the selected media files.
     */
    @Deprecated
    public static void startActivity(@NonNull Activity activity, int requestCode, @NonNull ArrayList<Uri> uris) {
        startActivity(activity, requestCode, new Options(Action.NONE, uris));
    }

    /**
     * Start the {@link UploadWidgetActivity} configured for full process - Launch file selection UI
     * as well as dispatching the created upload request automatically.
     *
     * @param activity    The activity which requested the upload widget.
     * @param requestCode A request code to start the upload widget with.
     */
    public static void startActivity(@NonNull Activity activity, int requestCode) {
        startActivity(activity, requestCode, new Options(Action.DISPATCH, null));
    }

    /**
     * Start the {@link UploadWidgetActivity} configured according to the supplied launch options.
     *
     * @param activity      The activity which requested the upload widget.
     * @param requestCode   A request code to start the upload widget with.
     * @param options The launch option to define the required upload widget behaviour
     */
    public static void startActivity(@NonNull Activity activity, int requestCode, Options options) {
        Intent intent = new Intent(activity, UploadWidgetActivity.class).putExtra(ACTION_EXTRA, options.action);

        if (options.uris != null && !options.uris.isEmpty()) {
            intent.putParcelableArrayListExtra(URIS_EXTRA, new ArrayList<Parcelable>(options.uris));
        }

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
     * @param result        Result data from the upload widget.
     * @return Preprocessed {@link UploadRequest}
     * @throws IllegalStateException if {@code uploadRequest} was already dispatched.
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
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

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

        /**
         * The request id, in case the full flow was requested and an upload request was already
         * started or dispatched.
         */
        public String requestId;


        public Result(Uri uri) {
            this.uri = uri;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(uri, flags);
            dest.writeParcelable(cropPoints, flags);
            dest.writeInt(rotationAngle);
            dest.writeString(requestId);
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
            requestId = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }

    /**
     * This class is used to define the required launch behaviour of the upload widget.
     */
    public static class Options {
        final Action action;
        final Collection<Uri> uris;

        /**
         * Construct a new instance to use when launching the upload widget activity.
         *
         * @param action Indicates the widget how to handle the selected files. This also
         *                       affects the result received later in onActivityResult. When the action
         *                       used is DISPATCH or START_NOW the widget returns a list of request IDs.
         *                       When the action is NONE, the widget returns results that needs to be
         *                       processed into UploadRequest, allowing customization before dispatching/starting.
         * @param uris           A list of Uris of files to display and upload.
         */
        public Options(@NonNull Action action, @Nullable Collection<Uri> uris) {
            this.action = action;
            this.uris = uris;
        }
    }

    /**
     * Define how the upload widget handles the selected files to upload
     */
    public enum Action {
        /**
         * Dispatch the selected files within the upload widget, and return request IDs.
         */
        DISPATCH,

        /**
         * Immediately start the selected files within the upload widget, and return request IDs.
         */
        START_NOW,

        /**
         * Create the request data and preprocess configuration without starting any request.
         */
        NONE,
    }

}
