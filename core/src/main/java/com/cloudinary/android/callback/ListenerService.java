package com.cloudinary.android.callback;

import android.app.Service;
import android.content.Intent;

import com.cloudinary.android.MediaManager;
import com.cloudinary.utils.StringUtils;

/**
 * Build a service derived from this class to receive request callback when the application is in the background.
 * Note: The concrete service class should be registered in the manifest twice, once like any other service (under application/service tag)
 * and once for cloudinary metadata like so:
 * <pre>
 * {@code
 * <meta-data
 * android:name="cloudinaryCallbackService"
 * android:value="<fully.qualified.class.name>"/>
 * }
 * </pre>
 */
public abstract class ListenerService extends Service implements UploadCallback{
    private static final String TAG = "ListenerService";

    @Override
    public void onCreate() {
        super.onCreate();
        MediaManager.get().registerCallback(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String requestId = intent.getStringExtra(MediaManager.INTENT_EXTRA_REQUEST_ID);

            if (StringUtils.isNotBlank(requestId)) {
                if (MediaManager.ACTION_REQUEST_STARTED.equals(intent.getAction())){
                    onStart(requestId);
                } else if (MediaManager.ACTION_REQUEST_FINISHED.equals(intent.getAction())){
                    UploadStatus result = (UploadStatus) intent.getSerializableExtra(MediaManager.INTENT_EXTRA_REQUEST_RESULT_STATUS);
                    UploadResult uploadResult = MediaManager.get().popPendingResult(requestId);

                    // ACTION_REQUEST_FINISHED means either success or failure:
                    if (result == UploadStatus.FAILURE) {
                        onError(requestId, uploadResult.getError());
                    } else if (result == UploadStatus.SUCCESS) {
                        onSuccess(requestId, uploadResult.getSuccessResultData());
                    }
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MediaManager.get().unregisterCallback(this);
    }
}
