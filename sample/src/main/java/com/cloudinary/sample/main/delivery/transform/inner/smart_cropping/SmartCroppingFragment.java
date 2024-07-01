package com.cloudinary.sample.main.delivery.transform.inner.smart_cropping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cloudinary.sample.R;
import com.cloudinary.sample.databinding.SmartCroppingFragmentBinding;

public class SmartCroppingFragment extends Fragment {
    SmartCroppingFragmentBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = SmartCroppingFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setImages();
    }

    private void setImages() {
        ImageView main = binding.smartCroppingMain;
        main.setImageResource(R.drawable.ski);

    }
}
