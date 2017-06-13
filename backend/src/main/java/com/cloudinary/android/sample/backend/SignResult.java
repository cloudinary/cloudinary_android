package com.cloudinary.android.sample.backend;

/**
 * The object model for the data we are sending through endpoints
 */
public class SignResult {
    private final String signature;
    private final long timestamp;

    public SignResult(String signature, long timestamp) {
        this.signature = signature;
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public long getTimestamp() {
        return timestamp;
    }
}