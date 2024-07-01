package com.cloudinary.sample.main.upload;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cloudinary.sample.databinding.UploadChoiceFragmentBinding;
import com.cloudinary.sample.databinding.UploadFragmentBinding;
import com.cloudinary.sample.main.delivery.base.BaseActivity;
import com.cloudinary.sample.main.delivery.base.BaseActivityType;

public class UploadFragment extends Fragment {

    UploadFragmentBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = UploadFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setViews();
    }

    private void setViews() {
        binding.uploadContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BaseActivity.class);
                intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TYPE, BaseActivityType.Upload.ordinal());
                startActivity(intent);
            }
        });
        binding.preProcessContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BaseActivity.class);
                intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TYPE, BaseActivityType.PreProcess.ordinal());
                startActivity(intent);
            }
        });
        binding.fetchContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BaseActivity.class);
                intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TYPE, BaseActivityType.FetchUpload.ordinal());
                startActivity(intent);
            }
        });
    }
}
