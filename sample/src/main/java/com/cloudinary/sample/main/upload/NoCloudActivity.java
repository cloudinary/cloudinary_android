package com.cloudinary.sample.main.upload;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.sample.databinding.NoCloudFragmentBinding;
import com.cloudinary.sample.helpers.CloudinaryHelper;

public class NoCloudActivity extends AppCompatActivity {

    NoCloudFragmentBinding binding;
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = NoCloudFragmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCloseButton();
        setGetStartedButton();
        setCantFindCloudName();
    }

    private void setCantFindCloudName() {
        binding.cantFindCloudContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), WebViewActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setGetStartedButton() {
        binding.getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloudinaryHelper.setMediaManager(getBaseContext(), binding.hiDevelopersEdittext.getText().toString());
                saveCloudName(binding.hiDevelopersEdittext.getText().toString());
                finish();
            }
        });
    }

    private void saveCloudName(String cloudName) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cloud_name", cloudName);
        editor.apply();
    }

    private void setCloseButton() {
        binding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
