package com.cloudinary.android.uploadwidget.ui;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cloudinary.android.R;
import com.cloudinary.android.uploadwidget.CropPoints;
import com.cloudinary.android.uploadwidget.UploadWidget;

/**
 * TODO: Document this
 */
public class UploadWidgetActivity extends AppCompatActivity {

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_widget);

        /**
         * TODO: This should come from the fragment
         */
        imageUri = getIntent().getData();
        ImagePreviewFragment imagePreviewFragment = ImagePreviewFragment.newInstance(imageUri);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, imagePreviewFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        // TODO: uncomment this
//        setResultCancel();
        setResult(imageUri, new CropPoints(new Point(0, 0), new Point(200, 200)));
        super.onBackPressed();
    }

    public void setResult(Uri imageUri, CropPoints cropPoints) {
        UploadWidget.ActivityResult result = new UploadWidget.ActivityResult(cropPoints);

        Intent data = new Intent();
        data.setData(imageUri);
        data.putExtra(UploadWidget.RESULT_EXTRA, result);

        setResult(RESULT_OK, data);
    }

    private void setResultCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
