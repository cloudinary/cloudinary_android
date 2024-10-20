package com.cloudinary.sample.main.delivery.transform.inner.localization;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cloudinary.sample.databinding.LocalizationFragmentBinding;
import com.cloudinary.sample.helpers.views.RevealImageView;


public class LocalizationFragment extends Fragment {

    LocalizationFragmentBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = LocalizationFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRevealImageView();
    }

    private void setRevealImageView() {
        RevealImageView revealImageView = binding.localizationRevealImageview;
//        String url = MediaManager.get().url().transformation(new Transformation().overlay("text:Arial_72:NEW%2520COLLECTION").color("black").flags("layer_apply").gravity("center")).generate("Demo%20app%20content/layers-fashion-2_1_xsfbvm");
        Glide.with(revealImageView).asBitmap().load("https://res.cloudinary.com/mobiledemoapp/image/upload/co_black,fl_layer_apply,g_center,l_text:Arial_72:NEW%2520COLLECTION/v1/Demo%20app%20content/layers-fashion-2_1_xsfbvm?_a=DAFAMiAiAiA0").into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                revealImageView.setLeftImage(resource);
            }
        });
//        String url = MediaManager.get().url().generate("Demo%20app%20content/layers-fashion-2_1_xsfbvm");
        Glide.with(revealImageView).asBitmap().load("https://res.cloudinary.com/mobiledemoapp/image/upload/v1/Demo%20app%20content/layers-fashion-2_1_xsfbvm?_a=DAFAMiAiAiA0").into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                revealImageView.setRightImage(resource);
            }
        });
    }
}
