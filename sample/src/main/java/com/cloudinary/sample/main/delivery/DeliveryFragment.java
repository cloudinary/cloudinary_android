package com.cloudinary.sample.main.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.sample.R;
import com.cloudinary.sample.databinding.DeliveryFragementBinding;
import com.cloudinary.sample.main.delivery.base.BaseActivity;
import com.cloudinary.sample.main.delivery.base.BaseActivityType;
import com.cloudinary.sample.main.delivery.transform.DeliveryTransformAdapter;
import com.cloudinary.sample.main.delivery.transform.OnTransformationItemSelectedListener;
import com.cloudinary.sample.main.delivery.usecases.DeliveryUsecasesAdapter;
import com.cloudinary.sample.main.delivery.usecases.OnUseCaseItemSelectedListener;
import com.cloudinary.sample.main.delivery.usecases.RecyclerViewItemDecoration;

public class DeliveryFragment extends Fragment implements OnTransformationItemSelectedListener, OnUseCaseItemSelectedListener {

    DeliveryFragementBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = DeliveryFragementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTransformationRecyclerView();
        setUsecasesRecyclerView();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        ConstraintLayout optimizationLayout = binding.deliveryOptimizationContainer;
        optimizationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BaseActivity.class);
                intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TYPE, BaseActivityType.Optimization.ordinal());
                startActivity(intent);
            }
        });
    }

    private void setTransformationRecyclerView() {
        RecyclerView recyclerView = binding.deliveryTransformationRecyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        DeliveryTransformAdapter adapter = new DeliveryTransformAdapter(getActivity(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setUsecasesRecyclerView() {
        RecyclerView recyclerView = binding.deliveryUsecasesRecyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        DeliveryUsecasesAdapter adapter = new DeliveryUsecasesAdapter(getActivity(), this);
        recyclerView.setAdapter(adapter);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing); // Adjust the spacing as needed
        recyclerView.addItemDecoration(new RecyclerViewItemDecoration(getActivity(), spacingInPixels));
    }

    @Override
    public void onTransformationItemSelected(int position) {
        Intent intent = new Intent(getActivity(), BaseActivity.class);
        intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TYPE, BaseActivityType.Transformation.ordinal());
        intent.putExtra(BaseActivity.EXTRA_ITEM_SELECTED_POSITION, position);
        startActivity(intent);
    }

    @Override
    public void onUseCaseItemSelected(int position) {
        Intent intent = new Intent(getActivity(), BaseActivity.class);
        intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TYPE, BaseActivityType.UseCases.ordinal());
        intent.putExtra(BaseActivity.EXTRA_ITEM_SELECTED_POSITION, position);
        startActivity(intent);
    }
}
