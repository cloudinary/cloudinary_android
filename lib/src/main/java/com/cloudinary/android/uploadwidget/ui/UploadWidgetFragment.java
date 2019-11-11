package com.cloudinary.android.uploadwidget.ui;


import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.cloudinary.android.R;
import com.cloudinary.android.uploadwidget.CropPoints;
import com.cloudinary.android.uploadwidget.UploadWidget;

import java.lang.ref.WeakReference;

/**
 * Previews an image and lets the user to optionally edit it.
 */
public class UploadWidgetFragment extends Fragment {

    private static final String IMAGE_URI_ARG = "image_uri_arg";
    private WeakReference<UploadWidgetListener> uploadWidgetListener;
    private Uri imageUri;
    private ImageView imageView;
    private Button confirmButton;

    public UploadWidgetFragment() { }

    /**
     * Instantiates a new {@link UploadWidgetFragment} with an image uri argument and an {@link UploadWidgetListener}.
     * @param imageUri Uri of the image to be displayed.
     * @param listener Listener for the Upload Widget.
     */
    public static UploadWidgetFragment newInstance(Uri imageUri, UploadWidgetListener listener) {
        if (imageUri == null) {
            throw new IllegalArgumentException("Image uri must be provided");
        }

        UploadWidgetFragment fragment = new UploadWidgetFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URI_ARG, imageUri.toString());
        fragment.setArguments(args);
        fragment.setUploadWidgetListener(listener);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            imageUri = Uri.parse(arguments.getString(IMAGE_URI_ARG));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_widget, container, false);

        imageView = view.findViewById(R.id.imageUriImageView);
        imageView.setImageURI(imageUri);

        confirmButton = view.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropPoints cropPoints = new CropPoints(new Point(0, 0), new Point(200, 200));
                UploadWidget.Result result = new UploadWidget.Result.Builder()
                        .cropPoints(cropPoints)
                        .build();

                UploadWidgetListener listener = uploadWidgetListener.get();
                if (listener != null) {
                    listener.onConfirm(imageUri, result);
                }
            }
        });

        return view;
    }

    /**
     * Sets a uploadWidgetListener for the Upload Widget.
     */
    public void setUploadWidgetListener(UploadWidgetListener listener) {
        this.uploadWidgetListener = new WeakReference<>(listener);
    }

    /**
     * Listener for the Upload Widget.
     */
    public interface UploadWidgetListener {

        /**
         * Called when the upload widget (optional) edits are confirmed.
         * @param imageUri Original image uri.
         * @param result   Confirmed edits' results.
         */
        void onConfirm(Uri imageUri, UploadWidget.Result result);
    }
}
