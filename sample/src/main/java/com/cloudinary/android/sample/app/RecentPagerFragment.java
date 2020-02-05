package com.cloudinary.android.sample.app;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.android.sample.persist.ResourceRepo;

import java.util.List;

public class RecentPagerFragment extends UploadedPageFragment {

    public static Fragment newInstance() {
        return new RecentPagerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        TextView labelTextView = (TextView) view.findViewById(R.id.emptyViewLabel);
        Button emptyButton = (Button) view.findViewById(R.id.emptyViewButton);

        emptyButton.setVisibility(View.VISIBLE);
        emptyButton.setText(R.string.uploaded_button_label);
        labelTextView.setVisibility(View.VISIBLE);
        labelTextView.setText(R.string.recent_label);

        return view;
    }

    protected boolean isRecent() {
        return true;
    }

    @Override
    protected List<Resource> getData() {
        return ResourceRepo.getInstance().listRecent();
    }
}
