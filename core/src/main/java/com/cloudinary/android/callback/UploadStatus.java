package com.cloudinary.android.callback;

/**
 * Upload request result
 */
public enum UploadStatus {
    /**
     * The request encountered an unrecoverable error and will not retry.
     */
    FAILURE,
    /**
     * The requests completed successfully.
     */
    SUCCESS,
    /**
     * The request couldn't finish successfully due to temporary conditions (e.g. timeout, network disconnections), and will be rescheduled automatically.
     */
    RESCHEDULE;

    /**
     * True if this is expected to be the last status for a given request (i.e. no rescheduling of any kind)
     */
    public boolean isFinal() {
        return this == SUCCESS || this == FAILURE;
    }
}
