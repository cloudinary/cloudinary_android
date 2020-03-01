package com.cloudinary.android.uploadwidget.ui;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import com.cloudinary.android.ui.R;
import com.cloudinary.android.uploadwidget.model.BitmapManager;
import com.cloudinary.android.uploadwidget.model.CropRotateResult;
import com.cloudinary.android.uploadwidget.ui.imageview.UploadWidgetImageView;
import com.cloudinary.android.uploadwidget.utils.MediaType;
import com.cloudinary.android.uploadwidget.utils.UriUtils;

/**
 * Crops and rotates a media file
 */
public class CropRotateFragment extends Fragment {

    private static final String URI_ARG = "uri_arg";

    private UploadWidgetImageView uploadWidgetImageView;
    private Uri uri;
    private Callback callback;

    public CropRotateFragment() {
    }

    /**
     * Instantiate a new {@link CropRotateFragment}.
     *
     * @param uri Uri of the media file to crop and rotate.
     * @param callback Callback to be called when there is a result for the crop and rotate.
     */
    public static CropRotateFragment newInstance(Uri uri, Callback callback) {
        if (uri == null) {
            throw new IllegalArgumentException("Uri must be provided");
        }

        CropRotateFragment fragment = new CropRotateFragment();
        Bundle args = new Bundle();
        args.putString(URI_ARG, uri.toString());
        fragment.setArguments(args);
        fragment.setCallback(callback);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            uri = Uri.parse(arguments.getString(URI_ARG));
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crop_rotate, container, false);
        uploadWidgetImageView = view.findViewById(R.id.imageView);

        MediaType mediaType = UriUtils.getMediaType(getContext(), uri);
        if (mediaType == MediaType.VIDEO) {
            Bitmap videoThumbnail = UriUtils.getVideoThumbnail(getContext(), uri);
            BitmapManager.get().save(getContext(), videoThumbnail, new BitmapManager.SaveCallback() {
                @Override
                public void onSuccess(Uri resultUri) {
                    uploadWidgetImageView.setImageUri(resultUri);
                    uploadWidgetImageView.showCropOverlay();
                }

                @Override
                public void onFailure() { }
            });
        } else {
            uploadWidgetImageView.setImageUri(uri);
            uploadWidgetImageView.showCropOverlay();
        }


        Button doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onCropRotateFinish(uri, getResult(), uploadWidgetImageView.getResultBitmap());
                    onBackPressed();
                }
            }
        });

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onCropRotateCancel(uri);
                }
                onBackPressed();
            }
        });

        ImageView rotateButton = view.findViewById(R.id.rotateButton);
        if (mediaType == MediaType.VIDEO) {
            rotateButton.setVisibility(View.GONE);
        } else {
            rotateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadWidgetImageView.rotateImage();
                }
            });
        }

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_UP) {
                    if (callback != null) {
                        callback.onCropRotateCancel(uri);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (callback != null) {
                callback.onCropRotateCancel(uri);
            }
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set a crop and rotate callback.
     *
     * @param callback Crop and rotate callback to be called for the result.
     */
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private CropRotateResult getResult() {
        return new CropRotateResult(uploadWidgetImageView.getRotationAngle(), uploadWidgetImageView.getCropPoints());
    }

    private void onBackPressed() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }

    /**
     * Callback for the result of the crop and rotate.
     */
    public interface Callback {

        /**
         * Called when finished to crop and rotate.
         *
         * @param uri The source uri.
         * @param result Crop and rotate result.
         * @param resultBitmap Crop and rotate result bitmap.
         */
        void onCropRotateFinish(Uri uri, CropRotateResult result, Bitmap resultBitmap);

        /**
         * Called when canceled to crop and rotate.
         *
         * @param uri The source uri.
         */
        void onCropRotateCancel(Uri uri);

    }

}
