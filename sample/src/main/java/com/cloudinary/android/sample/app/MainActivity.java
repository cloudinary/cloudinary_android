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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.core.CloudinaryHelper;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.android.sample.persist.ResourceRepo;
import com.cloudinary.utils.StringUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ResourcesAdapter.ImageClickedListener, DeleteImageDialogFragment.ImageDeleteRequested {
    public static final int PAGE_COUNT = 3;
    private static final int CHOOSE_IMAGE_REQUEST_CODE = 1000;
    private FloatingActionButton fab;
    private BroadcastReceiver receiver;
    private ViewPager pager;
    private PagerAdapter pagerAdapter;

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }

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


        // Instantiate a ViewPager and a PagerAdapter.
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(2);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);
        registerLocalReceiver();
        onNewIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterLocalReceiver();
    }

    private void unregisterLocalReceiver() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
    }

    private void registerLocalReceiver() {
        IntentFilter filter = new IntentFilter(CloudinaryService.ACTION_RESOURCE_MODIFIED);
        filter.addAction(CloudinaryService.ACTION_UPLOAD_PROGRESS);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (CloudinaryService.ACTION_RESOURCE_MODIFIED.equals(intent.getAction())) {
                    Resource resource = (Resource) intent.getSerializableExtra("resource");
                    for (AbstractPagerFragment fragment : getPages()) {
                        ResourcesAdapter adapter = (ResourcesAdapter) fragment.getRecyclerView().getAdapter();
                        adapter.resourceUpdated(resource);
                    }
                } else if (CloudinaryService.ACTION_UPLOAD_PROGRESS.equals(intent.getAction())) {
                    String requestId = intent.getStringExtra("requestId");
                    long bytes = intent.getLongExtra("bytes", 0);
                    long totalBytes = intent.getLongExtra("totalBytes", 0);
                    for (AbstractPagerFragment fragment : getPages()) {
                        ResourcesAdapter adapter = (ResourcesAdapter) fragment.getRecyclerView().getAdapter();
                        adapter.progressUpdated(requestId, bytes, totalBytes);
                    }

                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            if (Intent.ACTION_SEND.equals(intent.getAction()) || Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                uploadImageFromIntentUri(intent);
            }
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
                for (AbstractPagerFragment fragment : getPages()) {
                    fragment.clearData();
                }
                return true;
            case R.id.menu_upload:
                syncCloudinary();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearLocalImages() {
        ResourceRepo.getInstance().clear();
        MediaManager.get().cancelAllRequests();
        PicassoTools.clearCache(Picasso.with(this));
    }

    private void syncCloudinary() {
        List<Resource> resources = ResourceRepo.getInstance().listAll();
        for (Resource resource : resources) {
            if (StringUtils.isBlank(resource.getCloudinaryPublicId())) {
                uploadImage(resource);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ImageActivity.UPLOAD_IMAGE_REQUEST_CODE) {
                // if the user chose to upload right now we want to schedule an immediate upload:
                uploadImage((Resource) data.getSerializableExtra(ImageActivity.IMAGE_INTENT_EXTRA));
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

        Resource resource = new Resource(uri.toString());
        uploadImage(resource);
    }

    private void uploadImage(Resource resource) {
        if (StringUtils.isNotBlank(resource.getRequestId())) {
            // cancel previous upload requests for this resource:
            MediaManager.get().cancelRequest(resource.getRequestId());
        }

        String requestId = CloudinaryHelper.uploadImage(resource.getLocalUri());
        resource.setRequestId(requestId);
        resource.setResourceType("image");
        ResourceRepo.getInstance().resourceQueued(resource);
    }

    public void onImageClicked(Resource resource) {
        startActivityForResult(new Intent(this, ImageActivity.class).putExtra(ImageActivity.IMAGE_INTENT_EXTRA, resource), ImageActivity.UPLOAD_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onDeleteClicked(Resource resource) {
        DeleteImageDialogFragment.newInstance(resource.getLocalUri()).show(getFragmentManager(), "DELETE_DIALOG");
    }

    private void showSnackBar(String message) {
        Snackbar.make(fab, message, Snackbar.LENGTH_LONG).show();
    }

    private void openMediaChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
//        intent.setType("video/*");
        startActivityForResult(intent, CHOOSE_IMAGE_REQUEST_CODE);
    }

    @Override
    public void deleteResource(String id) {
        List<AbstractPagerFragment> pages = getPages();
        for (AbstractPagerFragment page : pages) {
            RecyclerView recyclerView = page.getRecyclerView();
            ResourcesAdapter adapter = (ResourcesAdapter) recyclerView.getAdapter();
            Resource resource = adapter.removeResource(id);
            if (resource != null) {
                ResourceRepo.getInstance().delete(id);
                // delete remotely if possible
                CloudinaryHelper.deleteByToken(resource.getDeleteToken(), new CloudinaryHelper.DeleteCallback() {
                    @Override
                    public void onSuccess() {
                        showSnackBar("Remote resource deleted successfully.");
                    }

                    @Override
                    public void onError(String error) {
                        showSnackBar("Remote resource could not be deleted: " + error);
                    }
                });
            }
        }
    }

    public List<AbstractPagerFragment> getPages() {
        List<AbstractPagerFragment> pages = new ArrayList<>();
        for (int i = 0; i < PAGE_COUNT; i++) {
            pages.add((AbstractPagerFragment) getSupportFragmentManager().findFragmentByTag(makeFragmentName(pager.getId(), i)));
        }

        return pages;
    }

    private final class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return UploadedPageFragment.newInstance();
                case 1:
                    return QueuedPagerFragment.newInstance();
                case 2:
                    return FailedPagerFragment.newInstance();
            }

            // should never happen
            return UploadedPageFragment.newInstance();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_title_uploaded);
                case 1:
                    return getString(R.string.tab_title_progress);
                case 2:
                    return getString(R.string.tab_title_failed);
            }
            return "tab " + position;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }


    }
}
