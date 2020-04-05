package com.cloudinary.android.uploadwidget.ui;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.ui.R;
import com.cloudinary.android.uploadwidget.UploadWidget;

import java.util.ArrayList;

import static com.cloudinary.android.uploadwidget.UploadWidget.RESULT_EXTRA;

/**
 * Provides a solution out of the box for developers who want to use the Upload Widget.
 */
public class UploadWidgetActivity extends AppCompatActivity implements UploadWidgetFragment.UploadWidgetListener {

    private static final String UPLOAD_WIDGET_FRAGMENT_TAG = "upload_widget_fragment_tag";
    private static final int MEDIA_CHOOSER_REQUEST_CODE = 5050;
    private boolean isFullFlow;
    private boolean startNow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_widget);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        isFullFlow = !getIntent().hasExtra(UploadWidget.URIS_EXTRA);

        if (isFullFlow) {
            startNow = getIntent().getBooleanExtra(UploadWidget.START_NOW_EXTRA, false);
            UploadWidget.openMediaChooser(this, MEDIA_CHOOSER_REQUEST_CODE);
        } else {
            ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(UploadWidget.URIS_EXTRA);
            showImages(uris);
        }
    }

    private void showImages(ArrayList<Uri> uris) {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEDIA_CHOOSER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<Uri> uris = extractImageUris(data);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    for (Uri uri : uris) {
                        if (DocumentsContract.isDocumentUri(this, uri)) {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                    }
                }

                showImages(uris);
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    private ArrayList<Uri> extractImageUris(Intent data) {
        ArrayList<Uri> imageUris = new ArrayList<>();

        ClipData clipData = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            clipData = data.getClipData();
        }

        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                imageUris.add(clipData.getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            imageUris.add(data.getData());
        }

        return imageUris;
    }

    private ArrayList<UploadWidget.Result> handleUploadWidgetResult(final ArrayList<UploadWidget.Result> results) {
        for (UploadWidget.Result result : results) {
            UploadRequest uploadRequest = UploadWidget.preprocessResult(this, result);
            String requestId = startNow ? uploadRequest.startNow(this) : uploadRequest.dispatch(this);
            result.requestId = requestId;
        }

        return results;
    }

    @Override
    public void onConfirm(ArrayList<UploadWidget.Result> results) {
        Intent data = new Intent();
        if (isFullFlow) {
            ArrayList<UploadWidget.Result> populatedResults = handleUploadWidgetResult(results);
            data.putParcelableArrayListExtra(RESULT_EXTRA, populatedResults);
        } else {
            data.putParcelableArrayListExtra(UploadWidget.RESULT_EXTRA, results);
        }

        setResult(RESULT_OK, data);
        finish();
    }
}
