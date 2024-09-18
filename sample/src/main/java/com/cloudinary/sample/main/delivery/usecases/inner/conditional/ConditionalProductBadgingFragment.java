package com.cloudinary.sample.main.delivery.usecases.inner.conditional;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.sample.databinding.ConditionalProductBadgingBinding;

public class ConditionalProductBadgingFragment extends Fragment {
    ConditionalProductBadgingBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = ConditionalProductBadgingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setImages();
    }

    private void setImages() {
        ImageView topTopLeft = binding.imageviewTopLeft;
        ImageView topTopRight = binding.imageviewTopRight;
        ImageView bottom = binding.imageviewBottom;
//        String url = MediaManager.get().url().generate("Group_15_jda5ms");
        Glide.with(topTopLeft).load("https://res.cloudinary.com/mobiledemoapp/image/upload/Group_15_jda5ms?_a=DAFAMiAiAiA0").into(topTopLeft);
//        String url = MediaManager.get().url().generate("tshirt4_1_si0swc");
        Glide.with(topTopRight).load("https://res.cloudinary.com/mobiledemoapp/image/upload/tshirt4_1_si0swc?_a=DAFAMiAiAiA0").into(topTopRight);
//        String url = MediaManager.get().url().transformation(new Transformation()
//                .overlay("Group_15_jda5ms")
//                .gravity("north_west")
//                .width(0.4)
//                .x(10)
//                .y(10)).generate("tshirt4_1_si0swc");
        Glide.with(bottom).load("https://res.cloudinary.com/mobiledemoapp/image/upload/g_north_west,l_Group_15_jda5ms,w_0.4,x_10,y_10/tshirt4_1_si0swc?_a=DAFAMiAiAiA0").into(bottom);
    }
}
