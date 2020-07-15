package com.cloudinary.android.sample.app;


import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.cloudinary.android.LogLevel;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.download.glide.GlideDownloadRequestBuilderFactory;
import com.cloudinary.android.policy.GlobalUploadPolicy;
import com.cloudinary.android.policy.UploadPolicy;
import com.cloudinary.android.sample.R;

public class MainApplication extends Application {
    public static final String NOTIFICATION_CHANNEL_ID = "CLOUDINARY_CHANNEL";
    static MainApplication _instance;
    private Handler mainThreadHandler;

    public static MainApplication get() {
        return _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mainThreadHandler = new Handler(Looper.getMainLooper());
        // This can be called any time regardless of initialization.
        MediaManager.setLogLevel(LogLevel.DEBUG);

        // Mandatory - call a flavor of init. Config can be null if cloudinary_url is provided in the manifest.
        MediaManager.init(this);
        MediaManager.get().setDownloadRequestBuilderFactory(new GlideDownloadRequestBuilderFactory());

        // Optional - configure global policy.
        MediaManager.get().setGlobalUploadPolicy(
                new GlobalUploadPolicy.Builder()
                        .maxConcurrentRequests(4)
                        .networkPolicy(UploadPolicy.NetworkType.ANY)
                        .build());

        createNotificationChannel();

        _instance = this;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(false);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void runOnMainThread(Runnable runnable) {
        mainThreadHandler.post(runnable);
    }
}
