package com.cloudinary.sample.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.cloudinary.android.MediaManager;
import com.cloudinary.sample.R;
import com.cloudinary.sample.helpers.CloudinaryHelper;
import com.cloudinary.sample.main.delivery.DeliveryFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cloudinary.sample.databinding.ActivityMainBinding;
import com.cloudinary.sample.main.upload.NoCloudActivity;
import com.cloudinary.sample.main.upload.UploadFragment;
import com.cloudinary.sample.main.video.VideoFragment;
import com.cloudinary.sample.main.widgets.WidgetsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import android.view.MenuItem;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        DeliveryFragment deliveryFragment = new DeliveryFragment();
        setFragment(deliveryFragment);
        setBottomNavigationView();
        checkForActiveCloud();
    }

    private void checkForActiveCloud() {
        String cloudName = getCloudName();
        if (cloudName == null) {
            Intent intent = new Intent(this, NoCloudActivity.class);
            startActivity(intent);
        } else {
            if(MediaManager.get() == null) {
                CloudinaryHelper.setMediaManager(getBaseContext(), cloudName);
            }
        }
    }

    private String getCloudName() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("cloud_name", null); // "" is the default value if cloud_name is not found
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void setBottomNavigationView() {
        BottomNavigationView bottomNavigationView = binding.contentMain.navigationView;
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.navigation_delivery) {
                    setFragment(new DeliveryFragment());
                    return true;
                } else if(id == R.id.navigation_upload) {
                    setFragment(new UploadFragment());
                    return true;

                } else if(id == R.id.navigation_widgets) {
                    setFragment(new WidgetsFragment());
                    return true;

                } else if(id == R.id.navigation_video) {
                    setFragment(new VideoFragment());
                    return true;
                }
                return false;
            }
        });
    }
}