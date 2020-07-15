package com.cloudinary.android.sample.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.android.sample.persist.ResourceRepo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class QueuedPagerFragment extends AbstractPagerFragment {

    private List<Resource.UploadStatus> statuses = Arrays.asList(Resource.UploadStatus.QUEUED, Resource.UploadStatus.UPLOADING);

    public static Fragment newInstance() {
        return new QueuedPagerFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        TextView labelTextView = (TextView) view.findViewById(R.id.emptyViewLabel);
        Button emptyButton = (Button) view.findViewById(R.id.emptyViewButton);

        emptyButton.setVisibility(View.GONE);
        labelTextView.setVisibility(View.VISIBLE);
        labelTextView.setText(R.string.queued_label);

        return view;
    }

    @Override
    protected ResourcesAdapter getAdapter(int thumbSize) {
        return new ResourcesAdapter(new ArrayList<Resource>(), thumbSize, statuses, new ResourcesAdapter.ImageClickedListener() {
            @Override
            public void onImageClicked(Resource resource) {

            }

            @Override
            public void onDeleteClicked(Resource resource, Boolean recent) {

            }

            @Override
            public void onRetryClicked(Resource resource) {

            }

            @Override
            public void onCancelClicked(Resource resource) {
                ((ResourcesAdapter.ImageClickedListener) getActivity()).onCancelClicked(resource);
            }
        });
    }

    @Override
    protected int getSpan() {
        return 2;
    }

    protected List<Resource> getData() {
        return ResourceRepo.getInstance().list(statuses);
    }
}
