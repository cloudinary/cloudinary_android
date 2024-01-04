package com.cloudinary.android.cldvideoplayer.analytics.models;

public enum EventNames {
    VIEW_START("viewStart"),
    VIEW_END("viewEnd"),
    LOAD_METADATA("loadMetadata"),
    PLAY("play"),
    PAUSE("pause");

    private final String value;

    EventNames(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
