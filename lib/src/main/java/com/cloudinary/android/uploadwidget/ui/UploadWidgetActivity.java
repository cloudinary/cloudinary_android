package com.cloudinary.android.uploadwidget.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.cloudinary.android.R;
import com.cloudinary.android.uploadwidget.UploadWidget;

/**
 * Provides a solution out of the box for developers who want to use the Upload Widget.
 */
public class UploadWidgetActivity extends AppCompatActivity implements UploadWidgetFragment.UploadWidgetListener {

    private static final String UPLOAD_WIDGET_FRAGMENT_ID = "upload_widget_fragment_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_widget);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Uri imageUri = getIntent().getData();
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(UPLOAD_WIDGET_FRAGMENT_ID);
        if (fragment == null) {
            fragment = UploadWidgetFragment.newInstance(imageUri);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, UPLOAD_WIDGET_FRAGMENT_ID)
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
}
