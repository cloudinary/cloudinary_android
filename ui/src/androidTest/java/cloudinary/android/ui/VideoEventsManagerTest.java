package cloudinary.android.ui;


import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.cldvideoplayer.analytics.VideoEventsManager;
import com.cloudinary.android.cldvideoplayer.analytics.models.TrackingType;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoViewStartEvent;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class VideoEventsManagerTest {

    private static VideoEventsManager videoEventsManager;

    @BeforeClass
    public static void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        MediaManager.init(context);
        videoEventsManager = new VideoEventsManager(context);
        videoEventsManager.cloudName = "TestCloudName";
        videoEventsManager.publicId = "TestPublicId";
    }

    @Test
    public void getUserId_NewUserIdGenerated() {
        String userId = videoEventsManager.getUserId();
        // Verify that a new user ID is generated and stored in SharedPreferences
        assertEquals(32, userId.length()); // Assuming user ID length is 32 characters
    }

    @Test
    public void sendViewStartEvent_EventAddedToQueue() {
        // Create mock data
        String videoUrl = "https://example.com/video.mp4";
        Map<String, Object> providedData = new HashMap<>();
        providedData.put("key", "value");

        // Trigger the method
        videoEventsManager.sendViewStartEvent(videoUrl, providedData);

        // Verify that the event is added to the queue
        assertEquals(1, videoEventsManager.eventQueue.size());
    }

    @Test
    public void sendEvents_WithNonEmptyQueue_SendsEventsAndClearsQueue() {
        // Populate the eventQueue with some mock events
        VideoViewStartEvent mockEvent1 = new VideoViewStartEvent(TrackingType.AUTO, "videoUrl", new HashMap<>(), new HashMap<>());
        VideoViewStartEvent mockEvent2 = new VideoViewStartEvent(TrackingType.MANUAL, "videoUrl2", new HashMap<>(), new HashMap<>());
        videoEventsManager.eventQueue.add(mockEvent1);
        videoEventsManager.eventQueue.add(mockEvent2);

        int initialQueueSize = videoEventsManager.eventQueue.size();

        videoEventsManager.sendEvents();

        assertEquals(0, videoEventsManager.eventQueue.size());
    }
}