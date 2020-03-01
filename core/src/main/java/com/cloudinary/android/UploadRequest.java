package com.cloudinary.android;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadNotFoundException;
import com.cloudinary.android.policy.TimeWindow;
import com.cloudinary.android.policy.UploadPolicy;
import com.cloudinary.android.preprocess.PayloadDecodeException;
import com.cloudinary.android.preprocess.PreprocessChain;
import com.cloudinary.android.preprocess.PreprocessException;
import com.cloudinary.android.preprocess.ResourceCreationException;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A request to upload a single {@link Payload} to Cloudinary. Note: Once calling {@link #dispatch()} the request is sealed and any
 * attempt to modify it will produce an {@link IllegalStateException}. If there's a need to change a request after dispatching,
 * it needs to be cancelled ({@link MediaManager#cancelRequest(String)}) and a new request should be dispatched in it's place.
 *
 * @param <T> The payload type this request will upload
 */
public class UploadRequest<T extends Payload> {
    private static final String TAG = UploadRequest.class.getSimpleName();

    private final UploadContext<T> uploadContext;
    private final Object optionsLockObject = new Object();
    private PreprocessChain preprocessChain;
    private String requestId = UUID.randomUUID().toString();
    private boolean dispatched = false;
    private UploadPolicy uploadPolicy = MediaManager.get().getGlobalUploadPolicy();
    private TimeWindow timeWindow = TimeWindow.getDefault();
    private UploadCallback callback;
    private Map<String, Object> options;
    private String optionsAsString = null;
    private Long maxFileSize;
    private boolean startNow = false;

    public UploadRequest(UploadContext<T> uploadContext) {
        this.uploadContext = uploadContext;
    }

    UploadRequest(UploadContext<T> uploadContext, @Nullable Map<String, Object> options) {
        this.uploadContext = uploadContext;
        this.options = options;
    }

    static String encodeOptions(Map<String, Object> options) throws IOException {
        return ObjectUtils.serialize(options);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> decodeOptions(String encoded) throws IOException, ClassNotFoundException {
        return (Map<String, Object>) ObjectUtils.deserialize(encoded);
    }


    /**
     * Setup a callback to get notified on upload events.
     *
     * @return This request for chaining.
     */
    public synchronized UploadRequest<T> callback(UploadCallback callback) {
        assertNotDispatched();
        this.callback = new DelegateCallback(callback);
        return this;
    }

    /**
     * Make this an unsigned upload
     *
     * @param uploadPreset The name of the upload preset to use, as defined in your cloudinary console
     * @return This request for chaining.
     */
    public synchronized UploadRequest<T> unsigned(String uploadPreset) {
        assertNotDispatched();
        verifyOptionsExist();
        options.put("unsigned", true);
        options.put("upload_preset", uploadPreset);
        return this;
    }

    /**
     * Fail the request is the file size is larger than this
     *
     * @param bytes Maximum allowed file size to upload
     * @return This request for chaining
     */
    public synchronized UploadRequest<T> maxFileSize(long bytes) {
        assertNotDispatched();
        this.maxFileSize = bytes;
        return this;
    }

    /**
     * Add a chain of preprocessing step to run on the resource before uploading
     *
     * @param preprocessChain Preprocess chain to run on the file before the upload
     * @return This request for chaining.
     */
    public synchronized UploadRequest<T> preprocess(PreprocessChain preprocessChain) {
        assertNotDispatched();
        this.preprocessChain = preprocessChain;
        return this;
    }

    /**
     * Constrain this request to run within a specific {@link TimeWindow}.
     *
     * @return This request for chaining.
     */
    public synchronized UploadRequest<T> constrain(TimeWindow timeWindow) {
        assertNotDispatched();
        this.timeWindow = timeWindow;
        return this;
    }

    /**
     * Set a map of options for this request. Note: This replaces any existing options.
     *
     * @return This request for chaining.
     */
    public synchronized UploadRequest<T> options(Map<String, Object> options) {
        assertNotDispatched();
        this.options = options;
        return this;
    }

    /**
     * Add an option to this request.
     *
     * @param name  Option name.
     * @param value Option value.
     * @return This request for chaining.
     */
    public synchronized UploadRequest<T> option(String name, Object value) {
        assertNotDispatched();
        verifyOptionsExist();
        options.put(name, value);
        return this;
    }

    /**
     * Set the upload uploadPolicy for the request
     *
     * @param policy The uploadPolicy to set. See {@link UploadPolicy.Builder}
     * @return This request for chaining.
     */
    public synchronized UploadRequest<T> policy(UploadPolicy policy) {
        assertNotDispatched();
        this.uploadPolicy = policy;
        return this;
    }

    /**
     * Dispatch the request
     *
     * @return The unique id of the request.
     */
    public synchronized String dispatch() {
        return dispatch(null);
    }

    /**
     * Start the request immediately, ignoring all other constraints.
     *
     * @param context Android context
     * @return The started request id.
     */
    public synchronized String startNow(@NonNull Context context) {
        startNow = true;
        return dispatch(context);
    }

    /**
     * Dispatch the request
     *
     * @param context Android context. Needed if using preprocessing.
     *                Otherwise can be null.
     * @return The unique id of the request.
     */
    public synchronized String dispatch(@Nullable final Context context) {
        assertNotDispatched();
        verifyOptionsExist();
        this.dispatched = true;
        serializeOptions();

        MediaManager.get().registerCallback(requestId, callback);

        final RequestDispatcher dispatcher = uploadContext.getDispatcher();
        boolean hasPreprocess = preprocessChain != null && !preprocessChain.isEmpty();
        if (!hasPreprocess && maxFileSize == null) {
            doDispatch(dispatcher, context, UploadRequest.this);
        } else {
            if (context == null) {
                throw new IllegalArgumentException("A valid android context must be supplied to UploadRequest.dispatch() when using preprocessing or setting maxFileSize");
            }

            MediaManager.get().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final UploadRequest newRequest = preprocessChain != null ?
                                preprocessAndClone(context) : UploadRequest.this;

                        long length = newRequest.getPayload().getLength(context);
                        if (maxFileSize != null && length > maxFileSize) {
                            MediaManager.get().dispatchRequestError(context, requestId, new ErrorInfo(ErrorInfo.PREPROCESS_ERROR, String.format("Payload size is too large, %d, max is %d", length, maxFileSize)));
                        } else {
                            doDispatch(dispatcher, context, newRequest);
                        }
                    } catch (RuntimeException e) {
                        Logger.e(TAG, "Error running preprocess for request", e);
                        MediaManager.get().dispatchRequestError(context, requestId, new ErrorInfo(ErrorInfo.PREPROCESS_ERROR, e.getClass().getSimpleName() + ": " + e.getMessage()));
                    } catch (PreprocessException e) {
                        MediaManager.get().dispatchRequestError(context, requestId, new ErrorInfo(ErrorInfo.PREPROCESS_ERROR, e.getClass().getSimpleName() + ": " + e.getMessage()));
                    } catch (PayloadNotFoundException e) {
                        MediaManager.get().dispatchRequestError(context, requestId, new ErrorInfo(ErrorInfo.PREPROCESS_ERROR, e.getClass().getSimpleName() + ": " + e.getMessage()));
                    }
                }
            });
        }

        return requestId;
    }

    private void doDispatch(RequestDispatcher dispatcher, @Nullable Context context, UploadRequest<T> uploadRequest) {
        if (startNow) {
            if (context == null) {
                throw new IllegalArgumentException("Context cannot be null when calling startNow()");
            }

            dispatcher.startNow(context, uploadRequest);

        } else {
            dispatcher.dispatch(uploadRequest);
        }
    }

    synchronized void serializeOptions() {
        try {
            optionsAsString = encodeOptions(options);
        } catch (IOException e) {
            throw new InvalidParamsException("Parameters must be serializable", e);
        }
    }

    /**
     * Run all the preprocessing steps on the request and replicate a new request, with a file payload
     * containing the processed resource.
     *
     * @param context Android context for preprocssing
     * @return A new request with the preprocessed resource
     * @throws PayloadNotFoundException
     * @throws PayloadDecodeException
     * @throws ResourceCreationException
     */
    private UploadRequest preprocessAndClone(Context context) throws PayloadNotFoundException, PreprocessException {
        String newFile = preprocessChain.execute(context, getPayload());
        UploadRequest<FilePayload> uploadRequest = new UploadRequest<>(new UploadContext<>(new FilePayload(newFile), getUploadContext().getDispatcher()));
        uploadRequest.uploadPolicy = uploadPolicy;
        uploadRequest.timeWindow = TimeWindow.getDefault();
        uploadRequest.callback = callback;
        uploadRequest.options = options;
        uploadRequest.optionsAsString = optionsAsString;
        uploadRequest.requestId = requestId;
        uploadRequest.dispatched = dispatched;

        return uploadRequest;
    }

    private void verifyOptionsExist() {
        if (options == null) {
            synchronized (optionsLockObject) {
                if (options == null) {
                    options = new HashMap<>();
                }
            }
        }
    }

    public String getRequestId() {
        return requestId;
    }

    public T getPayload() {
        return uploadContext.getPayload();
    }

    UploadCallback getCallback() {
        return callback;
    }

    UploadContext<T> getUploadContext() {
        return uploadContext;
    }

    TimeWindow getTimeWindow() {
        return timeWindow;
    }

    private void assertNotDispatched() {
        if (dispatched) {
            throw new IllegalStateException("Request already dispatched");
        }
    }

    private String getOptionsString() {
        return optionsAsString;
    }

    UploadPolicy getUploadPolicy() {
        return uploadPolicy;
    }

    void defferByMinutes(int minutes) {
        timeWindow = timeWindow.newDeferredWindow(minutes);
    }

    void populateParamsFromFields(RequestParams target) {
        target.putString("uri", getPayload().toUri());
        target.putString("requestId", getRequestId());
        target.putInt("maxErrorRetries", getUploadPolicy().getMaxErrorRetries());
        target.putString("options", getOptionsString());
    }

    /**
     * Wraps the delegate and unregisters the callback once a request is finished.
     */
    private static final class DelegateCallback implements UploadCallback {
        private final UploadCallback callback;

        DelegateCallback(UploadCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onStart(String requestId) {
            callback.onStart(requestId);
        }

        @Override
        public void onProgress(String requestId, long bytes, long totalBytes) {
            callback.onProgress(requestId, bytes, totalBytes);
        }

        @Override
        public void onSuccess(String requestId, Map resultData) {
            callback.onSuccess(requestId, resultData);
            MediaManager.get().unregisterCallback(this);
        }

        @Override
        public void onError(String requestId, ErrorInfo error) {
            callback.onError(requestId, error);
            MediaManager.get().unregisterCallback(this);
        }

        @Override
        public void onReschedule(String requestId, ErrorInfo error) {
            callback.onReschedule(requestId, error);
        }
    }
}
