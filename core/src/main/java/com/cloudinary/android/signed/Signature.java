package com.cloudinary.android.signed;

public class Signature {
    private final String signature;
    private final String apiKey;
    private final long timestamp;

    public Signature(String signature, String apiKey, long timestamp) {
        this.signature = signature;
        this.apiKey = apiKey;
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public String getApiKey() {
        return apiKey;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
