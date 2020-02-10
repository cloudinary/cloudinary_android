package com.cloudinary.android;

import android.content.Context;
import androidx.annotation.NonNull;

import com.cloudinary.Cloudinary;
import com.cloudinary.ProgressCallback;
import com.cloudinary.Uploader;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadStatus;
import com.cloudinary.android.payload.EmptyByteArrayException;
import com.cloudinary.android.payload.FileNotFoundException;
import com.cloudinary.android.payload.LocalUriNotFoundException;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadFactory;
import com.cloudinary.android.payload.PayloadNotFoundException;
import com.cloudinary.android.payload.ResourceNotFoundException;
import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.cloudinary.android.callback.UploadStatus.FAILURE;
import static com.cloudinary.android.callback.UploadStatus.RESCHEDULE;
import static com.cloudinary.android.callback.UploadStatus.SUCCESS;
import static java.lang.Boolean.TRUE;

/**
 * {@inheritDoc}
 */
class DefaultRequestProcessor implements RequestProcessor {
    private static final String TAG = "DefaultRequestProcessor";
    public static final String ERROR_COUNT_PARAM = "errorCount";
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
        final int maxErrorRetries = params.getInt("maxErrorRetries", MediaManager.get().getGlobalUploadPolicy().getMaxErrorRetries());
        final int errorCount = params.getInt(ERROR_COUNT_PARAM, 0);
        final boolean isImmediate = params.getBoolean("immediate", false);
        Logger.i(TAG, String.format("Processing Request %s.", requestId));

        callbackDispatcher.dispatchStart(requestId);
        callbackDispatcher.wakeListenerServiceWithRequestStart(context, requestId);

        UploadStatus requestResultStatus;
        final Context appContext = context.getApplicationContext();

        Map resultData = null;
        boolean optionsLoadedSuccessfully = false;
        Map<String, Object> options = null;
        try {
            options = StringUtils.isBlank(optionsAsString) ? new HashMap<String, Object>() : UploadRequest.decodeOptions(optionsAsString);
            optionsLoadedSuccessfully = true;
        } catch (Exception e) {
            Logger.e(TAG, String.format("Request %s, error loading options.", requestId), e);
        }

        ErrorInfo error = null;

