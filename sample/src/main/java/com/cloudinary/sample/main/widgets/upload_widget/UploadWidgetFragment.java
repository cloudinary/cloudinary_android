package com.cloudinary.sample.main.widgets.upload_widget;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.uploadwidget.UploadWidget;
import com.cloudinary.sample.databinding.UploadChoiceFragmentBinding;
import com.cloudinary.sample.databinding.UploadWidgetFragmentBinding;
import com.cloudinary.sample.helpers.Utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UploadWidgetFragment extends Fragment {

    public static final int UPLOAD_WIDGET_REQUEST_CODE = 7731;
    UploadWidgetFragmentBinding binding;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = UploadWidgetFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setOpenUploadWidgetButton();
    }

    private void setOpenUploadWidgetButton() {
        binding.openUploadWidgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.uploadWidgetProgressbar.setVisibility(View.VISIBLE);
                UploadWidget.startActivity(getActivity(), UPLOAD_WIDGET_REQUEST_CODE);
            }
        });
    }

    public void handleResultWidgetResult(Intent data) {
        List<UploadWidget.Result> results = data.getParcelableArrayListExtra(UploadWidget.RESULT_EXTRA);
        UploadRequest request = UploadWidget.preprocessResult(getActivity(), Objects.requireNonNull(results).get(0));
        request.unsigned(Utils.UPLOAD_PRESET).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {

            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
            }

            @SuppressLint("UseRequireInsteadOfGet")
            @Override
            public void onSuccess(String requestId, Map resultData) {
                ImageView uploadWidgetImageview = binding.uploadWidgetImageview;
                String secureUrl = (String) resultData.get("secure_url");
                Glide.with(Objects.requireNonNull(getActivity())).load(secureUrl).into(uploadWidgetImageview);
                binding.uploadWidgetProgressbar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {

            }
        }).dispatch(getActivity());
    }
}
