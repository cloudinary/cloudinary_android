package com.cloudinary.android.cldvideoplayer.analytics.models;

public enum PlayerKeyPath {
    STATUS("status"),
    TIME_CONTROL_STATUS("timeControlStatus"),
    DURATION("duration");

    private final String value;

    PlayerKeyPath(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
