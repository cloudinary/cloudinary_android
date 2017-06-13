package com.cloudinary.android.sample.app;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.cloudinary.android.ListenerService;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.core.CloudinaryHelper;
import com.cloudinary.android.sample.persist.ImageRepository;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CloudinaryService extends ListenerService {
    public static final String ACTION_UPLOAD_SUCCESS = "ACTION_UPLOAD_SUCCESS";
    public static final String ACTION_UPLOAD_ERROR = "ACTION_UPLOAD_ERROR";
    private static final String TAG = "CloudinaryService";
    private NotificationManager notificationManager;
    private AtomicInteger idsProvider = new AtomicInteger(1000);

    private Map<String, Integer> requestIdsToNotificationIds = new ConcurrentHashMap<>();
    private NotificationCompat.Builder builder;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Image upload")
                .setContentText("Upload in progress...")
                .setSmallIcon(R.drawable.ic_launcher);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(String requestId) {
    }

    @Override
    public synchronized void onProgress(String requestId, long bytes, long totalBytes) {
        Integer notificationId = requestIdsToNotificationIds.get(requestId);
        if (notificationId == null) {
            notificationId = idsProvider.incrementAndGet();
            requestIdsToNotificationIds.put(requestId, notificationId);
        }

        if (totalBytes > 0) {
            double progressFraction = (double) bytes / totalBytes;
            int progress = (int) Math.round(progressFraction * 1000);
            builder.setProgress(1000, progress, false);
        } else {
            builder.setProgress(1000, 1000, true);
        }

        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    public void onSuccess(String requestId, Map resultData) {
        cancelNotification(requestId);
        String publicId = (String) resultData.get("public_id");
        ImageRepository.getInstance().imageUploaded(requestId, publicId, (String) resultData.get("delete_token"), (int) resultData.get("width"), (int) resultData.get("height"), new Date());

        // prefetch the image into picasso cache:
        Picasso.with(this).load(CloudinaryHelper.getUrlForMaxWidth(this, publicId)).fetch();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_UPLOAD_SUCCESS).putExtra("requestId", requestId));
    }

    private void cancelNotification(String requestId) {
        Integer id = requestIdsToNotificationIds.get(requestId);
        if (id != null) {
            notificationManager.cancel(id);
        }
    }

    @Override
    public void onError(String requestId, String error) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_UPLOAD_ERROR).putExtra("requestId", requestId).putExtra("error", error));
        cancelNotification(requestId);
    }

    @Override
    public void onReschedule(String requestId) {
        cancelNotification(requestId);
    }
}