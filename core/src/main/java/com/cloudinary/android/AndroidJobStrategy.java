package com.cloudinary.android;

import android.content.Context;
import android.os.PowerManager;
import androidx.annotation.NonNull;

import com.cloudinary.android.callback.UploadStatus;
import com.cloudinary.android.policy.UploadPolicy;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Background work strategy implementation based on com.evernote.android.job
 */
class AndroidJobStrategy implements BackgroundRequestStrategy {
    private static final String JOB_TAG = "CLD";
    private static final String TAG = AndroidJobStrategy.class.getSimpleName();
    private static final Map<String, WeakReference<Thread>> threads = new ConcurrentHashMap<>();
    private static final Object threadsMapLockObject = new Object();
    private static final int RUN_NOW_TIME_WINDOW_START = 10_000;
    private static final int RUN_NOW_TIME_WINDOW_END = 60_000;

    static JobRequest adapt(UploadRequest request) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        request.populateParamsFromFields(new AndroidJobRequestParams(extras));

        UploadPolicy policy = request.getUploadPolicy();

        JobRequest.Builder builder = new JobRequest.Builder(JOB_TAG)
                .setBackoffCriteria(policy.getBackoffMillis(), adaptPolicy(policy.getBackoffPolicy()))
                .setExtras(extras)
                .setExecutionWindow(request.getTimeWindow().getMinLatencyOffsetMillis(), request.getTimeWindow().getMaxExecutionDelayMillis())
                .setRequiredNetworkType(adaptNetworkType(policy.getNetworkType()))
                .setRequiresCharging(policy.isRequiresCharging())
                .setRequiresDeviceIdle(policy.isRequiresIdle())
                .setRequirementsEnforced(true);

        return builder.build();
    }

    private static JobRequest.BackoffPolicy adaptPolicy(UploadPolicy.BackoffPolicy backoffPolicy) {
        switch (backoffPolicy) {
            case LINEAR:
                return JobRequest.BackoffPolicy.LINEAR;
            case EXPONENTIAL:
            default:
                return JobRequest.BackoffPolicy.EXPONENTIAL;
        }
    }

    private static JobRequest.NetworkType adaptNetworkType(UploadPolicy.NetworkType networkType) {
        switch (networkType) {
            case NONE:
                return JobRequest.NetworkType.ANY;
            case ANY:
                return JobRequest.NetworkType.CONNECTED;
            case UNMETERED:
                return JobRequest.NetworkType.UNMETERED;
        }

        return JobRequest.NetworkType.ANY;
    }

    @NonNull
    private static Job.Result adaptResult(UploadStatus res) {
        switch (res) {
            case FAILURE:
                return Job.Result.FAILURE;
            case SUCCESS:
                return Job.Result.SUCCESS;
            case RESCHEDULE:
                return Job.Result.RESCHEDULE;
        }

        // unexpected result, we don't want to retry because we have no idea why it failed.
        return Job.Result.FAILURE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Context context) {
        JobManager.create(context).addJobCreator(new JobCreator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doDispatch(UploadRequest request) {
        JobRequest job = adapt(request);
        job.schedule();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeRequestsNow(int howMany) {
        int started = 0;
        for (JobRequest jobRequest : JobManager.instance().getAllJobRequests()) {
            if (isSoonButNotImmediate(jobRequest)) {
                JobRequest.Builder builder = jobRequest.cancelAndEdit();
                long endMillis = Math.max(jobRequest.getEndMs(), RUN_NOW_TIME_WINDOW_END);
                builder.setExecutionWindow(RUN_NOW_TIME_WINDOW_START, endMillis).build().schedule();
                started++;
            }

            if (started == howMany) {
                break;
            }
        }

        Logger.d(TAG, String.format("Job scheduled started %d requests.", started));
    }

    private boolean isSoonButNotImmediate(JobRequest jobRequest) {
        return IMMEDIATE_THRESHOLD < jobRequest.getStartMs() && jobRequest.getStartMs() < SOON_THRESHOLD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cancelRequest(String requestId) {
        boolean cancelled = false;

        for (Job job : JobManager.instance().getAllJobs()) {
            String currJobId = ((UploadJob) job).requestId;
            if (requestId.equals(currJobId)) {
                job.cancel();
                cancelled = true;
            }
        }

        if (!cancelled) {
            for (JobRequest job : JobManager.instance().getAllJobRequests()) {
                String id = job.getExtras().getString("requestId", null);
                if (requestId.equals(id)) {
                    cancelled = JobManager.instance().cancel(job.getJobId());
                    break;
                }
            }
        }

        killThread(requestId);

        Logger.i(TAG, String.format("Cancelling request %s, success: %s", requestId, cancelled));
        return cancelled;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int cancelAllRequests() {
        Logger.i(TAG, "All requests cancelled.");
        int count = JobManager.instance().cancelAll();
        killAllThreads();
        return count;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPendingImmediateJobsCount() {
        int pending = 0;
        for (JobRequest jobRequest : JobManager.instance().getAllJobRequests()) {
            if (isImmediate(jobRequest)) {
                pending++;
            }
        }

        return pending;
    }

    private boolean isImmediate(JobRequest jobRequest) {
        return jobRequest.getStartMs() < IMMEDIATE_THRESHOLD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRunningJobsCount() {
        int running = 0;
        for (Job job : JobManager.instance().getAllJobs()) {
            if (!job.isFinished()) {
                running++;
            }
        }

        return running;
    }

    private static class JobCreator implements com.evernote.android.job.JobCreator {

        @Override
        public Job create(String tag) {
            return new UploadJob();
        }
    }

    private static final class UploadJob extends Job {
        private String requestId;

        UploadJob() {
        }


        @NonNull
        @Override
        protected Result onRunJob(Params params) {
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CLD_UPLOADER");
            this.requestId = params.getExtras().getString("requestId", null);

            registerThread();

            // Acquire wake readWriteLock - evernote android job only takes wakelock for 3 minutes and file uploads can take (much) longer.
            wl.acquire();
            try {

                // call the generic processor:
                UploadStatus result = MediaManager.get().processRequest(getContext(), new AndroidJobRequestParams(params.getExtras()));
                return adaptResult(result);
            } finally {
                wl.release();
                unregisterThread();
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

        private void registerThread() {
            synchronized (threadsMapLockObject) {
                threads.put(requestId, new WeakReference<>(Thread.currentThread()));
            }
        }
    }

    private static final class AndroidJobRequestParams implements RequestParams {
        private final PersistableBundleCompat bundle;

        private AndroidJobRequestParams(PersistableBundleCompat bundle) {
            this.bundle = bundle;
        }

        @Override
        public void putString(String key, String value) {
            this.bundle.putString(key, value);
        }

        @Override
        public void putInt(String key, int value) {
            this.bundle.putInt(key, value);
        }

        @Override
        public void putLong(String key, long value) {
            this.bundle.putLong(key, value);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            this.bundle.putBoolean(key, value);
        }

        @Override
        public String getString(String key, String defaultValue) {
            return bundle.getString(key, defaultValue);
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
