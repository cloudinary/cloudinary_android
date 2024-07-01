package com.cloudinary.sample.main.delivery.usecases.inner;

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
import com.cloudinary.sample.databinding.UsecasesFragmentBinding;
import com.cloudinary.sample.main.delivery.usecases.OnUseCaseItemSelectedListener;
import com.cloudinary.sample.main.delivery.usecases.inner.adapt.AdaptToScreenSizeFragment;
import com.cloudinary.sample.main.delivery.usecases.inner.conditional.ConditionalProductBadgingFragment;
import com.cloudinary.sample.main.delivery.usecases.inner.normalize.NormalizeAssetSizingFragment;

public class UseCasesFragment extends Fragment implements OnUseCaseItemSelectedListener {

    UsecasesFragmentBinding binding;

    UseCasesType type = UseCasesType.NormalizeAssetSizing;
    private int selectedItemPosition;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = UsecasesFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFragment(type);
        setRecycleView();
    }

    private void setRecycleView() {
        RecyclerView recyclerView = binding.usecasesInnerRecyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        UseCasesAdapter adapter = new UseCasesAdapter(getActivity(), this);
        adapter.setSelectedItemPosition(selectedItemPosition);
        recyclerView.setAdapter(adapter);
    }

    private void setFragment(UseCasesType type) {
        Fragment fragment = null;
        switch (type) {
            case NormalizeAssetSizing:
                fragment = new NormalizeAssetSizingFragment();
                break;
            case ConditionalProductBadging:
                fragment = new ConditionalProductBadgingFragment();
                break;
            case AdaptToScreenSize:
                fragment = new AdaptToScreenSizeFragment();
                ((AdaptToScreenSizeFragment)fragment).setUrl("https://res.cloudinary.com/mobiledemoapp/video/upload/DevApp_Adapt_Video_02_diett8");
                break;
            case AIGeneratedBackgrounds:
                fragment = new AdaptToScreenSizeFragment();
                ((AdaptToScreenSizeFragment)fragment).setUrl("https://res.cloudinary.com/mobiledemoapp/video/upload/DevApp_Generative_Fill_01_fneqxw");
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(binding.usecasesFragmentContainer.getId(), fragment);
            fragmentTransaction.commit();
        }
    }

    public void setPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
        switch(selectedItemPosition) {
            case 0:
                type = UseCasesType.NormalizeAssetSizing;
                break;
            case 1:
                type = UseCasesType.ConditionalProductBadging;
                break;
            case 2:
                type = UseCasesType.AdaptToScreenSize;
                break;
            case 3:
                type = UseCasesType.AIGeneratedBackgrounds;
                break;
            default:
                type = UseCasesType.NormalizeAssetSizing;
                break;

        }
    }

    @Override
    public void onUseCaseItemSelected(int position) {
        this.selectedItemPosition = position;
        switch (selectedItemPosition) {
            case 0:
                setFragment(UseCasesType.NormalizeAssetSizing);
                break;
            case 1:
                setFragment(UseCasesType.ConditionalProductBadging);
                break;
            case 2:
                setFragment(UseCasesType.AdaptToScreenSize);
                break;
            case 3:
                setFragment(UseCasesType.AIGeneratedBackgrounds);
                break;
            default:
                setFragment(UseCasesType.NormalizeAssetSizing);
                break;

        }
    }
}