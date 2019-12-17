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

import java.util.ArrayList;

/**
 * Provides a solution out of the box for developers who want to use the Upload Widget.
 */
public class UploadWidgetActivity extends AppCompatActivity implements UploadWidgetFragment.UploadWidgetListener {

    private static final String UPLOAD_WIDGET_FRAGMENT_TAG = "upload_widget_fragment_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_widget);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        ArrayList<Uri> imageUris = getIntent().getParcelableArrayListExtra(UploadWidget.IMAGES_URI_EXTRA);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(UPLOAD_WIDGET_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = UploadWidgetFragment.newInstance(imageUris);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, UPLOAD_WIDGET_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onConfirm(ArrayList<UploadWidget.Result> results) {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(UploadWidget.RESULT_EXTRA, results);

        setResult(RESULT_OK, data);
        finish();
    }
}
