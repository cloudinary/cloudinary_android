package com.cloudinary.android.cldvideoplayer.analytics.models;

public enum TrackingType {
    MANUAL("manual"),
    AUTO("auto");

    private final String value;

    TrackingType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
