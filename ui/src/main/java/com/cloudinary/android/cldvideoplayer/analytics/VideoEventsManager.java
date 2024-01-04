package com.cloudinary.android.cldvideoplayer.analytics;
import android.content.Context;
import android.content.SharedPreferences;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.cldvideoplayer.analytics.models.TrackingType;
import com.cloudinary.android.cldvideoplayer.analytics.models.VideoEvent;
import com.cloudinary.android.cldvideoplayer.analytics.models.VideoEventJSONKeys;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoLoadMetadata;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoPauseEvent;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoPlayEvent;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoViewEnd;
import com.cloudinary.android.cldvideoplayer.analytics.models.events.VideoViewStartEvent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoEventsManager {
    private final String CLD_ANALYTICS_ENDPOINT_PRODUCTION_URL = "https://video-analytics-api.cloudinary.com/v1/video-analytics";
    private static String CLD_ANALYTICS_ENDPOINT_DEVELOPMENT_URL = "http://10.0.2.2:3001/events";

    private String viewId;
    private String userId;
    public TrackingType trackingType = TrackingType.AUTO;
    public String cloudName;
    public String publicId;

    private SharedPreferences sharedPreferences;

    public final List<VideoEvent> eventQueue = new ArrayList<>();

    public VideoEventsManager(Context context) {
        viewId = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
        sharedPreferences = context.getSharedPreferences("CldVideoPlayerRefs", Context.MODE_PRIVATE);
        userId = getUserId();
        cloudName = MediaManager.get().getCloudinary().config.cloudName;
    }

    public String getUserId() {
        String storedUserId = sharedPreferences.getString("CLDVideoPlayerUserId", null);

        if (storedUserId != null) {
            return storedUserId;
        } else {
            String newUserId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("CLDVideoPlayerUserId", newUserId);
            editor.apply();
            return newUserId;
        }
    }

    public void sendViewStartEvent(String videoUrl, Map<String, Object> providedData) {
        Map<String, String> trackingData = new HashMap<>();
        trackingData.put("cloudName", cloudName != null ? cloudName : "");
        trackingData.put("publicId", publicId != null ? publicId : "");
        VideoViewStartEvent event = new VideoViewStartEvent(trackingType, videoUrl, trackingData, providedData);
        addEventToQueue(event);
    }

    public void sendViewEndEvent(Map<String, Object> providedData) {
        VideoViewEnd event = new VideoViewEnd(trackingType, providedData);
        addEventToQueue(event);
    }

    public void sendLoadMetadataEvent(int duration, Map<String, Object> providedData) {
        VideoLoadMetadata event = new VideoLoadMetadata(trackingType, duration, providedData);
        addEventToQueue(event);
    }

    public void sendPlayEvent(Map<String, Object> providedData) {
        VideoPlayEvent event = new VideoPlayEvent(trackingType, providedData);
        addEventToQueue(event);
    }

    public void sendPauseEvent(Map<String, Object> providedData) {
        VideoPauseEvent event = new VideoPauseEvent(trackingType, providedData);
        addEventToQueue(event);
    }

    private void addEventToQueue(VideoEvent event) {
        eventQueue.add(event);
    }

    public void sendEvents() {
        if (eventQueue.isEmpty()) {
            return;
        }
        List<VideoEvent> eventsToSend = new ArrayList<>(eventQueue);
        sendEventToEndpoint(eventsToSend);
        eventQueue.removeAll(eventsToSend);
    }

    public void sendEventToEndpoint(final List<VideoEvent> childEvents) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String boundary = "Boundary-" + UUID.randomUUID().toString().toUpperCase();
                URL url = new URL(CLD_ANALYTICS_ENDPOINT_PRODUCTION_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                Map<String, List<String>> requestHeaders = urlConnection.getRequestProperties();
                for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    for (String value : values) {
                        System.out.println(key + ": " + value);
                    }
                }
                String formData = buildFormData(boundary, childEvents, viewId, userId);
                try (OutputStream outputStream = urlConnection.getOutputStream()) {
                    outputStream.write(formData.getBytes());
                }

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Success: " + responseCode);
                } else {
                    // Error handling for other response codes
                    System.out.println("Error: " + responseCode);
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executor.shutdown();
    }

    private String buildFormData(String boundary, List<VideoEvent> events, String viewId, String userId) {
        StringBuilder formData = new StringBuilder();

        try {
            formData.append(buildFormDataPart(boundary, "userId", userId));
            formData.append(buildFormDataPart(boundary, "viewId", viewId));
            formData.append(buildFormDataPart(boundary, "events", buildEventsJson(events)));
            formData.append("--").append(boundary).append("--\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return formData.toString();
    }

    private String buildFormDataPart(String boundary, String name, String value) {
        StringBuilder formData = new StringBuilder();
        formData.append("--").append(boundary).append("\r\n");
        formData.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n");
        formData.append(value).append("\r\n");
        return formData.toString();
    }

    private String buildEventsJson(List<VideoEvent> events) {
        List<String> eventObjects = new ArrayList<>();

        for (VideoEvent event : events) {
            Map<String, Object> eventDetails = new HashMap<>();
            eventDetails.put(VideoEventJSONKeys.EVENT_NAME.getValue(), event.eventName);
            eventDetails.put(VideoEventJSONKeys.EVENT_TIME.getValue(), event.eventTime);
            eventDetails.put(VideoEventJSONKeys.EVENT_DETAILS.getValue(), event.eventDetails);

            // Convert the event details map to a JSON object string
            String eventJson = mapToJson(eventDetails);
            eventObjects.add(eventJson);
        }

        // Join the event objects into a JSON array
        return "[" + String.join(",", eventObjects) + "]";
    }

    // Helper method to convert a Map to a JSON object string
    private String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (json.length() > 1) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeString((String) value)).append("\"");
            } else if (value instanceof Map) {
                json.append(mapToJson((Map<String, Object>) value)); // No wrapping quotes for nested maps
            } else {
                json.append(value);
            }
        }

        json.append("}");
        return json.toString();
    }

    private String escapeString(String value) {
        // Implement your own logic to escape special characters in a string
        // For example, replacing double quotes with escaped quotes
        return value.replace("\"", "\\\"");
    }
}
