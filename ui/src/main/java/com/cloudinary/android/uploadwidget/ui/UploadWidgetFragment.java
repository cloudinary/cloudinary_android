package com.cloudinary.android.uploadwidget.ui;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cloudinary.android.ui.R;
import com.cloudinary.android.uploadwidget.UploadWidget;
import com.cloudinary.android.uploadwidget.model.BitmapManager;
import com.cloudinary.android.uploadwidget.model.CropRotateResult;
import com.cloudinary.android.uploadwidget.utils.MediaType;
import com.cloudinary.android.uploadwidget.utils.UriUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Previews media files, and optionally edits them, before uploading.
 */
public class UploadWidgetFragment extends Fragment implements CropRotateFragment.Callback {


    private static final String IMAGES_URIS_LIST_ARG = "images_uris_list_arg";
    private FloatingActionButton uploadFab;
    private ViewPager mediaViewPager;
    private MediaPagerAdapter mediaPagerAdapter;
    private RecyclerView thumbnailsRecyclerView;
    private ThumbnailsAdapter thumbnailsAdapter;
    private ArrayList<Uri> uris;
    private Map<Uri, UploadWidget.Result> uriResults;

    public UploadWidgetFragment() {
        // Required empty public constructor
    }

    public static UploadWidgetFragment newInstance(ArrayList<Uri> imagesUris) {
        UploadWidgetFragment fragment = new UploadWidgetFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(IMAGES_URIS_LIST_ARG, imagesUris);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle arguments = getArguments();
        if (arguments != null) {
            uris = arguments.getParcelableArrayList(IMAGES_URIS_LIST_ARG);
        }
        uriResults = new HashMap<>(uris.size());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upload_widget, container, false);

        mediaViewPager = view.findViewById(R.id.imagesViewPager);
        mediaPagerAdapter = new MediaPagerAdapter(uris, mediaViewPager);
        mediaViewPager.setAdapter(mediaPagerAdapter);
        mediaViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                thumbnailsAdapter.setSelectedThumbnail(position);
                thumbnailsRecyclerView.scrollToPosition(position);
                super.onPageSelected(position);
            }
        });

        uploadFab = view.findViewById(R.id.uploadFab);
        uploadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof UploadWidgetListener) {
                    ((UploadWidgetListener) getActivity()).onConfirm(getResults());
                }
            }
        });

        thumbnailsRecyclerView = view.findViewById(R.id.thumbnailsRecyclerView);
        if (uris.size() > 1) {
            thumbnailsAdapter = new ThumbnailsAdapter(uris, new ThumbnailsAdapter.Callback() {
                @Override
                public void onThumbnailClicked(Uri uri) {
                    mediaViewPager.setCurrentItem(mediaPagerAdapter.getMediaPosition(uri), true);
                }
            });
            thumbnailsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            thumbnailsRecyclerView.setAdapter(thumbnailsAdapter);
        } else {
            thumbnailsRecyclerView.setVisibility(View.INVISIBLE);
        }

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
        final MenuItem cropItem = menu.findItem(R.id.crop_action);
        final View cropActionView = cropItem.getActionView();

        cropActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = uris.get(mediaViewPager.getCurrentItem());
                CropRotateFragment cropRotateFragment = CropRotateFragment.newInstance(uri, UploadWidgetFragment.this);

                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, cropRotateFragment, null)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCropRotateFinish(final Uri uri, final CropRotateResult result, Bitmap resultBitmap) {
        UploadWidget.Result uwResult = uriResults.get(uri);
        if (uwResult == null) {
            uwResult = new UploadWidget.Result(uri);
        }
        uwResult.rotationAngle = result.getRotationAngle();
        uwResult.cropPoints = result.getCropPoints();
        uriResults.put(uri, uwResult);

        MediaType mediaType = UriUtils.getMediaType(getContext(), uri);
        if (mediaType == MediaType.IMAGE) {
            BitmapManager.get().save(getContext(), resultBitmap, new BitmapManager.SaveCallback() {
                @Override
                public void onSuccess(Uri resultUri) {
                    mediaPagerAdapter.updateMediaResult(uri, resultUri);
                }

                @Override
                public void onFailure() { }
            });
        }
    }

    @Override
    public void onCropRotateCancel(Uri uri) {
        UploadWidget.Result result = uriResults.get(uri);
        if (result != null) {
            result.rotationAngle = 0;
            result.cropPoints = null;
        }

        mediaPagerAdapter.resetMediaResult(uri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<UploadWidget.Result> getResults() {
        for (Uri uri : uris) {
            if (!uriResults.containsKey(uri)) {
                uriResults.put(uri, new UploadWidget.Result(uri));
            }
        }

        ArrayList<UploadWidget.Result> results = new ArrayList<>(uriResults.size());
        results.addAll(uriResults.values());

        return results;
    }

    /**
     * Listener for the Upload Widget.
     */
    public interface UploadWidgetListener {

        /**
         * Called when the upload widget results are confirmed.
         *
         * @param results Upload widget's results.
         */
        void onConfirm(ArrayList<UploadWidget.Result> results);
    }

}
