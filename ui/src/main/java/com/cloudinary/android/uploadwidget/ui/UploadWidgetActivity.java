package com.cloudinary.android.uploadwidget.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.ui.R;
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

        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(UploadWidget.URIS_EXTRA);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(UPLOAD_WIDGET_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = UploadWidgetFragment.newInstance(uris);
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
