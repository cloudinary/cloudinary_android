package com.cloudinary.android.uploadwidget.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.cloudinary.android.R;
import com.cloudinary.android.uploadwidget.UploadWidget;

/**
 * Provides a solution out of the box for developers who want to use the Upload Widget.
 */
public class UploadWidgetActivity extends AppCompatActivity implements UploadWidgetFragment.UploadWidgetListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_widget);
        enterFullscreen();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Uri imageUri = getIntent().getData();
        UploadWidgetFragment uploadWidgetFragment = UploadWidgetFragment.newInstance(imageUri, this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, uploadWidgetFragment)
                .commit();
    }

    @Override
    public void onConfirm(Uri imageUri, UploadWidget.Result result) {
        Intent data = new Intent();
        data.setData(imageUri);
        data.putExtra(UploadWidget.RESULT_EXTRA, result);

        setResult(RESULT_OK, data);
        finish();
    }

    private void enterFullscreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
