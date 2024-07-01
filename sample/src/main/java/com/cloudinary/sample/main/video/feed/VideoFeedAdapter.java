package com.cloudinary.sample.main.video.feed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaCodec;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.cldvideoplayer.CldVideoPlayer;
import com.cloudinary.sample.databinding.VideoFeedItemBinding;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class VideoFeedAdapter extends RecyclerView.Adapter<VideoFeedAdapter.VideoViewHolder> {

    private final List<String> videoUrls;
    private Context context;

    public VideoFeedAdapter(Context context, List<String> videoUrls) {
        this.context = context;
        this.videoUrls = videoUrls;
    }

    private int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        VideoFeedItemBinding binding = VideoFeedItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VideoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return videoUrls.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private final VideoFeedItemBinding binding;
        private CldVideoPlayer player;
        private boolean isPlaying = false;

        public VideoViewHolder(VideoFeedItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.playerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(@NonNull View v) {
                    if (player != null && !isPlaying) {
                        player.getPlayer().play();
                        isPlaying = true;
                    }
                }

                @Override
                public void onViewDetachedFromWindow(@NonNull View v) {
                    if (player != null && isPlaying) {
                        player.getPlayer().pause();
                        isPlaying = false;
                    }
                }
            });
        }

        @SuppressLint("UnsafeOptInUsageError")
        public void bind(int position) {
            PlayerView playerView = binding.playerView;

            String videoUrl = videoUrls.get(position);
            int screenHeight = getScreenHeight(context);

            ViewGroup.LayoutParams layoutParams = playerView.getLayoutParams();
            layoutParams.height = screenHeight;
            playerView.setLayoutParams(layoutParams);

            try {
                player = new CldVideoPlayer(playerView.getContext(), new URL(videoUrl));
                player.getPlayer().setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                player.getPlayer().setRepeatMode(Player.REPEAT_MODE_ALL);
                playerView.setPlayer(player.getPlayer());

                playerView.hideController();

                playerView.setUseController(false);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
