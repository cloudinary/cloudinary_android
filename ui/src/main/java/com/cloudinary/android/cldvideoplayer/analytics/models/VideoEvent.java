package com.cloudinary.android.cldvideoplayer.analytics.models;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class VideoEvent {
    public final TrackingType trackingType;
    public final String eventName;
    public final int eventTime;
    public final HashMap<String, Object> eventDetails;

    public VideoEvent(TrackingType trackingType, String eventName, HashMap<String, Object> eventDetails) {
        this.trackingType = trackingType;
        this.eventName = eventName;
        this.eventTime = (int) (System.currentTimeMillis() / 1000);
        this.eventDetails = eventDetails != null ? eventDetails : new HashMap<>();
        this.eventDetails.put(VideoEventJSONKeys.VIDEO_PLAYER.getValue(), createVideoPlayerObject());
    }

    private Map<String, Object> createVideoPlayerObject() {
        Map<String, Object> videoPlayer = new HashMap<>();
        videoPlayer.put(VideoEventJSONKeys.TYPE.getValue(), "android_player");
        videoPlayer.put(VideoEventJSONKeys.VERSION.getValue(), MediaManager.VERSION);
        return videoPlayer;
    }

    protected Map<String, Object> createCustomerData(Map<String, String> trackingData, Map<String, Object> providedData) {
        Map<String, Object> customerData = new HashMap<>();
        Map<String, Object> videoData = new HashMap<>();
        String cloudName = "";
        String publicId = "";
        if(trackingData != null) {
            cloudName = trackingData.containsKey(VideoEventJSONKeys.CLOUD_NAME.getValue()) ?
                    trackingData.get(VideoEventJSONKeys.CLOUD_NAME.getValue()) : "";
            publicId = trackingData.containsKey(VideoEventJSONKeys.PUBLIC_ID.getValue()) ?
                    trackingData.get(VideoEventJSONKeys.PUBLIC_ID.getValue()) : "";
        }
        videoData.put(VideoEventJSONKeys.CLOUD_NAME.getValue(), cloudName);
        videoData.put(VideoEventJSONKeys.PUBLIC_ID.getValue(), publicId);
        customerData.put(VideoEventJSONKeys.VIDEO_DATA.getValue(), videoData);
        customerData.put(VideoEventJSONKeys.PROVIDED_DATA.getValue(), providedData);
        return customerData;
    }
}

