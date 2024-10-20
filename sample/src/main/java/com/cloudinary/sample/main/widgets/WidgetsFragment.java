package com.cloudinary.sample.main.widgets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;

import com.cloudinary.android.cldvideoplayer.CldVideoPlayer;
import com.cloudinary.sample.databinding.WidgetsFragementBinding;
import com.cloudinary.sample.main.delivery.base.BaseActivity;
import com.cloudinary.sample.main.delivery.base.BaseActivityType;

public class WidgetsFragment extends Fragment {

    WidgetsFragementBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = WidgetsFragementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setVideoPlayers();
        setContainers();
    }

    private void setVideoPlayers() {
        CldVideoPlayer imageWidgetPlayer = new CldVideoPlayer(getActivity(), "https://res.cloudinary.com/mobiledemoapp/video/upload/DevApp_ImageUpload_02_vpsz7p");
        imageWidgetPlayer.getPlayer().setRepeatMode(Player.REPEAT_MODE_ALL);
        binding.widgetsImageWidgetPlayerview.setPlayer(imageWidgetPlayer.getPlayer());
        imageWidgetPlayer.play();

        CldVideoPlayer uploadWidgetPlayer = new CldVideoPlayer(getActivity(), "https://res.cloudinary.com/mobiledemoapp/video/upload/DevApp_UploadWidget_02_r61cfi");
        uploadWidgetPlayer.getPlayer().setRepeatMode(Player.REPEAT_MODE_ALL);
        binding.widgetsUploadWidgetPlayerview.setPlayer(uploadWidgetPlayer.getPlayer());
        uploadWidgetPlayer.play();
    }

    private void setContainers() {
        binding.widgetsImageWidgetContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BaseActivity.class);
                intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TYPE, BaseActivityType.ImageWidget.ordinal());
                startActivity(intent);
            }
        });
        binding.widgetsUploadWidgetContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BaseActivity.class);
                intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TYPE, BaseActivityType.UploadWidget.ordinal());
                startActivity(intent);
            }
        });
    }
}
