package com.cloudinary.android;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

/**
 * Callback implementations that saved the last result
 */
final class StatefulCallback implements UploadCallback {

    ErrorInfo lastErrorObject = null;
    Map lastSuccess = null;
    ErrorInfo lastReschedule = null;

    @Override
    public void onStart(String requestId) {
    }

    @Override
    public void onProgress(String requestId, long bytes, long totalBytes) {
    }

    @Override
    public void onSuccess(String requestId, Map resultData) {
        this.lastSuccess = resultData;
        this.lastErrorObject = null;
    }

    @Override
    public void onError(String requestId, ErrorInfo errorObject) {
        this.lastErrorObject = errorObject;
    }

    public boolean hasResponse(){
        return lastErrorObject != null || lastSuccess != null || lastReschedule != null;
    }

    @Override
    public void onReschedule(String requestId, ErrorInfo error) {
        this.lastReschedule = error;
    }
}
