package com.cloudinary.sample.main.video;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.sample.R;
import com.cloudinary.sample.databinding.VideoFeedSelectionCellBinding;
import com.cloudinary.sample.main.delivery.usecases.OnUseCaseItemSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoFeedSelectionAdapter extends RecyclerView.Adapter<VideoFeedSelectionAdapter.VideoFeedSelectionCellViewHolder> {

    private List<String> dataList;
    private int selectedItemPosition = 0;

    private final OnUseCaseItemSelectedListener onUseCasesItemSelectedListener;

    public VideoFeedSelectionAdapter(Context context, OnUseCaseItemSelectedListener listener) {
        dataList = new ArrayList<>(Collections.nCopies(3, ""));
        onUseCasesItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public VideoFeedSelectionCellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        VideoFeedSelectionCellBinding binding = VideoFeedSelectionCellBinding.inflate(inflater, parent, false);
        return new VideoFeedSelectionCellViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoFeedSelectionCellViewHolder holder, int position) {
        holder.setImageView(position);

        holder.itemView.setSelected(position == selectedItemPosition);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemSelected(holder.getBindingAdapterPosition());
            }
        });
    }

    private void itemSelected(int position) {
        if (selectedItemPosition != position) {
            notifyItemChanged(selectedItemPosition);
            selectedItemPosition = position;
            notifyItemChanged(selectedItemPosition);
        }
        onUseCasesItemSelectedListener.onUseCaseItemSelected(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class VideoFeedSelectionCellViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public VideoFeedSelectionCellViewHolder(@NonNull VideoFeedSelectionCellBinding binding) {
            super(binding.getRoot());
            imageView = binding.videoFeedSelectionCellImageview;
        }

        public void setImageView(int position) {
            switch (position) {
                case 0:
                    imageView.setImageResource(R.drawable.tiktok);
                    break;
                case 1:
                    imageView.setImageResource(R.drawable.instagram);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.youtube);
                    break;
            }
        }
    }
}
