package com.cloudinary.sample.main.upload.single_upload;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.preprocess.BitmapDecoder;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.DimensionsValidator;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.preprocess.Limit;
import com.cloudinary.sample.CloudinarySampleApplication;
import com.cloudinary.sample.R;
import com.cloudinary.sample.databinding.SingleUploadFragmentBinding;
import com.cloudinary.sample.helpers.Utils;
import com.cloudinary.sample.local_storage.AssetModelEntity;
import com.cloudinary.sample.local_storage.AssetViewModel;
import java.util.Map;

public class SingleUploadFragment extends Fragment {

    private AssetViewModel viewModel;

    UploadRecycleAdapter adapter;

    UploadViewType type = UploadViewType.Upload;

    private static final int PICK_IMAGE_REQUEST = 14425;
    SingleUploadFragmentBinding binding;

    ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = SingleUploadFragmentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(AssetViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setGallery();
        setRecyclerView();
        observeData();
        setOpenGalleryButton();
        checkPermissions();
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = binding.singleUploadRecycler;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3); // 3 items per row
        recyclerView.setLayoutManager(layoutManager);
        adapter = new UploadRecycleAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new UploadCellItemDecoration(getActivity(), R.dimen.item_spacing));
    }
    private void observeData() {
        viewModel.getAssetModels().observe(this, assetModels -> {
            if (assetModels != null) {
                adapter.setAssetModels(assetModels);
            }
        });
    }

    private void setGallery() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            if(result.getData() != null) {
                                Uri imageUri = result.getData().getData();
                                if(type == UploadViewType.Upload) {
                                    uploadImage(imageUri);
                                }
                                if(type == UploadViewType.PreProcess) {
                                    preProcessImage(imageUri);
                                }
                            }
                        }
                    }
                });
        requestPermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            // Handle permission requests results
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                String permission = entry.getKey();
                Boolean isGranted = entry.getValue();
                if (isGranted) {
                    // Permission is granted
                    Log.d("Permissions", "Permission granted: " + permission);
                } else {
                    // Permission is denied
                    Log.d("Permissions", "Permission denied: " + permission);
                }
            }
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            });
        } else {
            requestPermissionsLauncher.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }
    }

    private void setOpenGalleryButton() {
        binding.singleUploadOpenGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                galleryLauncher.launch(intent);
            }
        });
    }

    private void preProcessImage(Uri imageUri) {
        MediaManager.get().upload(imageUri)
                .unsigned(Utils.UPLOAD_PRESET)
                .preprocess(new ImagePreprocessChain()
                        .loadWith(new BitmapDecoder(1000, 1000))
                        .addStep(new Limit(1000, 1000))
                        .addStep(new DimensionsValidator(10,10,1000,1000))
                        .saveWith(new BitmapEncoder(BitmapEncoder.Format.JPEG, 80)))
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Course", "Upload starting");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        Log.d("Course", "Upload progress");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        saveToLocalStorage(resultData);
                        observeData();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.d("Course", "Upload error");
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {

                    }
                })
                .dispatch(getActivity());
    }

    private void uploadImage(Uri imageUri) {
        showLoadingView();
        MediaManager.get().upload(imageUri).unsigned(Utils.UPLOAD_PRESET).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d(CloudinarySampleApplication.APP_NAME, "Upload started");
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {

            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                Log.d(CloudinarySampleApplication.APP_NAME, "Upload success");
                saveToLocalStorage(resultData);
                observeData();
                hideLoadingView();
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Log.d(CloudinarySampleApplication.APP_NAME, "Upload error");
                hideLoadingView();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                Log.d(CloudinarySampleApplication.APP_NAME, "Upload rescheduled");
            }
        }).dispatch();
    }

    private void showLoadingView() {
        binding.singleUploadProgressBarContainer.setVisibility(View.VISIBLE);
        binding.singleUploadProgressBar.animate();
    }

    private void hideLoadingView() {
        binding.singleUploadProgressBarContainer.setVisibility(View.GONE);
    }

    private void saveToLocalStorage(Map resultData) {
        String publicId = (String) resultData.get("public_id");
        String deliveryType = (String) resultData.get("type");
        String assetType = (String) resultData.get("resource_type");
        String transformation = (String) resultData.get("transformation");
        String url = (String) resultData.get("url");
        AssetModelEntity model = new AssetModelEntity(publicId, deliveryType, assetType, transformation, url);

        viewModel.insert(model);

    }

    public void setType(UploadViewType type) {
        this.type = type;
    }
}
