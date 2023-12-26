package com.cloudinary.android;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cloudinary.android.callback.UploadStatus;
import com.cloudinary.android.policy.UploadPolicy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AndroidJobStrategy implements BackgroundRequestStrategy {

    private static final String JOB_TAG = "CLD";

    private static final Map<String, WeakReference<Thread>> threads = new ConcurrentHashMap<>();
    private static final Object threadsMapLockObject = new Object();

    private Context context;

    public static OneTimeWorkRequest adapt(UploadRequest<?> request, File payloadFile) {
        UploadPolicy policy = request.getUploadPolicy();

        Constraints.Builder constraintsBuilder = new Constraints.Builder().setRequiredNetworkType(adaptNetworkType(policy.getNetworkType())).setRequiresCharging(policy.isRequiresCharging());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            constraintsBuilder.setRequiresDeviceIdle(policy.isRequiresIdle());
        }
        Constraints constraints = constraintsBuilder.build();

        Data inputData = request.buildPayload(payloadFile);

        return new OneTimeWorkRequest.Builder(UploadJob.class).setBackoffCriteria(adaptBackoffPolicy(policy.getBackoffPolicy()), policy.getBackoffMillis(), TimeUnit.MILLISECONDS).setInputData(inputData).setConstraints(constraints).addTag(request.getRequestId()).build();
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
        this.context = context;
    }

    @Override
    public void doDispatch(@SuppressWarnings("rawtypes") @NonNull UploadRequest request) {
        File cacheDir = context.getCacheDir();
        try {
            // Prepare payload file placeholder to temporarily store payload data.
            File payloadFile = File.createTempFile("payload", request.getRequestId(), cacheDir);
            OneTimeWorkRequest uploadWorkRequest = adapt(request, payloadFile);
            WorkManager.getInstance(context).beginUniqueWork(
                    // Use request ID as unique work name
                    request.getRequestId(),
                    // If work already exist, do nothing.
                    ExistingWorkPolicy.KEEP, uploadWorkRequest).enqueue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void executeRequestsNow(int howMany) {
        //Cant execute manually.
    }

    @Override
    public boolean cancelRequest(String requestId) {
        Operation operation = WorkManager.getInstance(context).cancelAllWorkByTag(requestId);
        killThread(requestId);
        return operation.getResult().isCancelled();
    }

    @Override
    public int cancelAllRequests() {
        WorkManager.getInstance(context).cancelAllWork();
        killAllThreads();
        return 0;
    }

    private void killThread(String requestId) {
        synchronized (threadsMapLockObject) {
            WeakReference<Thread> ref = threads.remove(requestId);
            if (ref != null) {
                Thread thread = ref.get();
                if (thread != null) {
                    thread.interrupt();
                }

                ref.clear();
            }
        }
    }

    private void killAllThreads() {
        synchronized (threadsMapLockObject) {
            for (String requestId : threads.keySet()) {
                WeakReference<Thread> ref = threads.get(requestId);
                Thread thread = ref.get();

                if (thread != null) {
                    thread.interrupt();
                }

                ref.clear();
            }

            threads.clear();
        }
    }

    @Override
    public int getPendingImmediateJobsCount() {
        return getJobCountByState(WorkInfo.State.ENQUEUED);
    }

    @Override
    public int getRunningJobsCount() {
        return getJobCountByState(WorkInfo.State.RUNNING);
    }

    private int getJobCountByState(WorkInfo.State state) {
        int counter = 0;
        List<WorkInfo> list = WorkManager.getInstance(context).getWorkInfosByTagLiveData(JOB_TAG).getValue();
        if (list != null) {
            for (WorkInfo info : list) {
                if (info.getState() == state) {
                    counter++;
                }
            }
            return counter;
        }
        return 0;
    }

    public static final class UploadJob extends Worker {

        private final Context context;
        private final WorkerParameters workParams;

        private String requestId;

        public UploadJob(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            this.context = context;
            this.workParams = workerParams;
        }

        @Override
        public void onStopped() {
            super.onStopped();
            unregisterThread(requestId);
        }

        private void registerThread(String requestId, Thread thread) {
            synchronized (threadsMapLockObject) {
                threads.put(requestId, new WeakReference<>(thread));
            }
        }

        private void unregisterThread(String requestId) {
            synchronized (threadsMapLockObject) {
                if(requestId != null) {
                    WeakReference<Thread> removed = threads.remove(requestId);
                    if (removed != null) {
                        removed.clear();
                    }
                }
            }
        }

        @NonNull
        @Override
        public Result doWork() {

            // Prepare extract payload data from temporary file.
            String payloadFilePath = workParams.getInputData().getString(UploadRequest.PayloadData.KEY);
            if (payloadFilePath == null) {
                // NO Payload input file created prior to request.
                return Result.failure();
            }

            File payloadFile = new File(payloadFilePath);
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(payloadFile))) {
                UploadRequest.PayloadData payloadData = (UploadRequest.PayloadData) ois.readObject();
                AndroidJobStrategy.AndroidJobRequestParams jobInputData = new AndroidJobStrategy.AndroidJobRequestParams(payloadData);
                requestId = payloadData.getRequestId();
                registerThread(requestId, Thread.currentThread());
                UploadStatus result = MediaManager.get().processRequest(context, jobInputData); // Replace this with your actual upload logic
                return adaptResult(result);
            } catch (NullPointerException | IOException | ClassNotFoundException e ) {
                // Unable to deserialize payload data from file.
                e.printStackTrace();
                return Result.failure();
            } finally {
                unregisterThread(requestId);
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
        private final Bundle data;

        private AndroidJobRequestParams(UploadRequest.PayloadData payloadData) {
            this.data = new Bundle();
            this.data.putString("uri", payloadData.getUri());
            this.data.putString("requestId", payloadData.getRequestId());
            this.data.putInt("maxErrorRetries", payloadData.getMaxErrorRetries());
            this.data.putString("options", payloadData.getOptions());
        }

        @Override
        public void putString(String key, String value) {
            data.putString(key, value);
        }

        @Override
        public void putInt(String key, int value) {
            data.putInt(key, value);
        }

        @Override
        public void putLong(String key, long value) {
            data.putLong(key, value);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            data.putBoolean(key, value);
        }

        @Override
        public String getString(String key, String defaultValue) {
            return (data.getString(key) != null) ? data.getString(key) : defaultValue;
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return data.getInt(key, defaultValue);
        }

        @Override
        public long getLong(String key, long defaultValue) {
            return data.getLong(key, defaultValue);
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            return data.getBoolean(key, defaultValue);
        }
    }
}
