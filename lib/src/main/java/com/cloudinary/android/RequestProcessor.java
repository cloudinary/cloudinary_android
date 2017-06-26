package com.cloudinary.android;

import android.content.Context;
import android.content.res.Resources;

import com.cloudinary.ProgressCallback;
import com.cloudinary.android.payload.NotFoundException;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadFactory;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class RequestProcessor implements RequestProcessorInterface {
    private static final String TAG = "RequestProcessor";
    private final CallbackDispatcherInterface callbackDispatcher;
    private AtomicInteger runningJobs = new AtomicInteger(0);

    RequestProcessor(CallbackDispatcherInterface callbackDispatcher) {
        this.callbackDispatcher = callbackDispatcher;
    }

    public RequestResultStatus processRequest(Context context, ParamsAdaptable params) {

        final String requestId = params.getString("requestId", null);
        final String uri = params.getString("uri", null);
        final String optionsAsString = params.getString("options", null);
        final int maxErrorRetries = params.getInt("maxErrorRetries", CldAndroid.get().getGlobalUploadPolicy().getMaxErrorRetries());
        final int errorCount = params.getInt("errorCount", 0);

        Logger.i(TAG, String.format("Processing Request %s.", requestId));

        if (errorCount > maxErrorRetries) {
            Logger.d(TAG, String.format("Failing request %s, too many errors: %d, max: %d", requestId, errorCount, maxErrorRetries));
            return RequestResultStatus.FAILURE;
        }

        callbackDispatcher.dispatchStart(requestId);
        callbackDispatcher.wakeListenerServiceWithRequestStart(context, requestId);

        RequestResultStatus requestResultStatus;
        final Context appContext = context.getApplicationContext();

        Map resultData = null;
        boolean optionsLoadedSuccessfully = false;
        Map<String, Object> options = null;
        try {
            options = (Map<String, Object>) ObjectUtils.deserialize(optionsAsString);
            optionsLoadedSuccessfully = true;
        } catch (IOException e) {
            Logger.e(TAG, String.format("Request %s, error loading options.", requestId), e);
        } catch (ClassNotFoundException e) {
            Logger.e(TAG, String.format("Request %s, error loading options.", requestId), e);
        }

        String error = null;

        if (!optionsLoadedSuccessfully) {
            requestResultStatus = RequestResultStatus.FAILURE;
            error = "Cloud not load request options.";
            Logger.d(TAG, String.format("Failing request %s, cannot load options.", requestId));
        } else {
            if (StringUtils.isBlank(uri)) {
                requestResultStatus = RequestResultStatus.FAILURE;
                error = "Request Uri is empty.";
                Logger.d(TAG, String.format("Failing request %s, Uri is empty.", requestId));
            } else {
                Payload payload = PayloadFactory.fromUri(uri);
                if (payload == null) {
                    Logger.d(TAG, String.format("Failing request %s, payload cannot be loaded.", requestId));
                    error = "Request payload could not be loaded.";
                    requestResultStatus = RequestResultStatus.FAILURE;
                } else {
                    int maxConcurrentRequests = CldAndroid.get().getGlobalUploadPolicy().getMaxConcurrentRequests();
                    int runningJobsCount = runningJobs.get();
                    if (runningJobsCount >= maxConcurrentRequests) {
                        Logger.d(TAG, String.format("Rescheduling request %s, too many running jobs: %d, max: %d", requestId, runningJobsCount, maxConcurrentRequests));
                        return RequestResultStatus.RESCHEDULE;
                    } else {
                        try {
                            resultData = doProcess(requestId, appContext, options, payload);
                            requestResultStatus = RequestResultStatus.SUCCESS;
                        } catch (NotFoundException e) {
                            Logger.e(TAG, String.format("NotFoundException for request %s.", requestId), e);
                            requestResultStatus = RequestResultStatus.FAILURE;
                            error = "The requested file does not exist.";
                        } catch (Resources.NotFoundException e) {
                            Logger.e(TAG, String.format("Resources.NotFoundException for request %s.", requestId), e);
                            requestResultStatus = RequestResultStatus.FAILURE;
                            error = "The requested file does not exist.";
                        } catch (FileNotFoundException e) {
                            Logger.e(TAG, String.format("FileNotFoundException for request %s.", requestId), e);
                            requestResultStatus = RequestResultStatus.FAILURE;
                            error = "The requested file does not exist.";
                        } catch (IOException e) {
                            Logger.e(TAG, String.format("IOException for request %s.", requestId), e);
                            error = String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
                            requestResultStatus = RequestResultStatus.RESCHEDULE;
                        } catch (Exception e) {
                            Logger.e(TAG, String.format("Unexpected exception for request %s.", requestId), e);
                            error = String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
                            requestResultStatus = RequestResultStatus.FAILURE;
                        } finally {
                            runningJobs.decrementAndGet();
                        }
                    }
                }
            }
        }

        if (requestResultStatus.isFinal()) {
            if (requestResultStatus == RequestResultStatus.SUCCESS) {
                callbackDispatcher.dispatchSuccess(context, requestId, resultData);
            } else {
                callbackDispatcher.dispatchError(context, requestId, StringUtils.isEmpty(error) ? "Unknown error." : error);
            }

            // wake up the listener (this is not needed for reschedules since if the listener is down no point in reschedule notification.
            // we'll wake them up once the request is actually finished.
            callbackDispatcher.wakeListenerServiceWithRequestFinished(context, requestId, requestResultStatus);

        } else {
            callbackDispatcher.dispatchReschedule(context, requestId);
        }

        Logger.i(TAG, String.format("Finished processing request %s, result: ", requestId, requestResultStatus));

        return requestResultStatus;
    }

    private Map doProcess(final String requestId, Context appContext, Map<String, Object> options, Payload payload) throws NotFoundException, IOException {
        Logger.d(TAG, String.format("Starting upload for request %s", requestId));
        runningJobs.incrementAndGet();
        final long actualTotalBytes = payload.getLength(appContext);

        ProgressCallback progressCallback = new ProgressCallback() {
            // calculate step size for progress - prevent flooding the user with callbacks.
            long chunkSize = actualTotalBytes > 0 ? actualTotalBytes / 100 : 500 * 1024 * 1024;
            int totalNotified = 0;

            @Override
            public void onProgress(long bytes, long totalBytes) {
                // throttle progress callbacks
                if (totalNotified + chunkSize < bytes || totalBytes == actualTotalBytes) {
                    totalNotified += chunkSize;
                    callbackDispatcher.dispatchProgress(requestId, bytes, actualTotalBytes);
                }
            }
        };

        // check credentials/signature
        if (!CldAndroid.get().hasCredentials()) {
            SignatureProvider signatureProvider = CldAndroid.get().getSignatureProvider();
            if (signatureProvider != null) {
                try {
                    Signature signature = signatureProvider.provideSignature(options);
                    options.put("signature", signature.getSignature());
                    options.put("timestamp", signature.getTimestamp());
                    options.put("api_key", signature.getApiKey());
                } catch (Exception e) {
                    throw new ErrorRetrievingSignatureException("Could not retrieve signature from the given provider: " + signatureProvider.getClass().getSimpleName(), e);
                }
            }
        }

        return CldAndroid.get().getCloudinary().uploader().uploadLarge(payload.prepare(appContext), options, progressCallback);
    }
}
