package com.cloudinary.android.uploadwidget.ui;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudinary.android.R;
import com.cloudinary.android.uploadwidget.UploadWidget;
import com.cloudinary.android.uploadwidget.ui.imagepreview.UploadWidgetImageView;

/**
 * Previews an image and lets the user to optionally edit it.
 */
public class CropRotateFragment extends Fragment {

    private static final String IMAGE_URI_ARG = "image_uri_arg";
    private Callback callback;

    private UploadWidgetImageView uploadWidgetImageView;
    private Button doneButton;
    private Button cancelButton;

    private Uri imageUri;
    private ImageView rotateButton;

    public CropRotateFragment() {
    }

    /**
     * Instantiates a new {@link CropRotateFragment} with an image uri argument.
     *
     * @param imageUri Uri of the image to be displayed.
     */
    public static CropRotateFragment newInstance(Uri imageUri, Callback callback) {
        if (imageUri == null) {
            throw new IllegalArgumentException("Image uri must be provided");
        }

        CropRotateFragment fragment = new CropRotateFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URI_ARG, imageUri.toString());
        fragment.setArguments(args);
        fragment.setCallback(callback);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            imageUri = Uri.parse(arguments.getString(IMAGE_URI_ARG));
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crop_rotate, container, false);
        uploadWidgetImageView = view.findViewById(R.id.imageUriImageView);
        uploadWidgetImageView.setImageUri(imageUri);
        uploadWidgetImageView.startCropping();

        doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onCropRotateFinish(imageUri, getResult());
                    onBackPressed();
                }
            }
        });

        cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onCropRotateCancel(imageUri);
                }
                onBackPressed();
            }
        });

        rotateButton = view.findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadWidgetImageView.rotateImage();
            }
        });

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_UP) {
                    if (callback != null) {
                        callback.onCropRotateCancel(imageUri);
                    }
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.cropRotateToolbar);
            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.crop_rotate_menu, menu);
        final MenuItem aspectRatioItem = menu.findItem(R.id.aspect_ratio_action);

        final View aspectRatioActionView = aspectRatioItem.getActionView();
        aspectRatioActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView aspectRatioTextView = aspectRatioActionView.findViewById(R.id.aspectRatioTextView);
                ImageView aspectRatioImageView = aspectRatioActionView.findViewById(R.id.aspectRatioImageView);

                if (uploadWidgetImageView.isAspectRatioLocked()) {
                    uploadWidgetImageView.setAspectRatioLocked(false);
                    aspectRatioTextView.setText(getString(R.string.menu_item_aspect_ratio_unlocked));
                    aspectRatioImageView.setImageResource(R.drawable.unlock);
                } else {
                    uploadWidgetImageView.setAspectRatioLocked(true);
                    aspectRatioTextView.setText(getString(R.string.menu_item_aspect_ratio_locked));
                    aspectRatioImageView.setImageResource(R.drawable.lock);
                }

            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public UploadWidget.Result getResult() {
        return new UploadWidget.Result.Builder()
                .imageUri(imageUri)
                .cropPoints(uploadWidgetImageView.getCropPoints())
                .rotationAngle(uploadWidgetImageView.getRotationAngle())
                .build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (callback != null) {
                callback.onCropRotateCancel(imageUri);
            }
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackPressed() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }

    /**
     * Callback for the result of the crop and rotate fragment.
     */
    public interface Callback {

        /**
         * Called when finished to edit the image.
         *
         * @param imageUri The source image uri.
         * @param result   Result of the image editing.
         */
        void onCropRotateFinish(Uri imageUri, UploadWidget.Result result);

        /**
         * Called when canceled to edit the image.
         *
         * @param imageUri The source image uri.
         */
        void onCropRotateCancel(Uri imageUri);

    }
}
