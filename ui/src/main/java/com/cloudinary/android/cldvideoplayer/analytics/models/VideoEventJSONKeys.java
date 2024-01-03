package com.cloudinary.android.cldvideoplayer.analytics.models;

public enum VideoEventJSONKeys {
    USER_ID("userId"),
    TRACKING_TYPE("trackingType"),
    VIEW_ID("viewId"),
    EVENTS("events"),
    EVENT_NAME("eventName"),
    EVENT_TIME("eventTime"),
    EVENT_DETAILS("eventDetails"),
    VIDEO_PLAYER("videoPlayer"),
    VIDEO_URL("videoUrl"),
    VIDEO_DURATION("videoDuration"),
    VIDEO_PUBLIC_ID("videoPublicId"),
    TRANSFORMATION("transformation"),
    VIDEO_EXTENSION("videoExtension"),
    CUSTOMER_DATA("customerData"),
    VIDEO_DATA("videoData"),
    PROVIDED_DATA("providedData"),
    CLOUD_NAME("cloudName"),
    PUBLIC_ID("publicId"),
    TYPE("type"),
    VERSION("version");

    private final String value;

    VideoEventJSONKeys(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
