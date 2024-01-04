package cloudinary.android.ui;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import com.cloudinary.android.cldvideoplayer.analytics.models.EventNames;
import com.cloudinary.android.cldvideoplayer.analytics.models.TrackingType;
import com.cloudinary.android.cldvideoplayer.analytics.models.VideoEventJSONKeys;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoLoadMetadata;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoPauseEvent;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoPlayEvent;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoViewEnd;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoViewStartEvent;

public class VideoEventTests {

    @Test
    public void testVideoViewStartEventInitialization() {
        String videoUrl = "https://www.example.com/video.mp4";
        Map<String, String> trackingData = new HashMap<>();
        trackingData.put("cloudName", "exampleCloud");
        trackingData.put("publicId", "abc123");
        Map<String, Object> providedData = new HashMap<>();
        providedData.put("key", "value");

        VideoViewStartEvent event = new VideoViewStartEvent(TrackingType.AUTO, videoUrl, trackingData, providedData);

        assertEquals(event.eventName, EventNames.VIEW_START.getValue());
        assertEquals(event.eventDetails.get(VideoEventJSONKeys.TRACKING_TYPE.getValue()), TrackingType.AUTO.getValue());
        assertEquals(event.eventDetails.get(VideoEventJSONKeys.VIDEO_URL.getValue()), videoUrl);

        Map<String, Object> customerData = (Map<String, Object>) event.eventDetails.get(VideoEventJSONKeys.CUSTOMER_DATA.getValue());
        Map<String, Object> videoData = (Map<String, Object>) customerData.get(VideoEventJSONKeys.VIDEO_DATA.getValue());
        assertEquals(videoData.get(VideoEventJSONKeys.CLOUD_NAME.getValue()), trackingData.get("cloudName"));
        assertEquals(videoData.get(VideoEventJSONKeys.PUBLIC_ID.getValue()), trackingData.get("publicId"));

        Map<String, Object> providedDataObject = (Map<String, Object>) customerData.get(VideoEventJSONKeys.PROVIDED_DATA.getValue());
        assertEquals(providedDataObject.get("key"), providedData.get("key"));
    }

    @Test
    public void testVideoLoadMetadataEventInitialization() {
        int duration = 120;

        VideoLoadMetadata event = new VideoLoadMetadata(TrackingType.AUTO, duration, null);

        assertEquals(event.eventName, EventNames.LOAD_METADATA.getValue());
        assertEquals(event.eventDetails.get(VideoEventJSONKeys.TRACKING_TYPE.getValue()), TrackingType.AUTO.getValue());
        assertEquals(event.eventDetails.get(VideoEventJSONKeys.VIDEO_DURATION.getValue()), duration);
    }

    @Test
    public void testVideoViewEndEventInitialization() {
        Map<String, Object> providedData = new HashMap<>();
        providedData.put("key", "value");

        VideoViewEnd event = new VideoViewEnd(TrackingType.AUTO, providedData);

        assertEquals(event.eventName, EventNames.VIEW_END.getValue());
        assertEquals(event.eventDetails.get(VideoEventJSONKeys.TRACKING_TYPE.getValue()), TrackingType.AUTO.getValue());
        Map<String, Object> customerData = (Map<String, Object>) event.eventDetails.get(VideoEventJSONKeys.CUSTOMER_DATA.getValue());
        assertEquals(customerData.get(VideoEventJSONKeys.PROVIDED_DATA.getValue()), providedData);
    }

    @Test
    public void testVideoPlayEventInitialization() {
        Map<String, Object> providedData = new HashMap<>();
        providedData.put("key", "value");

        VideoPlayEvent event = new VideoPlayEvent(TrackingType.AUTO, providedData);

        assertEquals(event.eventName, EventNames.PLAY.getValue());
        Map<String, Object> customerData = (Map<String, Object>) event.eventDetails.get(VideoEventJSONKeys.CUSTOMER_DATA.getValue());
        assertEquals(customerData.get(VideoEventJSONKeys.PROVIDED_DATA.getValue()), providedData);
    }

    @Test
    public void testVideoPauseEventInitialization() {
        Map<String, Object> providedData = new HashMap<>();
        providedData.put("key", "value");

        VideoPauseEvent event = new VideoPauseEvent(TrackingType.AUTO, providedData);

        assertEquals(event.eventName, EventNames.PAUSE.getValue());
        Map<String, Object> customerData = (Map<String, Object>) event.eventDetails.get(VideoEventJSONKeys.CUSTOMER_DATA.getValue());
        assertEquals(customerData.get(VideoEventJSONKeys.PROVIDED_DATA.getValue()), providedData);
    }
}
