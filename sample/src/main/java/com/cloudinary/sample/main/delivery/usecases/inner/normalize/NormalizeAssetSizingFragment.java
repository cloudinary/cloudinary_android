package com.cloudinary.sample.main.delivery.usecases.inner.normalize;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.sample.databinding.NormalizeAssetSizingBinding;

public class NormalizeAssetSizingFragment extends Fragment {
    NormalizeAssetSizingBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = NormalizeAssetSizingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setImages();
    }

    private void setImages() {
        ImageView topTopLeft = binding.topImageviewTopLeft;
        ImageView topTopRight = binding.topImageviewTopRight;
        ImageView topBottomRight = binding.topImageviewBottomRight;
        ImageView bottomLeft = binding.bottomImageviewLeft;
        ImageView bottomMiddle = binding.bottomImageviewCenter;
        ImageView bottomRight = binding.bottomImageviewRight;
//        String url = MediaManager.get().url().generate("pexels-aditya-aiyar-1407354_tiw4bv");
        Glide.with(topTopLeft).load("https://res.cloudinary.com/mobiledemoapp/image/upload/pexels-aditya-aiyar-1407354_tiw4bv?_a=DAFAMiAiAiA0").into(topTopLeft);
//        String url = MediaManager.get().url().generate("pexels-mnz-1670766_n9hfoi");
        Glide.with(topTopRight).load("https://res.cloudinary.com/mobiledemoapp/image/upload/pexels-mnz-1670766_n9hfoi?_a=DAFAMiAiAiA0").into(topTopRight);
//        String url = MediaManager.get().url().generate("pexels-wendy-wei-12511453_b4shho");
        Glide.with(topBottomRight).load("https://res.cloudinary.com/mobiledemoapp/image/upload/pexels-wendy-wei-12511453_b4shho?_a=DAFAMiAiAiA0").into(topBottomRight);
//        String url = MediaManager.get().url().generate("Rectangle_1434_fcnobi");
        Glide.with(bottomLeft).load("https://res.cloudinary.com/mobiledemoapp/image/upload/Rectangle_1434_fcnobi?_a=DAFAMiAiAiA0").into(bottomLeft);
//        String url = MediaManager.get().url().generate("Rectangle_1435_mwtszu");
        Glide.with(bottomMiddle).load("https://res.cloudinary.com/mobiledemoapp/image/upload/Rectangle_1435_mwtszu?_a=DAFAMiAiAiA0").into(bottomMiddle);
//        String url = MediaManager.get().url().generate("Rectangle_1436_kdsfld");
        Glide.with(bottomRight).load("https://res.cloudinary.com/mobiledemoapp/image/upload/Rectangle_1436_kdsfld?_a=DAFAMiAiAiA0").into(bottomRight);
    }
}
