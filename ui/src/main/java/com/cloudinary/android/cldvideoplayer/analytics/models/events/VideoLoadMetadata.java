package com.cloudinary.android.cldvideoplayer.analytics.models.events;

import com.cloudinary.android.cldvideoplayer.analytics.models.EventNames;
import com.cloudinary.android.cldvideoplayer.analytics.models.TrackingType;
import com.cloudinary.android.cldvideoplayer.analytics.models.VideoEvent;
import com.cloudinary.android.cldvideoplayer.analytics.models.VideoEventJSONKeys;

import java.util.HashMap;
import java.util.Map;

public class VideoLoadMetadata extends VideoEvent {
    public VideoLoadMetadata(TrackingType trackingType, int duration, Map<String, Object> providedData) {
        super(trackingType, EventNames.LOAD_METADATA.getValue(), new HashMap<>());
        eventDetails.put(VideoEventJSONKeys.TRACKING_TYPE.getValue(), trackingType.getValue());
        eventDetails.put(VideoEventJSONKeys.VIDEO_DURATION.getValue(), duration);
        super.eventDetails.put(VideoEventJSONKeys.CUSTOMER_DATA.getValue(), createCustomerData(null, providedData));
    }
}
