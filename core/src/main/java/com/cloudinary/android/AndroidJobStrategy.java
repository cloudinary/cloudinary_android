package com.cloudinary.android;

import android.content.Context;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cloudinary.android.callback.UploadStatus;
import com.cloudinary.android.policy.UploadPolicy;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AndroidJobStrategy implements BackgroundRequestStrategy {

    private static final Map<String, WeakReference<Thread>> threads = new ConcurrentHashMap<>();
    private static final Object threadsMapLockObject = new Object();

    public static WorkRequest adapt(UploadRequest request) {
        UploadPolicy policy = request.getUploadPolicy();

        Constraints constraints = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            constraints = new Constraints.Builder()
                    .setRequiredNetworkType(adaptNetworkType(policy.getNetworkType()))
                    .setRequiresCharging(policy.isRequiresCharging())
                    .setRequiresDeviceIdle(policy.isRequiresIdle())
                    .setRequiresCharging(true)
                    .build();
        } else {
            constraints = new Constraints.Builder()
                    .setRequiredNetworkType(adaptNetworkType(policy.getNetworkType()))
                    .setRequiresCharging(policy.isRequiresCharging())
                    .setRequiresCharging(true)
                    .build();
        }

        Data inputData = request.buildPayload();

        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(UploadJob.class)
                .setBackoffCriteria(adaptBackoffPolicy(policy.getBackoffPolicy()),policy.getBackoffMillis(), TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build();
        return uploadWorkRequest;
    }

    private static BackoffPolicy adaptBackoffPolicy(UploadPolicy.BackoffPolicy backoffPolicy) {
        switch (backoffPolicy) {
            case LINEAR:
                return BackoffPolicy.LINEAR;
            case EXPONENTIAL:
            default:
                return BackoffPolicy.EXPONENTIAL;
        }
    }

    private static NetworkType adaptNetworkType(UploadPolicy.NetworkType networkType) {
        switch (networkType) {
            case NONE:
                return NetworkType.NOT_REQUIRED;
            case ANY:
                return NetworkType.CONNECTED;
            case UNMETERED:
                return NetworkType.UNMETERED;
        }

        return NetworkType.NOT_REQUIRED;
    }

    @Override
    public void init(Context context) {

    }

    @Override
    public void doDispatch(UploadRequest request) {
        WorkRequest uploadWorkRequest = adapt(request);
        WorkManager.getInstance().enqueue(uploadWorkRequest);
    }

    @Override
    public void executeRequestsNow(int howMany) {

    }

    @Override
    public boolean cancelRequest(String requestId) {
        return false;
    }

    @Override
    public int cancelAllRequests() {
        return 0;
    }

    @Override
    public int getPendingImmediateJobsCount() {
        return 0;
    }

    @Override
    public int getRunningJobsCount() {
        return 0;
    }

    public static final class UploadJob extends Worker {

        private Context context;
        private String requestId;
        private WorkerParameters workParams;

        public UploadJob(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            this.context = context;
            this.workParams = workerParams;
        }

        @NonNull
        @Override
        public Result doWork() {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CLD:UPLOADER");
            requestId = workParams.getInputData().getString("requestId");
            registerThread();

            wl.acquire();
            try {

                // call the generic processor:
                UploadStatus result = MediaManager.get().processRequest(context, new AndroidJobStrategy.AndroidJobRequestParams(workParams.getInputData()));
                return adaptResult(result);
            } finally {
                wl.release();
                unregisterThread();
            }
        }

        private void registerThread() {
            synchronized (threadsMapLockObject) {
                threads.put(requestId, new WeakReference<>(Thread.currentThread()));
            }
        }

        private void unregisterThread() {
            synchronized (threadsMapLockObject) {
                WeakReference<Thread> removed = threads.remove(requestId);
                if (removed != null) {
                    removed.clear();
                }
            }
        }

        @NonNull
        private Result adaptResult(UploadStatus res) {
            switch (res) {
                case FAILURE:
                    return Result.failure();
                case SUCCESS:
                    return Result.success();
                case RESCHEDULE:
                    return Result.retry();
            }
            // unexpected result, we don't want to retry because we have no idea why it failed.
            return Result.failure();
        }
    }

    private static final class AndroidJobRequestParams implements RequestParams {
        private final Data bundle;

        private AndroidJobRequestParams(Data bundle) {
            this.bundle = bundle;
        }

        @Override
        public void putString(String key, String value) {
            new Data.Builder().putAll(bundle).putString(key, value).build();
        }

        @Override
        public void putInt(String key, int value) {
            new Data.Builder().putAll(bundle).putInt(key, value).build();
        }

        @Override
        public void putLong(String key, long value) {
            new Data.Builder().putAll(bundle).putLong(key, value).build();
        }

        @Override
        public void putBoolean(String key, boolean value) {
            new Data.Builder().putAll(bundle).putBoolean(key, value).build();
        }

        @Override
        public String getString(String key, String defaultValue) {
            return bundle.getString(key);
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return bundle.getInt(key, defaultValue);
        }

        @Override
        public long getLong(String key, long defaultValue) {
            return bundle.getLong(key, defaultValue);
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            return bundle.getBoolean(key, defaultValue);
        }
    }
}
