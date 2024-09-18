package com.cloudinary.sample.main.upload;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cloudinary.sample.databinding.UploadChoiceFragmentBinding;
import com.cloudinary.sample.databinding.VideoFragmentBinding;
import com.cloudinary.sample.main.widgets.upload_widget.UploadWidgetFragment;


public class UploadChoiceFragment extends Fragment {
    UploadChoiceFragmentBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = UploadChoiceFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String cloudName = getCloudName();
        selectView(cloudName);
    }

    private String getCloudName() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("cloud_name", null); // "" is the default value if cloud_name is not found
    }

    private void selectView(String cloudName) {
        if (cloudName == null) {
            openNoCloudFragment();
        } else {
            openUploadWidgetFragment();
        }

    }

    private void openUploadWidgetFragment() {
        Fragment fragment = new UploadWidgetFragment();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(binding.uploadChoiceFragmentContainer.getId(), fragment);
        fragmentTransaction.commit();
    }

    private void openNoCloudFragment() {
        Intent intent = new Intent(getActivity(), NoCloudActivity.class);
        startActivity(intent);
    }
}