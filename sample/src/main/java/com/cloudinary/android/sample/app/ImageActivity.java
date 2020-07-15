package com.cloudinary.android.sample.app;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.cloudinary.android.download.DownloadRequestCallback;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.core.CloudinaryHelper;
import com.cloudinary.android.sample.model.EffectData;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.utils.StringUtils;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import static com.cloudinary.android.ResponsiveUrl.Preset.FIT;

public class ImageActivity extends AppCompatActivity {
    public static final int UPLOAD_IMAGE_REQUEST_CODE = 1001;
    public static final String RESOURCE_INTENT_EXTRA = "RESOURCE_INTENT_EXTRA";
    private ImageView imageView;
    private Resource resource;
    private RecyclerView recyclerView;
    private int thumbHeight;
    private TextView descriptionTextView;
    private ProgressBar progressBar;
    private SimpleExoPlayer exoPlayer;
    private SimpleExoPlayerView exoPlayerView;
    private ExoPlayer.EventListener listener;
    private String currentUrl = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        imageView = (ImageView) findViewById(R.id.image_view);
        descriptionTextView = (TextView) findViewById(R.id.effectDescription);
        recyclerView = (RecyclerView) findViewById(R.id.effectsGallery);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        initExoPlayer();

        fetchImageFromIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_menu, menu);
        return true;
    }


    private void initExoPlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        exoPlayerView = ((SimpleExoPlayerView) findViewById(R.id.exoPlayer));
        exoPlayerView.setPlayer(exoPlayer);

        listener = new ExoPlayer.EventListener() {

            @Override
            public void onTracksChanged(TrackGroupArray trackGroupArray, TrackSelectionArray trackSelectionArray) {
                if (trackGroupArray.length > 0) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingChanged(boolean b) {
                progressBar.setVisibility(b ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPlayerStateChanged(boolean b, int i) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ImageActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        };
    }

    private void initEffectGallery() {
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                thumbHeight = Math.round((float) (recyclerView.getWidth() / 4));
                List<EffectData> data = CloudinaryHelper.generateEffectsList(ImageActivity.this, resource);
                recyclerView.setAdapter(new EffectsGalleryAdapter(data, resource.getResourceType(), thumbHeight, new EffectsGalleryAdapter.ItemClickListener() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
    }

    private void updateMainImage(EffectData data) {
        currentUrl = null;
        if (resource.getResourceType().equals("image")) {
            loadImage(data);
        } else {
            loadVideo(data);
        }

        descriptionTextView.setText(data.getDescription());
    }

    private void loadVideo(final EffectData data) {
        progressBar.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        final DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Cloudinary Sample App"), null);
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        Url baseUrl = MediaManager.get().url().resourceType("video").publicId(data.getPublicId()).transformation(data.getTransformation());
        MediaManager.get().responsiveUrl(exoPlayerView, baseUrl, FIT, new ResponsiveUrl.Callback() {
            @Override
            public void onUrlReady(Url url) {
                String urlString = url.generate();
                currentUrl = urlString;
                MediaSource videoSource = new ExtractorMediaSource(Uri.parse(urlString), dataSourceFactory, extractorsFactory, null, null);
                exoPlayer.addListener(listener);
                exoPlayer.prepare(videoSource);
            }
        });
    }

    private void loadImage(final EffectData data) {
        exoPlayer.removeListener(listener);
        exoPlayerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        Url baseUrl = MediaManager.get().url().publicId(data.getPublicId()).transformation(data.getTransformation());
        MediaManager.get().responsiveUrl(imageView, baseUrl, FIT, new ResponsiveUrl.Callback() {
            @Override
            public void onUrlReady(Url url) {
                currentUrl = url.generate();
            }
        });

        MediaManager.get().download(this)
                .load(data.getPublicId())
                .transformation(data.getTransformation())
                .responsive(FIT)
                .callback(new DownloadRequestCallback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_url:
                if (StringUtils.isNotBlank(currentUrl)) {
                    openUrlWithToast(currentUrl);
                }

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
        if (intent == null || !intent.hasExtra(RESOURCE_INTENT_EXTRA)) {
            // something wrong, nothing to load.
            Toast.makeText(this, "Could not load image.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            resource = (Resource) intent.getSerializableExtra(RESOURCE_INTENT_EXTRA);
            String cloudinaryPublicId = resource.getCloudinaryPublicId();
            if (StringUtils.isEmpty(cloudinaryPublicId)) {
                Toast.makeText(this, "Could not load image.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                initEffectGallery();
            }
        }
    }

    private void openUrlWithToast(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        ClipData.newPlainText("Cloudinary Url", url);
        Toast.makeText(ImageActivity.this, "Url copied to clipboard!", Toast.LENGTH_LONG).show();
    }
}