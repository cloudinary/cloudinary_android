package com.cloudinary.sample.main.video;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.cldvideoplayer.CldVideoPlayer;
import com.cloudinary.sample.databinding.VideoFragmentBinding;
import com.cloudinary.sample.main.delivery.usecases.OnUseCaseItemSelectedListener;
import com.cloudinary.sample.main.video.feed.VideoFeedActivity;

public class VideoFragment extends Fragment implements OnUseCaseItemSelectedListener {
    VideoFragmentBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = VideoFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setVideoPlayer();
        setRecycleView();
    }

    private void setVideoPlayer() {
        CldVideoPlayer player = new CldVideoPlayer(this.getContext(), "https://res.cloudinary.com/mobiledemoapp/video/upload/v1706627936/Demo%20app%20content/sport-1_tjwumh.mp4");
        player.getPlayer().setRepeatMode(Player.REPEAT_MODE_ALL);
        binding.videoPlayerPlayerview.setPlayer(player.getPlayer());
        player.play();
    }

    private void setRecycleView() {
        RecyclerView recyclerView = binding.videoFeedRecyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        VideoFeedSelectionAdapter adapter = new VideoFeedSelectionAdapter(getActivity(), this);
        recyclerView.setAdapter(adapter);
    }

    private void goToVideoFeed() {
        Intent intent = new Intent(getActivity(), VideoFeedActivity.class);
        startActivity(intent);
    }

    @Override
    public void onUseCaseItemSelected(int position) {
        goToVideoFeed();
    }
}
