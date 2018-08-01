package com.cloudinary.android;

import android.content.Context;

import com.cloudinary.android.callback.ErrorInfo;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * {@inheritDoc}
 */
class DefaultRequestDispatcher implements RequestDispatcher {
    private static final String TAG = DefaultRequestDispatcher.class.getSimpleName();

    private final Random rand = new Random();
    private final BackgroundRequestStrategy strategy;
    private final ImmediateRequestsRunner immediateRequestsRunner;
    private final Set<String> abortedRequestIds = new HashSet<>();
    private final Object cancellationLock = new Object();


    DefaultRequestDispatcher(BackgroundRequestStrategy strategy, ImmediateRequestsRunner immediateRequestsRunner) {
        this.strategy = strategy;
        this.immediateRequestsRunner = immediateRequestsRunner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String dispatch(UploadRequest request) {
        String requestId = request.getRequestId();

        // If we are at max capacity and the request is not urgent defer this request by [10-20] minutes.
        // Requests will be started once there's more room
        int totalCount = strategy.getPendingImmediateJobsCount() + strategy.getRunningJobsCount();

        if (!request.getTimeWindow().isImmediate() && totalCount >= MediaManager.get().getGlobalUploadPolicy().getMaxConcurrentRequests()) {
            int minutes = 10 + rand.nextInt(10);
            request.defferByMinutes(minutes);
            Logger.d(TAG, String.format("Request %s deferred by %d minutes.", requestId, minutes));
        }

        Logger.d(TAG, String.format("Dispatching Request %s, scheduled start in %d minutes.", requestId, request.getTimeWindow().getMinLatencyOffsetMillis() / 60000));

        synchronized (cancellationLock) {
            if (abortedRequestIds.remove(requestId)) {
                MediaManager.get().dispatchRequestError(null, requestId, new ErrorInfo(ErrorInfo.REQUEST_CANCELLED, "Request cancelled"));
                return requestId;
            }

            strategy.doDispatch(request);
        }

        return requestId;
    }

    @Override
    public String startNow(Context context, UploadRequest request) {
        String requestId = request.getRequestId();
        synchronized (cancellationLock) {
            if (abortedRequestIds.remove(requestId)) {
                MediaManager.get().dispatchRequestError(null, requestId, new ErrorInfo(ErrorInfo.REQUEST_CANCELLED, "Request cancelled"));
                return requestId;
            }

            immediateRequestsRunner.runRequest(context, request);
        }

        return requestId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cancelRequest(String requestId) {
        synchronized (cancellationLock) {
            boolean cancelled = strategy.cancelRequest(requestId);
            if (!cancelled) {
                // request not dispatched yet (still preprocessing/preparing)
                abortedRequestIds.add(requestId);
            }

            return cancelled;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void queueRoomFreed() {
        int room = MediaManager.get().getGlobalUploadPolicy().getMaxConcurrentRequests() - strategy.getPendingImmediateJobsCount() - strategy.getRunningJobsCount();
        Logger.d(TAG, String.format("queueRoomFreed called, there's room for %d requests.", room));
        if (room > 0) {
            strategy.executeRequestsNow(room);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int cancelAllRequests() {
        return strategy.cancelAllRequests();
    }
}
