package com.cloudinary.android.cldvideoplayer;

import android.content.Context;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import java.net.URL;


public class CldVideoPlayer {

    ExoPlayer player;

    String url;

    public CldVideoPlayer(Context context, URL url) {
        String tempUrl = url.toString();
        this.url = tempUrl.toString();
        initPlayer(context, tempUrl);
    }

    public CldVideoPlayer(Context context, String publicId) {
        initiliaze(context, publicId, null, true);
    }
    public CldVideoPlayer(Context context, String publicId, Transformation transformation) {
        initiliaze(context, publicId, transformation, true);
    }

    public CldVideoPlayer(Context context, String publicId, Transformation transformation, Boolean automaticStreamingProfile) {
        initiliaze(context, publicId, transformation, automaticStreamingProfile);
    }

    private void initiliaze(Context context, String publicId, Transformation transformation, Boolean automaticStreamingProfile) {
        if (automaticStreamingProfile && transformation == null) {
            transformation = new Transformation();
            transformation.streamingProfile("auto");
            this.url = MediaManager.get().url().resourceType("video").transformation(transformation).format("m3u8").generate(publicId);
        } else {
            this.url = MediaManager.get().url().resourceType("video").transformation(transformation).generate(publicId);
        }

        initPlayer(context, url);
    }

    private void initPlayer(Context context, String url) {
        player = new ExoPlayer.Builder(context).build();
        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
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

    public String getUrl() { return this.url; }
}