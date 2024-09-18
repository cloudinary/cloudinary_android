package com.cloudinary.sample.main.delivery.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cloudinary.sample.R;
import com.cloudinary.sample.databinding.ActivityBaseBinding;
import com.cloudinary.sample.databinding.ActivityMainBinding;
import com.cloudinary.sample.databinding.HeadingBinding;
import com.cloudinary.sample.helpers.StringHelper;
import com.cloudinary.sample.main.delivery.optimization.OptimizationFragment;
import com.cloudinary.sample.main.delivery.optimization.OptimizationType;
import com.cloudinary.sample.main.delivery.transform.inner.TransformationFragment;
import com.cloudinary.sample.main.delivery.usecases.inner.UseCasesFragment;
import com.cloudinary.sample.main.upload.UploadChoiceFragment;
import com.cloudinary.sample.main.upload.single_upload.SingleUploadFragment;
import com.cloudinary.sample.main.upload.single_upload.UploadViewType;
import com.cloudinary.sample.main.widgets.image_widget.ImageWidgetFragment;
import com.cloudinary.sample.main.widgets.upload_widget.UploadWidgetFragment;

import java.util.List;

public class BaseActivity extends AppCompatActivity {

    public static final String EXTRA_ACTIVITY_TYPE = "activity_type";
    public static final String EXTRA_ITEM_SELECTED_POSITION = "selected_item_position";
    ActivityBaseBinding binding;
    HeadingBinding headingBinding;

    BaseActivityType type = BaseActivityType.Optimization;
    private int selectedItemPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int typeOrdinal = getIntent().getIntExtra(EXTRA_ACTIVITY_TYPE, BaseActivityType.Optimization.ordinal());
        type = BaseActivityType.values()[typeOrdinal];

        selectedItemPosition = getIntent().getIntExtra(EXTRA_ITEM_SELECTED_POSITION, 0);

        binding = ActivityBaseBinding.inflate(getLayoutInflater());
        headingBinding = binding.baseHeading;
        setContentView(binding.getRoot());
        setHeadingTitle();
        setBackButton();
        setFragmentView();
    }

    private void setFragmentView() {
        switch(type) {
            case Optimization:
                setFragment(new OptimizationFragment());
                break;
            case Transformation:
                TransformationFragment transformationFragment = new TransformationFragment();
                transformationFragment.setPosition(selectedItemPosition);
                setFragment(transformationFragment);
                break;
            case UseCases:
                UseCasesFragment useCasesFragment = new UseCasesFragment();
                useCasesFragment.setPosition(selectedItemPosition);
                setFragment(useCasesFragment);
                break;
            case Upload:
                setFragment(new SingleUploadFragment());
                break;
            case PreProcess:
                SingleUploadFragment singleUploadFragment = new SingleUploadFragment();
                singleUploadFragment.setType(UploadViewType.PreProcess);
                setFragment(singleUploadFragment);
                break;
            case FetchUpload:
                OptimizationFragment optimizationFragment = new OptimizationFragment();
                optimizationFragment.setType(OptimizationType.FetchUplaod);
                optimizationFragment.setPublicId("https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Leonardo_da_Vinci_%281452-1519%29_-_The_Last_Supper_%281495-1498%29.jpg/1600px-Leonardo_da_Vinci_%281452-1519%29_-_The_Last_Supper_%281495-1498%29.jpg?20150402075721");
                setFragment(optimizationFragment);
                break;
            case ImageWidget:
                setFragment(new ImageWidgetFragment());
                break;
            case UploadWidget:
                setFragment(new UploadChoiceFragment());
                break;
        }
    }

    private void setHeadingTitle() {
        TextView headingTitle = headingBinding.headerTitle;
        switch (type) {
            case Optimization:
                headingTitle.setText(StringHelper.captialLetter(getString(R.string.optimization)));
                break;
            case Transformation:
                headingTitle.setText(StringHelper.captialLetter(getString(R.string.transformation)));
                break;
            case UseCases:
                headingTitle.setText(getString(R.string.usecases));
                break;
            case Upload:
                headingTitle.setText(getString(R.string.upload));
                break;
            case PreProcess:
                headingTitle.setText(getString(R.string.pre_process));
                break;
            case FetchUpload:
                headingTitle.setText(getString(R.string.fetch_upload));
                break;
            case ImageWidget:
                headingTitle.setText(getString(R.string.image_widget));
                break;
            case UploadWidget:
                headingTitle.setText(getString(R.string.upload_widget));
                break;
        }
    }

    private void setBackButton() {
        ImageButton button = headingBinding.headerBackButton;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(binding.baseFragemntContainer.getId(), fragment);
        fragmentTransaction.commit();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UploadWidgetFragment.UPLOAD_WIDGET_REQUEST_CODE) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            List<Fragment> fragments = fragmentManager.getFragments();
            if (fragments.get(1) instanceof UploadWidgetFragment) {
                assert data != null;
                ((UploadWidgetFragment) fragments.get(1)).handleResultWidgetResult(data);
            }
        }
    }
}

