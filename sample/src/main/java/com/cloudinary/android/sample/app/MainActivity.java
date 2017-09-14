package com.cloudinary.android.sample.app;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.DocumentsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
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

public class MainActivity extends AppCompatActivity implements ResourcesAdapter.ImageClickedListener, DeleteRequestsCallback {
    public static final int PAGE_COUNT = 4;
    public static final int UPLOADED_PAGE_POSITION = 0;
    public static final int RECENT_PAGE_POSITION = 1;
    public static final int IN_PROGRESS_PAGE_POSITION = 2;
    public static final int FAILED_PAGE_POSITION = 3;

    private static final int CHOOSE_IMAGE_REQUEST_CODE = 1000;
    private FloatingActionButton fab;
    private BroadcastReceiver receiver;
    private ViewPager pager;
    private Handler backgroundHandler;

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
                Utils.openMediaChooser(MainActivity.this, CHOOSE_IMAGE_REQUEST_CODE);
            }
        });

        // Instantiate a ViewPager and a PagerAdapter.
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);

        HandlerThread handlerThread = new HandlerThread("MainActivityWorker");
        handlerThread.start();

        backgroundHandler = new Handler(handlerThread.getLooper());

        registerLocalReceiver();
        onNewIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterLocalReceiver();
    }

    protected void resourceUpdated(final Resource resource) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (AbstractPagerFragment fragment : getPages()) {
                    ResourcesAdapter adapter = (ResourcesAdapter) fragment.getRecyclerView().getAdapter();
                    adapter.resourceUpdated(resource);
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                pager.setCurrentItem(IN_PROGRESS_PAGE_POSITION);
                uploadImageFromIntentUri(intent);
            } else if (CloudinaryService.ACTION_STATE_ERROR.equals(action)) {
                pager.setCurrentItem(FAILED_PAGE_POSITION);
            } else if (CloudinaryService.ACTION_STATE_IN_PROGRESS.equals(action)) {
                pager.setCurrentItem(IN_PROGRESS_PAGE_POSITION);
            } else if (CloudinaryService.ACTION_STATE_UPLOADED.equals(action)) {
                pager.setCurrentItem(UPLOADED_PAGE_POSITION);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_remove_from_cloud).setVisible(false);
        menu.findItem(R.id.menu_remove_from_local_album).setVisible(false);
        menu.findItem(R.id.menu_sync).setVisible(false);

        switch (pager.getCurrentItem()) {
            case UPLOADED_PAGE_POSITION:
                menu.findItem(R.id.menu_remove_from_local_album).setVisible(true);
                break;
            case IN_PROGRESS_PAGE_POSITION:
                break;
            case FAILED_PAGE_POSITION:
                menu.findItem(R.id.menu_sync).setVisible(true);
                break;
            case RECENT_PAGE_POSITION:
                menu.findItem(R.id.menu_remove_from_cloud).setVisible(true);
                break;

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_remove_from_local_album:
                new ClearMediaFromDeviceDialogFragment().show(getSupportFragmentManager(), "ClearMediaDialogTag");
                return true;
            case R.id.menu_sync:
                syncCloudinary();
                return true;
            case R.id.menu_remove_from_cloud:
                new ClearMediaFromEverywhereDialogFragment().show(getSupportFragmentManager(), "ClearRemoteMediaDialogTag");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ImageActivity.UPLOAD_IMAGE_REQUEST_CODE) {
                // if the user chose to upload right now we want to schedule an immediate upload:
                uploadImage((Resource) data.getSerializableExtra(ImageActivity.RESOURCE_INTENT_EXTRA));
            } else if (requestCode == CHOOSE_IMAGE_REQUEST_CODE && data != null) {
                uploadImageFromIntentUri(data);
            }
        }
    }

    private void syncCloudinary() {
        List<Resource> resources = ResourceRepo.getInstance().listAll();
        for (Resource resource : resources) {
            if (StringUtils.isBlank(resource.getCloudinaryPublicId())) {
                uploadImage(resource);
            }
        }
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
                if (!isFinishing()) {
                    if (CloudinaryService.ACTION_RESOURCE_MODIFIED.equals(intent.getAction())) {
                        Resource resource = (Resource) intent.getSerializableExtra("resource");
                        resourceUpdated(resource);
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
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
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

    private void handleUri(final Uri uri, final int flags) {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                if (DocumentsContract.isDocumentUri(MainActivity.this, uri)) {
                    getContentResolver().takePersistableUriPermission(uri, flags);
                }

                Pair<String, String> pair = Utils.getResourceNameAndType(MainActivity.this, uri);
                Resource resource = new Resource(uri.toString(), pair.first, pair.second);
                uploadImage(resource);
            }
        });
    }

    private void uploadImage(Resource resource) {
        resourceUpdated(ResourceRepo.getInstance().uploadResource(resource));
    }

    public void onImageClicked(Resource resource) {
        startActivityForResult(new Intent(this, ImageActivity.class).putExtra(ImageActivity.RESOURCE_INTENT_EXTRA, resource), ImageActivity.UPLOAD_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onDeleteClicked(Resource resource, Boolean recent) {
        DeleteImageDialogFragment.newInstance(resource, recent != null ? recent : false).show(getSupportFragmentManager(), "DELETE_DIALOG");
    }

    @Override
    public void onRetryClicked(Resource resource) {
        uploadImage(resource);
    }

    @Override
    public void onCancelClicked(Resource resource) {
        MediaManager.get().cancelRequest(resource.getRequestId());
        showSnackBar(getString(R.string.request_cancelled));
        deleteResource(resource, false, false);
    }

    private void showSnackBar(String message) {
        Snackbar.make(fab, message, Snackbar.LENGTH_LONG).show();
    }

    private void deleteResource(Resource resource, final boolean showMessages, final boolean deleteRemote) {
        // Cancel any pending requests
        MediaManager.get().cancelRequest(resource.getRequestId());

        // Delete from local db
        ResourceRepo.getInstance().delete(resource.getLocalUri());

        if (deleteRemote) {
            // Delete from remote cloud - using delete token (received in upload response.)
            CloudinaryHelper.deleteByToken(resource.getDeleteToken(), new CloudinaryHelper.DeleteCallback() {
                @Override
                public void onSuccess() {
                    if (showMessages) {
                        showSnackBar("Remote resource deleted successfully.");
                    }
                }

                @Override
                public void onError(String error) {
                    if (showMessages) {
                        showSnackBar("Remote resource could not be deleted: " + error);
                    }
                }
            });
        }

        List<AbstractPagerFragment> pages = getPages();
        for (AbstractPagerFragment page : pages) {
            RecyclerView recyclerView = page.getRecyclerView();
            ResourcesAdapter adapter = (ResourcesAdapter) recyclerView.getAdapter();
            adapter.removeResource(resource.getLocalUri());
        }
    }

    public List<AbstractPagerFragment> getPages() {
        List<AbstractPagerFragment> pages = new ArrayList<>();
        for (int i = 0; i < PAGE_COUNT; i++) {
            pages.add((AbstractPagerFragment) getSupportFragmentManager().findFragmentByTag(makeFragmentName(pager.getId(), i)));
        }

        return pages;
    }

    @Override
    public void onDeleteAllLocally() {
        ResourceRepo.getInstance().clear();
        MediaManager.get().cancelAllRequests();
        PicassoTools.clearCache(Picasso.with(this));

        for (AbstractPagerFragment fragment : getPages()) {
            fragment.clearData();
        }
    }

    @Override
    public void onDeleteEverywhere() {
        List<Resource> recents = ResourceRepo.getInstance().listRecent();
        for (final Resource recent : recents) {
            deleteResource(recent, false, true);
        }
    }

    @Override
    public void onDeleteResourceLocally(Resource resource) {
        deleteResource(resource, true, false);
    }

    @Override
    public void onDeleteResourceEverywhere(Resource resource) {
        deleteResource(resource, true, true);
    }

    private final class PagerAdapter extends FragmentPagerAdapter {


        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case UPLOADED_PAGE_POSITION:
                    return UploadedPageFragment.newInstance();
                case IN_PROGRESS_PAGE_POSITION:
                    return QueuedPagerFragment.newInstance();
                case FAILED_PAGE_POSITION:
                    return FailedPagerFragment.newInstance();
                case RECENT_PAGE_POSITION:
                    return RecentPagerFragment.newInstance();
            }

            // should never happen
            return UploadedPageFragment.newInstance();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case UPLOADED_PAGE_POSITION:
                    return getString(R.string.tab_title_uploaded);
                case IN_PROGRESS_PAGE_POSITION:
                    return getString(R.string.tab_title_progress);
                case FAILED_PAGE_POSITION:
                    return getString(R.string.tab_title_failed);
                case RECENT_PAGE_POSITION:
                    return getString(R.string.tab_title_recents);
            }

            return "tab " + position;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
}
