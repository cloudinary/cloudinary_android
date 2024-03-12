package com.cloudinary.android.cldvideoplayer;

import android.content.Context;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.cldvideoplayer.analytics.VideoEventsManager;
import com.cloudinary.android.cldvideoplayer.analytics.models.AnalyticsType;
import com.cloudinary.android.cldvideoplayer.analytics.models.TrackingType;

import java.net.URL;


public class CldVideoPlayer {

    ExoPlayer player;

    String url;

    VideoEventsManager videoEventsManager;

    boolean viewStartSent = false;
    private boolean analytics = true;

    public CldVideoPlayer(Context context, URL url) {
        this.url = url.toString();
        initPlayer(context, this.url);
    }

    public CldVideoPlayer(Context context, String publicId) {
        initiliaze(context, publicId, null, false);
    }
    public CldVideoPlayer(Context context, String publicId, Transformation transformation) {
        initiliaze(context, publicId, transformation, false);
    }

    public CldVideoPlayer(Context context, String publicId, Transformation transformation, Boolean automaticStreamingProfile) {
        initiliaze(context, publicId, transformation, automaticStreamingProfile);
    }

    public CldVideoPlayer(Context context, String publicId, Boolean automaticStreamingProfile) {
        initiliaze(context, publicId, null, automaticStreamingProfile);
    }

    private void initiliaze(Context context, String publicId, Transformation transformation, Boolean automaticStreamingProfile) {
        MediaManager.get().getCloudinary().analytics.setFeatureFlag("F");
        if (automaticStreamingProfile && transformation == null) {
            transformation = new Transformation();
            transformation.streamingProfile("auto");
            this.url = MediaManager.get().url().resourceType("video").transformation(transformation).format("m3u8").generate(publicId);
        } else {
            this.url = MediaManager.get().url().resourceType("video").transformation(transformation).generate(publicId);
        }
        MediaManager.get().getCloudinary().analytics.setFeatureFlag("0");
        initPlayer(context, url);
    }

    private void initPlayer(Context context, String url) {
        videoEventsManager = new VideoEventsManager(context);
        player = new ExoPlayer.Builder(context).build();
        player.setMediaItem(MediaItem.fromUri(url));
        setListeners();
        player.prepare();
    }

    private void setListeners() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(@Player.State int state) {
                Player.Listener.super.onPlaybackStateChanged(state);
                if (state == Player.STATE_READY && !viewStartSent && analytics) {
                    viewStartSent = true;
                    videoEventsManager.sendViewStartEvent(url, null);
                    int duration = (int) player.getDuration() / 1000;
                    videoEventsManager.sendLoadMetadataEvent(duration, null);
                }
            }
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if(analytics) {
                    if (isPlaying) {
                        videoEventsManager.sendPlayEvent(null);
                    } else {
                        videoEventsManager.sendPauseEvent(null);
                    }
                }
            }
        });
    }

    public void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            if(analytics) {
                videoEventsManager.sendViewEndEvent(null);
                videoEventsManager.sendEvents();
            }
        }
    }

    public void play() {
        if (player != null) {
            player.play();
        }
    }

    public void setAnalytics(AnalyticsType type, String cloudName, String publicId) {
        switch (type) {
            case AUTO:
                analytics = true;
                videoEventsManager.trackingType = TrackingType.AUTO;
                videoEventsManager.cloudName = (cloudName != null) ? cloudName : MediaManager.get().getCloudinary().config.cloudName;
                videoEventsManager.publicId = (publicId != null) ? cloudName : "";
                break;
            case MANUAL:
                analytics = true;
                videoEventsManager.trackingType = TrackingType.MANUAL;
                videoEventsManager.cloudName = (cloudName != null) ? cloudName : MediaManager.get().getCloudinary().config.cloudName;
                videoEventsManager.publicId = (publicId != null) ? cloudName : "";
                break;
            case DISABLED:
                analytics = false;
                break;
        }
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public String getUrl() { return this.url; }
}