        if (optionsLoadedSuccessfully) {
            if (StringUtils.isNotBlank(uri)) {
                Payload payload = PayloadFactory.fromUri(uri);
                if (payload != null) {
                    try {
                        runningJobs.incrementAndGet();
                        resultData = doProcess(requestId, appContext, options, params, payload);
                        requestResultStatus = SUCCESS;
                    } catch (FileNotFoundException e) {
                        Logger.e(TAG, String.format("FileNotFoundException for request %s.", requestId), e);
                        requestResultStatus = FAILURE;
                        error = new ErrorInfo(ErrorInfo.FILE_DOES_NOT_EXIST, e.getMessage());
                    } catch (LocalUriNotFoundException e) {
                        Logger.e(TAG, String.format("LocalUriNotFoundException for request %s.", requestId), e);
                        requestResultStatus = FAILURE;
                        error = new ErrorInfo(ErrorInfo.URI_DOES_NOT_EXIST, e.getMessage());
                    } catch (ResourceNotFoundException e) {
                        Logger.e(TAG, String.format("ResourceNotFoundException for request %s.", requestId), e);
                        error = new ErrorInfo(ErrorInfo.RESOURCE_DOES_NOT_EXIST, e.getMessage());
                        requestResultStatus = FAILURE;
                    } catch (EmptyByteArrayException e) {
                        Logger.e(TAG, String.format("EmptyByteArrayException for request %s.", requestId), e);
                        requestResultStatus = FAILURE;
                        error = new ErrorInfo(ErrorInfo.BYTE_ARRAY_PAYLOAD_EMPTY, e.getMessage());
                    } catch (ErrorRetrievingSignatureException e) {
                        Logger.e(TAG, String.format("Error retrieving signature for request %s.", requestId), e);
                        requestResultStatus = FAILURE;
                        error = new ErrorInfo(ErrorInfo.SIGNATURE_FAILURE, e.getMessage());
                    } catch (IOException e) {
                        if (e instanceof InterruptedIOException && !(e instanceof SocketTimeoutException)) {
                            // The thread was forcibly aborted but not due to timeout => cancelled
                            // upload request:
                            Logger.e(TAG, String.format("InterruptedIO exception for request %s.", requestId), e);
                            error = new ErrorInfo(ErrorInfo.REQUEST_CANCELLED, "Request cancelled.");
                            requestResultStatus = FAILURE;
                        } else {
                            Logger.e(TAG, String.format("IOException for request %s.", requestId), e);

                            if (isImmediate) {
                                // Don't reschedule immediate requests
                                error = new ErrorInfo(ErrorInfo.NETWORK_ERROR, e.getMessage());
                                requestResultStatus = FAILURE;
                            } else if (errorCount >= maxErrorRetries) {
                                // failure
                                error = getMaxRetryError(errorCount);
                                requestResultStatus = FAILURE;
                            } else {
                                // one up error count and reschedule
                                params.putInt(ERROR_COUNT_PARAM, errorCount + 1);
                                error = new ErrorInfo(ErrorInfo.NETWORK_ERROR, e.getMessage());
                                requestResultStatus = RESCHEDULE;
                            }
                        }
                    } catch (Exception e) {
                        Logger.e(TAG, String.format("Unexpected exception for request %s.", requestId), e);
                        error = new ErrorInfo(ErrorInfo.UNKNOWN_ERROR, e.getMessage());
                        requestResultStatus = FAILURE;
                    } finally {
                        runningJobs.decrementAndGet();
                    }
                } else {
                    Logger.d(TAG, String.format("Failing request %s, payload cannot be loaded.", requestId));
                    error = new ErrorInfo(ErrorInfo.PAYLOAD_LOAD_FAILURE, "Request payload could not be loaded.");
                    requestResultStatus = FAILURE;
                }
            } else {
                requestResultStatus = FAILURE;
                error = new ErrorInfo(ErrorInfo.PAYLOAD_EMPTY, "Request payload is empty.");
                Logger.d(TAG, String.format("Failing request %s, Uri is empty.", requestId));
            }
        } else {
            requestResultStatus = FAILURE;
            error = new ErrorInfo(ErrorInfo.OPTIONS_FAILURE, "Options could not be loaded.");
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

    @NonNull
    private ErrorInfo getMaxRetryError(int errorCount) {
        ErrorInfo error;
        String message = String.format(Locale.getDefault(),
                "Request reached max retries allowed (%d).",
                errorCount);
        error = new ErrorInfo(ErrorInfo.TOO_MANY_ERRORS, message);
        return error;
    }

    private Map doProcess(final String requestId, Context
            appContext, Map<String, Object> options, RequestParams params, Payload payload) throws
            PayloadNotFoundException, IOException, ErrorRetrievingSignatureException {
        Logger.d(TAG, String.format("Starting upload for request %s", requestId));
        Object preparedPayload = payload.prepare(appContext);
        final long actualTotalBytes = payload.getLength(appContext);
        final long offset = params.getLong("offset", 0);
        final int bufferSize;
        final String uploadUniqueId;
        int defaultBufferSize = options.containsKey("chunk_size") ? (int) options.get("chunk_size") : Uploader.BUFFER_SIZE;
        if (offset > 0) {
            // this is a RESUME operation, buffer size needs to be consistent with previous parts:
            bufferSize = params.getInt("original_buffer_size", defaultBufferSize);
            uploadUniqueId = params.getString("original_upload_id", null);
        } else {
            bufferSize = ObjectUtils.asInteger(options.get("chunk_size"), defaultBufferSize);
            uploadUniqueId = new Cloudinary().randomPublicId();
        }

        // if there are no credentials and the request is NOT unsigned - activate the signature provider (if present).
        if (!MediaManager.get().hasCredentials() && !TRUE.equals(options.get("unsigned"))) {
            SignatureProvider signatureProvider = MediaManager.get().getSignatureProvider();
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
            return MediaManager.get().getCloudinary().uploader().uploadLarge(preparedPayload, options, bufferSize, offset, uploadUniqueId, processorCallback);
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
            this.notifyThrottlingStepSize = totalBytes > 0 ? totalBytes / 100 : 500 * 1024;
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
