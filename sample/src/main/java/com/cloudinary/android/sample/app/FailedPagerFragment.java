package com.cloudinary.android.sample.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.android.sample.persist.ResourceRepo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FailedPagerFragment extends AbstractPagerFragment {
    private List<Resource.UploadStatus> statuses = Arrays.asList(Resource.UploadStatus.FAILED, Resource.UploadStatus.RESCHEDULED);

    public static Fragment newInstance() {
        return new FailedPagerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        TextView labelTextView = (TextView) view.findViewById(R.id.emptyViewLabel);
        Button emptyButton = (Button) view.findViewById(R.id.emptyViewButton);

        emptyButton.setVisibility(View.GONE);
        labelTextView.setVisibility(View.VISIBLE);
        labelTextView.setText(R.string.failed_label);

        return view;
    }

    @Override
    protected ResourcesAdapter getAdapter(int thumbSize) {
        return new ResourcesAdapter(getActivity(), new ArrayList<Resource>(), thumbSize, statuses, null);
    }

    protected List<Resource> getData() {
        return ResourceRepo.getInstance().list(statuses);
    }
}
