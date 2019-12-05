package com.cloudinary.android.uploadwidget.ui;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.cloudinary.android.uploadwidget.CropPoints;
import com.cloudinary.android.uploadwidget.UploadWidget;
import com.cloudinary.android.uploadwidget.ui.imagepreview.UploadWidgetImageView;

/**
 * Previews an image and lets the user to optionally edit it.
 */
public class UploadWidgetFragment extends Fragment {

    private static final String IMAGE_URI_ARG = "image_uri_arg";

    private UploadWidgetImageView uploadWidgetImageView;
    private FloatingActionButton uploadFab;
    private Button doneButton;
    private Button cancelButton;

    private Uri imageUri;
    private boolean isEditable = true;

    public UploadWidgetFragment() { }

    /**
     * Instantiates a new {@link UploadWidgetFragment} with an image uri argument and an {@link UploadWidgetListener}.
     * @param imageUri Uri of the image to be displayed.
     */
    public static UploadWidgetFragment newInstance(Uri imageUri) {
        if (imageUri == null) {
            throw new IllegalArgumentException("Image uri must be provided");
        }

        UploadWidgetFragment fragment = new UploadWidgetFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URI_ARG, imageUri.toString());
        fragment.setArguments(args);

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
        View view = inflater.inflate(R.layout.fragment_upload_widget, container, false);
        uploadWidgetImageView = view.findViewById(R.id.imageUriImageView);
        uploadWidgetImageView.setImageUri(imageUri);

        doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadWidgetImageView.cropImage();
                setPreviewMode(false);
            }
        });

        cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadWidgetImageView.stopCropping();
                setPreviewMode(true);
            }
        });

        uploadFab = view.findViewById(R.id.uploadFab);
        uploadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropPoints cropPoints = uploadWidgetImageView.getCropPoints();
                UploadWidget.Result result = new UploadWidget.Result.Builder()
                        .cropPoints(cropPoints)
                        .build();

                if (getActivity() instanceof UploadWidgetListener) {
                    ((UploadWidgetListener) getActivity()).onConfirm(imageUri, result);
                }
            }
        });

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_UP) {
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
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
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
        inflater.inflate(R.menu.upload_widget_menu, menu);
        final MenuItem aspectRatioItem = menu.findItem(R.id.aspect_ratio_action);
        final MenuItem cropItem = menu.findItem(R.id.crop_action);

        if (isEditable) {
            final View cropActionView = cropItem.getActionView();
            final View aspectRatioActionView = aspectRatioItem.getActionView();
            cropActionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    aspectRatioItem.setVisible(true);
                    uploadFab.hide();
                    doneButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                    cropItem.setVisible(false);
                    uploadWidgetImageView.startCropping();
                }
            });

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
        } else {
            cropItem.setVisible(false);
            aspectRatioItem.setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPreviewMode(boolean isEditable) {
        this.isEditable = isEditable;
        cancelButton.setVisibility(View.INVISIBLE);
        doneButton.setVisibility(View.INVISIBLE);
        uploadFab.show();
        invalidateOptionsMenu();
    }

    private void invalidateOptionsMenu() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void onBackPressed() {
        if (uploadWidgetImageView.isCropStarted()) {
            uploadWidgetImageView.stopCropping();
            setPreviewMode(true);
        } else {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.onBackPressed();
            }
        }
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
