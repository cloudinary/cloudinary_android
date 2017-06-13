package com.cloudinary.android;

import java.util.Random;
import java.util.UUID;

class RequestDispatcher implements RequestDispatcherInterface {
    private static final String TAG = RequestDispatcher.class.getSimpleName();

    private final Random rand = new Random();
    private final BackgroundRequestStrategy strategy;

    RequestDispatcher(BackgroundRequestStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public final String dispatch(UploadRequest request) {
        String requestId = UUID.randomUUID().toString();

        // If we are at max capacity and the request is not urgent defer this request by [10-20] minutes.
        // Requests will be started once there's more room
        int totalCount = strategy.getPendingImmediateJobsCount() + strategy.getRunningJobsCount();
        if (!request.getTimeWindow().isImmediate() && totalCount >= CldAndroid.get().getGlobalUploadPolicy().getMaxConcurrentRequests()) {
            int minutes = 10 + rand.nextInt(10);
            request.defferByMinutes(minutes);
            Logger.d(TAG, String.format("Request %s deferred by %d minutes.", requestId, minutes));
        }

        Logger.d(TAG, String.format("Dispatching Request %s, scheduled start in %d minutes.", requestId, request.getTimeWindow().getMinLatencyOffsetMillis() / 60000));

        request.setRequestId(requestId);
        strategy.doDispatch(request);

        return requestId;
    }
}
