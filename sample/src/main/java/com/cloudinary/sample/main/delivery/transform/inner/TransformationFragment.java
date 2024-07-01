package com.cloudinary.sample.main.delivery.transform.inner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.sample.databinding.TransformationFragmentBinding;
import com.cloudinary.sample.main.delivery.background.BackgroundNormalizingFragment;
import com.cloudinary.sample.main.delivery.transform.inner.color.ColorAlternationFragment;
import com.cloudinary.sample.main.delivery.transform.inner.localization.LocalizationFragment;
import com.cloudinary.sample.main.delivery.transform.OnTransformationItemSelectedListener;
import com.cloudinary.sample.main.delivery.transform.inner.smart_cropping.SmartCroppingFragment;

public class TransformationFragment extends Fragment implements OnTransformationItemSelectedListener {

    TransformationFragmentBinding binding;

    TransformationType type = TransformationType.SmartCropping;
    private int selectedItemPosition;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = TransformationFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFragment(type);
        setRecycleView();
    }

    private void setRecycleView() {
        RecyclerView recyclerView = binding.transformationInnerRecyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        TransformationAdapter adapter = new TransformationAdapter(getActivity(), this);
        adapter.setSelectedItemPosition(selectedItemPosition);
        recyclerView.setAdapter(adapter);
    }

    private void setFragment(TransformationType type) {
        Fragment fragment = null;
        switch (type) {
            case SmartCropping:
                fragment = new SmartCroppingFragment();
                break;
            case LocalizationAndBranding:
                fragment = new LocalizationFragment();
                break;
            case BackgroundNormalizing:
                fragment = new BackgroundNormalizingFragment();
                break;
            case ColorAlternation:
                fragment = new ColorAlternationFragment();
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(binding.transformationFragmentContainer.getId(), fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onTransformationItemSelected(int position) {
        switch(position) {
            case 0:
                setFragment(TransformationType.SmartCropping);
                break;
            case 1:
                setFragment(TransformationType.LocalizationAndBranding);
                break;
            case 2:
                setFragment(TransformationType.BackgroundNormalizing);
                break;
            case 3:
                setFragment(TransformationType.ColorAlternation);
                break;
            default:
                setFragment(TransformationType.SmartCropping);
                break;

        }
    }

    public void setPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
        switch(selectedItemPosition) {
            case 0:
                type = TransformationType.SmartCropping;
                break;
            case 1:
                type = TransformationType.LocalizationAndBranding;
                break;
            case 2:
                type = TransformationType.BackgroundNormalizing;
                break;
            case 3:
                type = TransformationType.ColorAlternation;
                break;
            default:
                type = TransformationType.SmartCropping;
                break;

        }
    }
}

