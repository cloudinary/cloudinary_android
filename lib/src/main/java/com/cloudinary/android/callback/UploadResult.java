package com.cloudinary.android.callback;

import java.util.Map;

/**
 * This object contains the results of a single upload.
 * If the upload was successful the upload params will be available through {@link UploadResult#getSuccessResultData()} and {@link UploadResult#getError()} will be null.
 * If the upload encountered a fatal error (i.e. will not be rescheduled) there will be no data and {@link UploadResult#getError()} will return the error description.
 */
public class UploadResult {
    private final Map successResultData;
    private final ErrorInfo error;

    public UploadResult(Map successResultData, ErrorInfo error) {
        this.successResultData = successResultData;
        this.error = error;
    }

    /**
     * Upload result params. Null if the upload failed.
     */
    public Map getSuccessResultData() {
        return successResultData;
    }

    /**
     * Error object with a code and a technical description in case the upload failed. Otherwise null.
     */
    public ErrorInfo getError() {
        return error;
    }
}
