package com.cloudinary.android;

import android.content.Context;
import android.content.res.Resources;

import com.cloudinary.Cloudinary;
import com.cloudinary.ProgressCallback;
import com.cloudinary.android.callback.UploadStatus;
import com.cloudinary.android.payload.NotFoundException;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadFactory;
import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.cloudinary.Uploader.BUFFER_SIZE;
import static com.cloudinary.android.callback.UploadStatus.FAILURE;
import static com.cloudinary.android.callback.UploadStatus.RESCHEDULE;
import static com.cloudinary.android.callback.UploadStatus.SUCCESS;

/**
 * {@inheritDoc}
 */
class DefaultRequestProcessor implements RequestProcessor {
    private static final String TAG = "DefaultRequestProcessor";
    private final CallbackDispatcher callbackDispatcher;
    private AtomicInteger runningJobs = new AtomicInteger(0);

    DefaultRequestProcessor(CallbackDispatcher callbackDispatcher) {
        this.callbackDispatcher = callbackDispatcher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UploadStatus processRequest(Context context, RequestParams params) {

        final String requestId = params.getString("requestId", null);
        final String uri = params.getString("uri", null);
        final String optionsAsString = params.getString("options", null);
        final int maxErrorRetries = params.getInt("maxErrorRetries", CldAndroid.get().getGlobalUploadPolicy().getMaxErrorRetries());
        final int errorCount = params.getInt("errorCount", 0);

        Logger.i(TAG, String.format("Processing Request %s.", requestId));

        if (errorCount > maxErrorRetries) {
            Logger.d(TAG, String.format("Failing request %s, too many errors: %d, max: %d", requestId, errorCount, maxErrorRetries));
            return FAILURE;
        }

        callbackDispatcher.dispatchStart(requestId);
        callbackDispatcher.wakeListenerServiceWithRequestStart(context, requestId);

        UploadStatus requestResultStatus;
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

        String error = "Unknown error.";

        if (optionsLoadedSuccessfully) {
            if (StringUtils.isNotBlank(uri)) {
                Payload payload = PayloadFactory.fromUri(uri);
                if (payload != null) {
                    int maxConcurrentRequests = CldAndroid.get().getGlobalUploadPolicy().getMaxConcurrentRequests();
                    int runningJobsCount = runningJobs.get();
                    if (runningJobsCount < maxConcurrentRequests) {
                        try {
                            runningJobs.incrementAndGet();
                            resultData = doProcess(requestId, appContext, options, params, payload);
                            requestResultStatus = SUCCESS;
                        } catch (NotFoundException e) {
                            Logger.e(TAG, String.format("NotFoundException for request %s.", requestId), e);
                            requestResultStatus = FAILURE;
                            error = "The requested file does not exist."; // REVIEW messages. consider const or resources
                        } catch (Resources.NotFoundException e) {
                            Logger.e(TAG, String.format("Resources.NotFoundException for request %s.", requestId), e);
                            requestResultStatus = FAILURE;
                            error = "The requested file does not exist.";
                        } catch (FileNotFoundException e) {
                            Logger.e(TAG, String.format("FileNotFoundException for request %s.", requestId), e);
                            requestResultStatus = FAILURE;
                            error = "The requested file does not exist.";
                        } catch (ErrorRetrievingSignatureException e) {
                            Logger.e(TAG, String.format("Error retrieving signature for request %s.", requestId), e);
                            requestResultStatus = FAILURE;
                            error = e.getMessage();
                        } catch (IOException e) {
                            Logger.e(TAG, String.format("IOException for request %s.", requestId), e);
                            error = String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
                            requestResultStatus = RESCHEDULE;
                        } catch (Exception e) {
                            Logger.e(TAG, String.format("Unexpected exception for request %s.", requestId), e);
                            error = String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
                            requestResultStatus = FAILURE;
                        } finally {
                            runningJobs.decrementAndGet();
                        }
                    } else {
                        Logger.d(TAG, String.format("Rescheduling request %s, too many running jobs: %d, max: %d", requestId, runningJobsCount, maxConcurrentRequests));
                        requestResultStatus = RESCHEDULE;
                    }
                } else {
                    Logger.d(TAG, String.format("Failing request %s, payload cannot be loaded.", requestId));
                    error = "Request payload could not be loaded.";
                    requestResultStatus = FAILURE;
                }
            } else {
                requestResultStatus = FAILURE;
                error = "Request Uri is empty.";
                Logger.d(TAG, String.format("Failing request %s, Uri is empty.", requestId));
            }
        } else {
            requestResultStatus = FAILURE;
            error = "Cloud not load request options.";
            Logger.d(TAG, String.format("Failing request %s, cannot load options.", requestId));
        }

        if (requestResultStatus.isFinal()) {
            if (requestResultStatus == SUCCESS) {
                callbackDispatcher.dispatchSuccess(context, requestId, resultData);
            } else {
                callbackDispatcher.dispatchError(context, requestId, error);
            }

            // wake up the listener (this is not needed for reschedules since if the listener is down no point in reschedule notification.
            // we'll wake them up once the request is actually finished.
            callbackDispatcher.wakeListenerServiceWithRequestFinished(context, requestId, requestResultStatus);

        } else {
            callbackDispatcher.dispatchReschedule(context, requestId, error);
        }

        Logger.i(TAG, String.format("Finished processing request %s, result: %s", requestId, requestResultStatus));

        return requestResultStatus;
    }

    private Map doProcess(final String requestId, Context appContext, Map<String, Object> options, RequestParams params, Payload payload) throws NotFoundException, IOException, ErrorRetrievingSignatureException {
        Logger.d(TAG, String.format("Starting upload for request %s", requestId));
        final long actualTotalBytes = payload.getLength(appContext);
        final long offset = params.getLong("offset", 0);
        final int bufferSize;
        final String uploadUniqueId;
        int defaultBufferSize = options.containsKey("chunk_size") ? (int) options.get("chunk_size") : BUFFER_SIZE;
        if (offset > 0) {
            // this is a RESUME operation, buffer size needs to be consistent with previous parts:
            bufferSize = params.getInt("original_buffer_size", defaultBufferSize);
            uploadUniqueId = params.getString("original_upload_id", null);
        } else {
            bufferSize = ObjectUtils.asInteger(options.get("chunk_size"), defaultBufferSize);
            uploadUniqueId = new Cloudinary().randomPublicId();
        }

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
                    throw new ErrorRetrievingSignatureException("Could not retrieve signature from the given provider: " + signatureProvider.getName(), e);
                }
            }
        }

        final ProcessorCallback processorCallback = new ProcessorCallback(actualTotalBytes, offset, callbackDispatcher, requestId);

        try {
            return CldAndroid.get().getCloudinary().uploader().uploadLarge(payload.prepare(appContext), options, bufferSize, offset, uploadUniqueId, processorCallback);
        } finally {
            // save data into persisted request params to enable resuming later on
            params.putInt("original_buffer_size", bufferSize);
            params.putLong("offset", processorCallback.bytesUploaded - processorCallback.bytesUploaded % bufferSize);
            params.putString("original_upload_id", uploadUniqueId);
        }
    }

    private static final class ProcessorCallback implements ProgressCallback {
        final long notifyThrottlingStepSize;
        private final CallbackDispatcher dispatcher;
        long bytesNotified;
        long bytesUploaded;
        long totalBytes;
        String requestId;

        ProcessorCallback(long totalBytes, long offset, CallbackDispatcher dispatcher, final String requestId) {
            // calculate step size for progress - prevent flooding the user with callbacks.
            this.notifyThrottlingStepSize = totalBytes > 0 ? totalBytes / 100 : 500 * 1024 * 1024;
            this.totalBytes = totalBytes;
            this.bytesNotified = offset;
            this.bytesUploaded = offset;
            this.dispatcher = dispatcher;
            this.requestId = requestId;
        }

        @Override
        public void onProgress(long bytes, long totalBytes) {
            // throttle progress callbacks
            this.bytesUploaded = bytes;
            if (bytesNotified + notifyThrottlingStepSize < bytes || totalBytes == this.totalBytes) {
                bytesNotified += notifyThrottlingStepSize;
                dispatcher.dispatchProgress(requestId, bytes, this.totalBytes);
            }
        }
    }
}
