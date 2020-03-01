package com.cloudinary.android.callback;

/**
 * Error object containing a technical description and an error code.
 */
public class ErrorInfo {
    public static final int NO_ERROR = 0;
    public static final int FILE_DOES_NOT_EXIST = 1;
    public static final int URI_DOES_NOT_EXIST = 2;
    public static final int RESOURCE_DOES_NOT_EXIST = 3;
    public static final int SIGNATURE_FAILURE = 4;
    public static final int NETWORK_ERROR = 5;
    public static final int UNKNOWN_ERROR = 6;
    public static final int PAYLOAD_LOAD_FAILURE = 7;
    public static final int PAYLOAD_EMPTY = 8;
    public static final int OPTIONS_FAILURE = 9;
    public static final int BYTE_ARRAY_PAYLOAD_EMPTY = 10;
    public static final int REQUEST_CANCELLED = 11;
    public static final int PREPROCESS_ERROR = 12;
    public static final int TOO_MANY_ERRORS = 13;

    private final int code;
    private final String description;

    public ErrorInfo(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Error code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Technical error description (Not for display purposes).
     */
    public String getDescription() {
        return description;
    }
}
