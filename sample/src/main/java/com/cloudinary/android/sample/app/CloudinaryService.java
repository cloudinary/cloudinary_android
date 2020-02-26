package com.cloudinary.android.sample.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.TypedValue;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.ListenerService;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.android.sample.persist.ResourceRepo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CloudinaryService extends ListenerService {

    public static final String ACTION_RESOURCE_MODIFIED = "ACTION_RESOURCE_MODIFIED";
    public static final String ACTION_UPLOAD_PROGRESS = "ACTION_UPLOAD_PROGRESS";
    public static final String ACTION_STATE_ERROR = "cloudinary.action_error";
    public static final String ACTION_STATE_UPLOADED = "cloudinary.action_uploaded";
    public static final String ACTION_STATE_IN_PROGRESS = "cloudinary.action_progress";

    private static final String TAG = "CloudinaryService";

    final private Map<String, BitmapResult> bitmaps = new HashMap<>();
    private NotificationManager notificationManager;
    private AtomicInteger idsProvider = new AtomicInteger(1000);
    private Map<String, Integer> requestIdsToNotificationIds = new ConcurrentHashMap<>();
    private Notification.Builder builder;
    private Handler backgroundThreadHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        builder = new Notification.Builder(this);
        builder.setContentTitle("Uploading to Cloudinary...")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(this, 999,
                        new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).setAction(ACTION_STATE_IN_PROGRESS),
                        0))
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(MainApplication.NOTIFICATION_CHANNEL_ID);
        }

        HandlerThread handlerThread = new HandlerThread("CloudinaryServiceBackgroundThread");
        handlerThread.start();
        backgroundThreadHandler = new Handler(handlerThread.getLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification.Builder getBuilder(String requestId, Resource.UploadStatus status) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 1234,
                        new Intent(this, MainActivity.class)
                                .setAction(actionFromStatus(status))
                        , 0))
                .setLargeIcon(getBitmap(requestId));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(MainApplication.NOTIFICATION_CHANNEL_ID);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(getResources().getColor(R.color.colorPrimary));
        }

        return builder;

    }

    private String actionFromStatus(Resource.UploadStatus status) {
        switch (status) {

            case QUEUED:
            case UPLOADING:
                return ACTION_STATE_IN_PROGRESS;
            case UPLOADED:
                return ACTION_STATE_UPLOADED;
            case RESCHEDULED:
            case FAILED:
                return ACTION_STATE_ERROR;
            default:
                return ACTION_STATE_UPLOADED;
        }
    }

    private Bitmap getBitmap(String requestId) {
        BitmapResult result = bitmaps.get(requestId);
        if (result == null) {
            synchronized (bitmaps) {
                result = bitmaps.get(requestId);
                if (result == null) {
                    Bitmap bitmap = null;
                    try {
                        Resource resource = ResourceRepo.getInstance().getResource(requestId);
                        String uri = resource.getLocalUri();
                        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
                        if (resource.getResourceType().equals("image")) {
                            bitmap = Utils.decodeBitmapStream(this, Uri.parse(uri), value, value);
                        }
                    } catch (Exception e) {
                        // print but don't fail the notification
                        e.printStackTrace();
                    }

                    // bitmap can be null, save it anyway (we don't want to retry)
                    result = new BitmapResult(bitmap);
                    bitmaps.put(requestId, result);
                }
            }
        }

        return result.bitmap;
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
        // This is called from background threads and the main thread  may touch the resource and delete it
        // in the meantime (from the activity) - verify it's still around before sending the broadcast
        if (updatedResource != null) {
            return LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_RESOURCE_MODIFIED).putExtra("resource", updatedResource));
        }

        return false;
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
                getBuilder(requestId, Resource.UploadStatus.UPLOADING)
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

        int id = idsProvider.incrementAndGet();
        requestIdsToNotificationIds.put(requestId, id);
        String resourceType = (String) resultData.get("resource_type");
        notificationManager.notify(id,
                getBuilder(requestId, Resource.UploadStatus.UPLOADED)
                        .setContentTitle("Cloudinary Upload")
                        .setContentText(String.format("The %s was uploaded successfully!", resourceType))
                        .build());

        cleanupBitmap(requestId);
    }

    @Override
    public void onError(final String requestId, final ErrorInfo error) {
        backgroundThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                final Resource resource;
                if (error.getCode() == ErrorInfo.REQUEST_CANCELLED) {
                    resource = ResourceRepo.getInstance().getResource(requestId);
                    if (resource != null) {
                        ResourceRepo.getInstance().delete(resource.getLocalUri());
                        resource.setStatus(Resource.UploadStatus.CANCELLED);
                    }
                } else {
                    resource = ResourceRepo.getInstance().resourceFailed(requestId, error.getCode(), error.getDescription());
                }

                sendBroadcast(resource);
            }
        });

        cancelNotification(requestId);

        int id = idsProvider.incrementAndGet();
        requestIdsToNotificationIds.put(requestId, id);

        notificationManager.notify(id,
                getBuilder(requestId, Resource.UploadStatus.FAILED)
                        .setContentTitle("Error uploading.")
                        .setContentText(error.getDescription())
                        .setStyle(new Notification.BigTextStyle()
                                .setBigContentTitle("Error uploading.")
                                .bigText(error.getDescription()))
                        .build());

        cleanupBitmap(requestId);
    }

    @Override
    public void onReschedule(final String requestId, final ErrorInfo error) {
        backgroundThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                sendBroadcast(ResourceRepo.getInstance().resourceRescheduled(requestId, error.getCode(), error.getDescription()));
            }
        });
        cancelNotification(requestId);
        int id = idsProvider.incrementAndGet();
        requestIdsToNotificationIds.put(requestId, id);
        notificationManager.notify(id,
                getBuilder(requestId, Resource.UploadStatus.RESCHEDULED)
                        .setContentTitle("Connection issues")
                        .setContentText("The upload will resume once network is available.")
                        .build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backgroundThreadHandler.removeCallbacksAndMessages(null);
    }

    private static final class BitmapResult {
        final Bitmap bitmap;

        private BitmapResult(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }
}