package com.cloudinary.android.sample.app;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.android.CldAndroid;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.core.CloudinaryHelper;
import com.cloudinary.android.sample.model.Image;
import com.cloudinary.android.sample.widget.GridDividerItemDecoration;
import com.cloudinary.transformation.TextLayer;
import com.cloudinary.utils.StringUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageActivity extends AppCompatActivity {
    public static final int UPLOAD_IMAGE_REQUEST_CODE = 1001;
    public static final String IMAGE_INTENT_EXTRA = "IMAGE_INTENT_EXTRA";
    public static final String ACTION_UPLOAD = "ACTION_UPLOAD";
    private static final int SPAN = 3;
    private ImageView imageView;
    private Button uploadedButton;
    private Image image;
    private RecyclerView recyclerView;
    private int thumbSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) findViewById(R.id.image_view);
        uploadedButton = (Button) findViewById(R.id.buttonUpload);
        uploadedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent(ACTION_UPLOAD).putExtra(IMAGE_INTENT_EXTRA, image));
                finish();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.effectsGallery);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, SPAN) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(layoutManager);

        fetchImageFromIntent(getIntent());
    }

    private void initEffectGallery() {
        final int dividerSize = getResources().getDimensionPixelSize(R.dimen.grid_divider_width);
        recyclerView.addItemDecoration(new GridDividerItemDecoration(SPAN, dividerSize));
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                thumbSize = Math.round((float) (recyclerView.getWidth() + dividerSize) / SPAN) - dividerSize;
                List<String> urls = generateUrls(thumbSize, image.getCloudinaryPublicId());
                int height = (thumbSize + dividerSize) * (urls.size() / SPAN + urls.size() % SPAN) - dividerSize;
                recyclerView.getLayoutParams().height = height;
                recyclerView.setAdapter(new EffectsGalleryAdapter(ImageActivity.this, urls, thumbSize));
                return true;
            }
        });
    }

    private List<String> generateUrls(int size, String publicId) {
        Cloudinary cloudinary = CldAndroid.get().getCloudinary();
        List<String> urls = new ArrayList<>();

        urls.add(cloudinary.url().transformation(new Transformation().width(size).height(size).gravity("face").radius("max").border("2px_solid_red").crop("thumb").fetchFormat("webp").quality("auto")).generate(publicId));
        urls.add(cloudinary.url().transformation(new Transformation().crop("fill").width(size).height(size).effect("hue:-40").fetchFormat("webp").quality("auto")).generate(publicId));
        urls.add(cloudinary.url().transformation(new Transformation().crop("fill").width(size).height(size).effect("hue:40").angle("hflip").fetchFormat("webp").quality("auto")).generate(publicId));
        urls.add(cloudinary.url().transformation(new Transformation().crop("fill").width(size).height(size).effect("sepia").fetchFormat("webp").quality("auto")).generate(publicId));
        urls.add(cloudinary.url().transformation(new Transformation().crop("fill").width(size).height(size).effect("grayscale").angle("vflip").fetchFormat("webp").quality("auto")).generate(publicId));
        urls.add(cloudinary.url().transformation(new Transformation().crop("fill").width(size).height(size).effect("improve").fetchFormat("webp").quality("auto")).generate(publicId));
        urls.add(cloudinary.url().transformation(new Transformation().crop("fill").width(size).height(size)
                .chain().overlay(new TextLayer().text("Custom text 1").fontFamily("arial").fontSize(500).fontWeight("bold")).width(0.9).flags("relative").color("red").gravity("north").y(10)
                .chain().overlay(new TextLayer().text("Custom text 2").fontFamily("arial").fontSize(500).fontWeight("bold")).width(0.7).flags("relative").color("green").gravity("center")
                .chain().overlay(new TextLayer().text("Custom text 3").fontFamily("arial").fontSize(500).fontWeight("bold")).width(0.6).flags("relative").color("blue").gravity("south").y(10)
                .fetchFormat("webp").quality("auto")).generate(publicId));
        urls.add(cloudinary.url().transformation(new Transformation().crop("fill").width(size).height(size).effect("pixelate_faces").angle("hflip").fetchFormat("webp").quality("auto")).generate(publicId));
        urls.add(cloudinary.url().transformation(new Transformation().crop("fill").width(size).height(size).gravity("faces").effect("sharpen:200").fetchFormat("webp").quality("auto")).generate(publicId));

        return urls;
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

        image = (Image) intent.getSerializableExtra(IMAGE_INTENT_EXTRA);

        String cloudinaryPublicId = image.getCloudinaryPublicId();
        if (StringUtils.isEmpty(cloudinaryPublicId)) {
            Picasso.with(this).load(image.getLocalUri()).placeholder(R.drawable.ic_launcher).into(imageView);
            uploadedButton.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
//            if (imageView.getWidth() > 0){
//                setLargeImageParams();
//            } else {
//                imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//                    @Override
//                    public boolean onPreDraw() {
//                        imageView.getViewTreeObserver().removeOnPreDrawListener(this);
//                        setLargeImageParams();
//                        return true;
//                    }
//                });
//            }

            uploadedButton.setVisibility(View.GONE);
            final String fullSizeUrl = CloudinaryHelper.getOriginalSizeImage(cloudinaryPublicId);
            Picasso.with(this).load(fullSizeUrl).placeholder(R.drawable.ic_launcher).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                    showSnackBar("Error loading image");
                }
            });
            recyclerView.setVisibility(View.VISIBLE);
            uploadedButton.setVisibility(View.GONE);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fullSizeUrl)));
                    ClipData.newPlainText("Cloudinary Url", fullSizeUrl);
                    Toast.makeText(ImageActivity.this, "Url copied to clipboard!", Toast.LENGTH_LONG).show();
                }
            });
            initEffectGallery();
        }
    }
}
