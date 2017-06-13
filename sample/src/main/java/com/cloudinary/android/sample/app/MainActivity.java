package com.cloudinary.android.sample.app;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.cloudinary.android.CldAndroid;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.core.CloudinaryHelper;
import com.cloudinary.android.sample.model.Image;
import com.cloudinary.android.sample.persist.ImageRepository;
import com.cloudinary.android.sample.widget.GridDividerItemDecoration;
import com.cloudinary.utils.StringUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainGalleryAdapter.ImageClickedListener {
    private static final int CHOOSE_IMAGE_REQUEST_CODE = 1000;
    private static final int SPAN = 2;

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private int thumbSize = 400; // some default for initialization.
    private BroadcastReceiver receiver;
    private int dividerSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMediaChooser();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.mainGallery);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, SPAN);
        recyclerView.setLayoutManager(layoutManager);

        dividerSize = getResources().getDimensionPixelSize(R.dimen.grid_divider_width);
        recyclerView.addItemDecoration(new GridDividerItemDecoration(SPAN, dividerSize));
        recyclerView.setAdapter(new MainGalleryAdapter(this, ImageRepository.getInstance().listImages(), thumbSize, this));

        onNewIntent(getIntent());
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterLocalReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (recyclerView.getWidth() > 0) {
            initThumbSizeAndLoadData();
        } else {
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    initThumbSizeAndLoadData();
                    return true;
                }
            });
        }
    }

    private void initThumbSizeAndLoadData() {
        thumbSize = recyclerView.getWidth() / SPAN - dividerSize / 2;
        recyclerView.setAdapter(new MainGalleryAdapter(MainActivity.this, new ArrayList<Image>(), thumbSize, MainActivity.this));
        // fetch data after we know the size so we can request the exact size from Cloudinary
        loadData();
    }

    private void unregisterLocalReceiver() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerLocalReceiver();
    }

    private void registerLocalReceiver() {
        IntentFilter filter = new IntentFilter(CloudinaryService.ACTION_UPLOAD_SUCCESS);
        filter.addAction(CloudinaryService.ACTION_UPLOAD_ERROR);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (CloudinaryService.ACTION_UPLOAD_ERROR.equals(intent.getAction())) {
                    showSnackBar("Error uploading image: " + intent.getStringExtra("error"));
                } else if (CloudinaryService.ACTION_UPLOAD_SUCCESS.equals(intent.getAction())) {
                    showSnackBar(getString(R.string.upload_successfully));
                    loadData();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction())) {
            uploadImageFromIntentUri(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                clearLocalImages();
                loadData();
                return true;
            case R.id.menu_upload:
                syncCloudinary();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearLocalImages() {
        ImageRepository.getInstance().clear();
        CldAndroid.get().cancelAllRequests();
        PicassoTools.clearCache(Picasso.with(this));
    }

    private void syncCloudinary() {
        List<Image> images = ImageRepository.getInstance().listImages();
        for (Image image : images) {
            if (StringUtils.isBlank(image.getCloudinaryPublicId())) {
                uploadImage(image);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ImageActivity.UPLOAD_IMAGE_REQUEST_CODE) {
                // if the user chose to upload right now we want to schedule an immediate upload:
                uploadImage((Image) data.getSerializableExtra(ImageActivity.IMAGE_INTENT_EXTRA));
            } else if (requestCode == CHOOSE_IMAGE_REQUEST_CODE && data != null) {
                uploadImageFromIntentUri(data);
            }
        }
    }

    private void uploadImageFromIntentUri(Intent data) {
        final int takeFlags = data.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Uri uri = data.getData();
        if (uri != null) {
            handleUri(uri, takeFlags);
        } else if (data.getClipData() != null) {
            ClipData clip = data.getClipData();
            for (int i = 0; i < clip.getItemCount(); i++) {
                handleUri(clip.getItemAt(i).getUri(), takeFlags);
            }
        }
    }

    private void handleUri(Uri uri, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(this, uri)) {
            getContentResolver().takePersistableUriPermission(uri, flags);
        }

        Image image = new Image(uri.toString());
        uploadImage(image);
        addImage(image);
    }

    private void uploadImage(Image image) {
        if (StringUtils.isNotBlank(image.getRequestId())) {
            // cancel previous upload requests for this image:
            CldAndroid.get().cancelRequest(image.getRequestId());
        }

        String requestId = CloudinaryHelper.uploadImage(image.getLocalUri());
        image.setRequestId(requestId);
        ImageRepository.getInstance().uploadQueued(image);
    }

    public void onImageClicked(Image image) {
        startActivityForResult(new Intent(this, ImageActivity.class).putExtra(ImageActivity.IMAGE_INTENT_EXTRA, image), ImageActivity.UPLOAD_IMAGE_REQUEST_CODE);
    }

    private void addImage(Image image) {
        MainGalleryAdapter adapter = (MainGalleryAdapter) recyclerView.getAdapter();
        adapter.addImage(image);
        recyclerView.smoothScrollToPosition(0);
    }

    private void showSnackBar(String message) {
        Snackbar.make(fab, message, Snackbar.LENGTH_LONG).show();
    }

    private void loadData() {
        ((MainGalleryAdapter) recyclerView.getAdapter()).replaceImages(ImageRepository.getInstance().listImages());
    }

    private void openMediaChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_IMAGE_REQUEST_CODE);
    }
}
