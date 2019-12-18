package com.cloudinary.android.uploadwidget.ui;


import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cloudinary.android.R;
import com.cloudinary.android.uploadwidget.UploadWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class UploadWidgetFragment extends Fragment implements CropRotateFragment.Callback {


    private static final String IMAGES_URIS_LIST_ARG = "images_uris_list_arg";
    private FloatingActionButton uploadFab;
    private ViewPager imagesViewPager;
    private ImagePagerAdapter imagesPagerAdapter;
    private RecyclerView thumbnailsRecyclerView;
    private ThumbnailsAdapter thumbnailsAdapter;
    private ArrayList<Uri> imagesUris;
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
            imagesUris = arguments.getParcelableArrayList(IMAGES_URIS_LIST_ARG);
        }
        uriResults = new HashMap<>(imagesUris.size());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upload_widget, container, false);

        imagesViewPager = view.findViewById(R.id.imagesViewPager);
        imagesPagerAdapter = new ImagePagerAdapter(imagesUris);
        imagesViewPager.setAdapter(imagesPagerAdapter);
        imagesViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
        if (imagesUris.size() > 1) {
            thumbnailsAdapter = new ThumbnailsAdapter(imagesUris, new ThumbnailsAdapter.Callback() {
                @Override
                public void onThumbnailClicked(Uri uri) {
                    imagesViewPager.setCurrentItem(imagesPagerAdapter.getImageIndex(uri), true);
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
                Uri imageUri = imagesUris.get(imagesViewPager.getCurrentItem());
                CropRotateFragment cropRotateFragment = CropRotateFragment.newInstance(imageUri, UploadWidgetFragment.this);

                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, cropRotateFragment, "image_fragment_tag")
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCropRotateFinish(final Uri imageUri, final CropRotateResult result, Bitmap resultBitmap) {
        UploadWidget.Result uwResult = uriResults.get(imageUri);
        if (uwResult == null) {
            uwResult = new UploadWidget.Result(imageUri);
        }
        uwResult.rotationAngle = result.getRotationAngle();
        uwResult.cropPoints = result.getCropPoints();
        uriResults.put(imageUri, uwResult);

        BitmapManager.get().save(getContext(), resultBitmap, new BitmapManager.SaveResultCallback() {
            @Override
            public void onSuccess(Uri resultUri) {
                imagesPagerAdapter.updateResultUri(imageUri, resultUri);
            }

            @Override
            public void onFailure() {
            }
        });

    }

    @Override
    public void onCropRotateCancel(Uri imageUri) {
        resetUriResult(imageUri);
        imagesPagerAdapter.resetResultUri(imageUri);
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
        for (Uri uri : imagesUris) {
            if (!uriResults.containsKey(uri)) {
                resetUriResult(uri);
            }
        }

        ArrayList<UploadWidget.Result> results = new ArrayList<>(uriResults.size());
        results.addAll(uriResults.values());

        return results;
    }

    private void resetUriResult(Uri uri) {
        uriResults.put(uri, new UploadWidget.Result(uri));
    }

    /**
     * Listener for the Upload Widget.
     */
    public interface UploadWidgetListener {

        /**
         * Called when the upload widget (optional) edits are confirmed.
         *
         * @param results Upload widget's results.
         */
        void onConfirm(ArrayList<UploadWidget.Result> results);
    }

}
