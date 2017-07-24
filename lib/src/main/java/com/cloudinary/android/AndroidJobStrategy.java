package com.cloudinary.android;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import com.cloudinary.android.callback.UploadStatus;
import com.cloudinary.android.policy.UploadPolicy;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

/**
 * Background work strategy implementation based on com.evernote.android.job
 */
class AndroidJobStrategy implements BackgroundRequestStrategy {
    private static final String JOB_TAG = "CLD";
    private static final String TAG = AndroidJobStrategy.class.getSimpleName();

    static JobRequest adapt(UploadRequest request) {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        request.populateParamsFromFields(new AndroidJobRequestParams(extras));

        UploadPolicy policy = request.getUploadPolicy();

        return new JobRequest.Builder(JOB_TAG)
                .setExecutionWindow(request.getTimeWindow().getMinLatencyOffsetMillis(), request.getTimeWindow().getMaxExecutionDelayMillis())
                .setRequiredNetworkType(adaptNetworkType(policy.getNetworkType()))
                .setBackoffCriteria(policy.getBackoffMillis(), adaptPolicy(policy.getBackoffPolicy()))
                .setRequiresCharging(policy.isRequiresCharging())
                .setRequiresDeviceIdle(policy.isRequiresIdle())
                .setPersisted(true)
                .setExtras(extras)
                .setRequirementsEnforced(true)
                .build();
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
                builder.setExecutionWindow(10000, jobRequest.getEndMs()).build().schedule();
                started++;
            }

            if (started == howMany) {
                break;
            }
        }

        Logger.d(TAG, String.format("Job scheduled started %d requests.", started));
    }

    private boolean isSoonButNotImmediate(JobRequest jobRequest) {
        return jobRequest.getStartMs() < SOON_THRESHOLD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cancelRequest(String requestId) {
        boolean cancelled = false;
        for (JobRequest job : JobManager.instance().getAllJobRequests()) {
            String id = job.getExtras().getString("requestId", null);
            if (requestId.equals(id)) {
                cancelled = JobManager.instance().cancel(job.getJobId());
                break;
            }
        }

        Logger.i(TAG, String.format("Cancelling request %s, success: %s", requestId, cancelled));
        return cancelled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int cancelAllRequests() {
        Logger.i(TAG, "All requests cancelled.");
        return JobManager.instance().cancelAll();
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

        UploadJob() {
        }

        @NonNull
        @Override
        protected Result onRunJob(Params params) {
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CLD_UPLOADER");

            // Acquire wake readWriteLock - evernote android job only takes wakelock for 3 minutes and file uploads can take (much) longer.
            wl.acquire();
            try {
                // call the generic processor:
                UploadStatus result = CldAndroid.get().processRequest(getContext(), new AndroidJobRequestParams(params.getExtras()));
                return adaptResult(result);
            } finally {
                wl.release();
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
    }
}
