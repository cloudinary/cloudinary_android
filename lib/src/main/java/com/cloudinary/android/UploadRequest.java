package com.cloudinary.android;

import android.support.annotation.Nullable;

import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.policy.TimeWindow;
import com.cloudinary.android.policy.UploadPolicy;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A request to upload a single {@link Payload} to Cloudinary.
 * @param <T> The payload type this request will upload
 */
public class UploadRequest<T extends Payload> {
    private final UploadContext<T> uploadContext;
    private String requestId;
    private boolean dispatched = false;
    private UploadPolicy uploadPolicy = CldAndroid.get().getGlobalUploadPolicy();
    private TimeWindow timeWindow = TimeWindow.getDefault();
    private UploadCallback callback;
    private Map<String, Object> options;
    private String optionsAsString = null;

    UploadRequest(UploadContext<T> uploadContext) {
        this.uploadContext = uploadContext;
    }

    UploadRequest(UploadContext<T> uploadContext, @Nullable Map<String, Object> options) {
        this.uploadContext = uploadContext;
        this.options = options;
    }

    String getRequestId() {
        return requestId;
    }

    synchronized void setRequestId(String requestId) {
        if (StringUtils.isNotBlank(this.requestId)) {
            throw new IllegalStateException("Id already set");
        }
        this.requestId = requestId;
    }

    /**
     * Setup a callback to get notified on upload events.
     * @return This request for chaining.
     */
    public synchronized UploadRequest<T> callback(UploadCallback callback) {
        assertNotDispatched();
        this.callback = new DelegateCallback(callback);
        return this;
    }

    public synchronized UploadRequest<T> unsigned(String uploadPreset) {
        assertNotDispatched();
        verifyOptionsExist();
        options.put("unsigned", true);
        options.put("upload_preset", uploadPreset);
        return this;
    }

    /**
     * Constrain this request to run within a specific {@link TimeWindow}.
     * @return This request for chaining.
     */
    public synchronized UploadRequest<T> constrain(TimeWindow timeWindow) {
        assertNotDispatched();
        this.timeWindow = timeWindow;
        return this;
    }

    /**
     * Set a map of options for this request. Note: This replaces any existing options.
     * @return This request for chaining.
     */
    public synchronized UploadRequest<T> options(Map<String, Object> options) {
        this.options = options;
        return this;
    }

    /**
     * Add an option to this request.
     * @param name Option name.
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
     * @return The unique id of the request.
     */
    public synchronized String dispatch() {
        assertNotDispatched();
        verifyOptionsExist();
        this.dispatched = true;
        try {
            optionsAsString = ObjectUtils.serialize(options);
        } catch (IOException e) {
            throw new InvalidParamsException("Parameters must be serializable", e);
        }

        String requestId = uploadContext.getDispatcher().dispatch(this);
        CldAndroid.get().registerCallback(requestId, callback);
        return requestId;
    }

    private synchronized void verifyOptionsExist() {
        if (options == null) {
            options = new HashMap<>();
        }
    }

    T getPayload() {
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

    String getOptionsString() {
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
            CldAndroid.get().unregisterCallback(this);
        }

        @Override
        public void onError(String requestId, String error) {
            callback.onError(requestId, error);
            CldAndroid.get().unregisterCallback(this);
        }

        @Override
        public void onReschedule(String requestId, String errorMessage) {
            callback.onReschedule(requestId, errorMessage);
        }
    }
}
