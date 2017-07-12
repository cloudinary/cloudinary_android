package com.cloudinary.android.sample.app;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.core.CloudinaryHelper;
import com.cloudinary.android.sample.model.EffectData;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.utils.StringUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageActivity extends AppCompatActivity {
    public static final int UPLOAD_IMAGE_REQUEST_CODE = 1001;
    public static final String IMAGE_INTENT_EXTRA = "IMAGE_INTENT_EXTRA";
    public static final String ACTION_UPLOAD = "ACTION_UPLOAD";
    private ImageView imageView;
    private Resource resource;
    private RecyclerView recyclerView;
    private int thumbSize;
    private TextView descriptionTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) findViewById(R.id.image_view);
        descriptionTextView = (TextView) findViewById(R.id.effectDescription);
        recyclerView = (RecyclerView) findViewById(R.id.effectsGallery);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        fetchImageFromIntent(getIntent());
    }

    private void initEffectGallery() {
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                thumbSize = Math.round((float) (recyclerView.getWidth() / 4));
                List<EffectData> data = CloudinaryHelper.generateEffectsList(ImageActivity.this, Utils.getScreenWidth(ImageActivity.this), thumbSize, resource.getCloudinaryPublicId());
                recyclerView.getLayoutParams().height = thumbSize;
                recyclerView.setAdapter(new EffectsGalleryAdapter(ImageActivity.this, data, thumbSize, new EffectsGalleryAdapter.ItemClickListener() {
                    @Override
                    public void onClick(EffectData data) {
                        updateMainImage(data);
                    }
                }));

                updateMainImage(data.get(0));

                return true;
            }
        });
    }

    private void updateMainImage(EffectData data) {
        imageView.setTag(data.getImageUrl());
        Picasso.with(this).load(Uri.parse(data.getImageUrl())).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
                showSnackBar("Error loading resource");
            }
        });

        descriptionTextView.setText(data.getDescription());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fetchImageFromIntent(intent);
    }

    private void showSnackBar(String message) {
        Snackbar.make(imageView, message, Snackbar.LENGTH_LONG).show();
    }

    private void fetchImageFromIntent(Intent intent) {
        if (intent == null || !intent.hasExtra(IMAGE_INTENT_EXTRA)) {
            finish();
        }

        resource = (Resource) intent.getSerializableExtra(IMAGE_INTENT_EXTRA);

        String cloudinaryPublicId = resource.getCloudinaryPublicId();
        if (StringUtils.isEmpty(cloudinaryPublicId)) {
            Toast.makeText(this, "Could not load image.", Toast.LENGTH_SHORT);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openUrlWithToast(v.getTag().toString());
                }
            });

            initEffectGallery();
        }
    }

    private void openUrlWithToast(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        ClipData.newPlainText("Cloudinary Url", url);
        Toast.makeText(ImageActivity.this, "Url copied to clipboard!", Toast.LENGTH_LONG).show();
    }
}
