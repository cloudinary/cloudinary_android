package com.cloudinary.android.cldvideoplayer.analytics.models.events;

import com.cloudinary.android.cldvideoplayer.analytics.models.EventNames;
import com.cloudinary.android.cldvideoplayer.analytics.models.TrackingType;
import com.cloudinary.android.cldvideoplayer.analytics.models.VideoEvent;
import com.cloudinary.android.cldvideoplayer.analytics.models.VideoEventJSONKeys;

import java.util.HashMap;
import java.util.Map;

public class VideoViewEnd extends VideoEvent {
    public VideoViewEnd(TrackingType trackingType, Map<String, Object> providedData) {
        super(trackingType, EventNames.VIEW_END.getValue(), new HashMap<>());
        eventDetails.put(VideoEventJSONKeys.TRACKING_TYPE.getValue(), trackingType.getValue());
        super.eventDetails.put(VideoEventJSONKeys.CUSTOMER_DATA.getValue(), createCustomerData(null, providedData));
    }
}
