package com.cloudinary.android.cldvideoplayer;

import android.content.Context;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;


public class CldVideoPlayer {

    ExoPlayer player;

    String url;

    public CldVideoPlayer(Context context, String publicId) {
        url = MediaManager.get().url().resourceType("video").transformation(new Transformation().streamingProfile("auto")).format("m3u8").generate(publicId);
        initPlayer(context, url);
    }
    public CldVideoPlayer(Context context, String publicId, Transformation transformation) {
        transformation.streamingProfile("auto");
        url = MediaManager.get().url().resourceType("video").transformation(transformation).format("m3u8").generate(publicId);
        initPlayer(context, url);
    }

    public CldVideoPlayer(Context context, String publicId, Transformation transformation, Boolean automaticStreamingProfile) {
        if (automaticStreamingProfile) {
            transformation.streamingProfile("auto");
            transformation.fetchFormat("m3u8");
        }
        url = MediaManager.get().url().resourceType("video").transformation(transformation).generate(publicId);
        initPlayer(context, url);
    }

    private void initPlayer(Context context, String url) {
        player = new ExoPlayer.Builder(context).build();
        MediaItem mediaItem = MediaItem.fromUri(url);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    public void play() {
        if (player != null) {
            player.play();
        }
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public String getUrl() { return url; }
}