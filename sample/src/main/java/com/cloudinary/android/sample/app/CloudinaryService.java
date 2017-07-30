package com.cloudinary.android.sample.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.TypedValue;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.ListenerService;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.core.CloudinaryHelper;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.android.sample.persist.ResourceRepo;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CloudinaryService extends ListenerService {

    public static final String ACTION_RESOURCE_MODIFIED = "ACTION_RESOURCE_MODIFIED";
    public static final String ACTION_UPLOAD_PROGRESS = "ACTION_UPLOAD_PROGRESS";
    private static final String TAG = "CloudinaryService";
    final private Map<String, Bitmap> bitmaps = new HashMap<>();
    private NotificationManager notificationManager;
    private AtomicInteger idsProvider = new AtomicInteger(1000);
    private Map<String, Integer> requestIdsToNotificationIds = new ConcurrentHashMap<>();
    private NotificationCompat.Builder builder;
    private Handler backgroundThreadHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Uploading to Cloudinary...")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(this, 999, new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0))
                .setOngoing(true);

        HandlerThread handlerThread = new HandlerThread("CloudinaryServiceBackgroundThread");
        handlerThread.start();
        backgroundThreadHandler = new Handler(handlerThread.getLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private android.support.v4.app.NotificationCompat.Builder getBuilder(String requestId) {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 1234, new Intent(this, MainActivity.class), 0))
                .setLargeIcon(getBitmap(requestId));
    }

    private Bitmap getBitmap(String requestId) {
        synchronized (bitmaps) {
            Bitmap bitmap = bitmaps.get(requestId);
            if (bitmap == null) {
                try {
                    String uri = ResourceRepo.getInstance().getLocalUri(requestId);
                    int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
                    bitmap = Utils.decodeBitmapStream(this, Uri.parse(uri), value, value);
                    bitmaps.put(requestId, bitmap);
                } catch (Exception e) {
                    // print but don't fail the notification
                    e.printStackTrace();
                }
            }

            return bitmap;
        }
    }

    private void cleanupBitmap(String requestId) {
        synchronized (bitmaps) {
            bitmaps.remove(requestId);
        }
    }

    private void cancelNotification(String requestId) {
        Integer id = requestIdsToNotificationIds.get(requestId);
        if (id != null) {
            notificationManager.cancel(id);
        }
    }

    private boolean sendBroadcast(Resource updatedResource) {
        return LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_RESOURCE_MODIFIED).putExtra("resource", updatedResource));
    }

    @Override
    public void onStart(final String requestId) {
        backgroundThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                sendBroadcast(ResourceRepo.getInstance().resourceUploading(requestId));
            }
        });
        cancelNotification(requestId);
        int id = idsProvider.incrementAndGet();
        requestIdsToNotificationIds.put(requestId, id);
        notificationManager.notify(id,
                getBuilder(requestId)
                        .setContentTitle("Preparing upload...")
                        .build());
    }

    @Override
    public synchronized void onProgress(String requestId, long bytes, long totalBytes) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_UPLOAD_PROGRESS).putExtra("requestId", requestId).putExtra("bytes", bytes).putExtra("totalBytes", totalBytes));
        Integer notificationId = requestIdsToNotificationIds.get(requestId);

        if (notificationId == null) {
            notificationId = idsProvider.incrementAndGet();
            requestIdsToNotificationIds.put(requestId, notificationId);
        }

        if (totalBytes > 0) {
            double progressFraction = (double) bytes / totalBytes;
            int progress = (int) Math.round(progressFraction * 1000);
            builder.setProgress(1000, progress, false);
            builder.setContentText(String.format("%d%% (%d KB)", (int) (progressFraction * 100), bytes / 1024));
        } else {
            builder.setProgress(1000, 1000, true);
            builder.setContentText(String.format("%d KB", bytes / 1024));
        }

        builder.setLargeIcon(getBitmap(requestId));
        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    public void onSuccess(final String requestId, final Map resultData) {
        final String publicId = (String) resultData.get("public_id");

        backgroundThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                sendBroadcast(ResourceRepo.getInstance().resourceUploaded(requestId, publicId, (String) resultData.get("delete_token")));
            }
        });

        cancelNotification(requestId);
        // prefetch the image into picasso cache:
        Picasso.with(this).load(CloudinaryHelper.getUrlForMaxWidth(this, publicId)).fetch();
        int id = idsProvider.incrementAndGet();
        requestIdsToNotificationIds.put(requestId, id);
        notificationManager.notify(id,
                getBuilder(requestId)
                        .setContentTitle("Cloudinary Upload")
                        .setContentText("The image was uploaded successfully!")
                        .build());

        cleanupBitmap(requestId);
    }

    @Override
    public void onError(final String requestId, final ErrorInfo error) {
        backgroundThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                sendBroadcast(ResourceRepo.getInstance().resourceFailed(requestId, error.getCode()));
            }
        });
        cancelNotification(requestId);


        int id = idsProvider.incrementAndGet();
        requestIdsToNotificationIds.put(requestId, id);

        String errorMessage = CloudinaryHelper.getPrettyErrorMessage(error.getCode());
        notificationManager.notify(id,
                getBuilder(requestId)
                        .setContentTitle("Error uploading.")
                        .setContentText(errorMessage)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .setBigContentTitle("Error uploading.")
                                .bigText(errorMessage))
                        .build());

        cleanupBitmap(requestId);
    }

    @Override
    public void onReschedule(final String requestId, final ErrorInfo error) {
        backgroundThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                sendBroadcast(ResourceRepo.getInstance().resourceRescheduled(requestId, error.getCode()));
            }
        });
        cancelNotification(requestId);
        int id = idsProvider.incrementAndGet();
        requestIdsToNotificationIds.put(requestId, id);
        notificationManager.notify(id,
                getBuilder(requestId)
                        .setContentTitle("Connection issues")
                        .setContentText("The upload will resume once network is available.")
                        .build());
    }
}