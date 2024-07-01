package com.cloudinary.sample.main.upload.single_upload;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.sample.R;
import com.cloudinary.sample.databinding.UploadCellBinding;
import com.cloudinary.sample.local_storage.AssetModelEntity;

import java.util.ArrayList;
import java.util.List;

public class UploadRecycleAdapter extends RecyclerView.Adapter<UploadRecycleAdapter.UploadCellViewHolder> {

    private List<AssetModelEntity> assetModels = new ArrayList<>();

    public UploadRecycleAdapter(Context context) {
    }

    public void setAssetModels(List<AssetModelEntity> assetModels) {
        this.assetModels = assetModels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UploadCellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        UploadCellBinding binding = UploadCellBinding.inflate(inflater, parent, false);
        return new UploadCellViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadCellViewHolder holder, int position) {
        holder.setImageView(assetModels.get(position).getPublicId());
    }

    @Override
    public int getItemCount() {
        return assetModels.size();
    }

    public static class UploadCellViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;


        public UploadCellViewHolder(@NonNull UploadCellBinding binding) {
            super(binding.getRoot());
            imageView = binding.uploadCellImageview;
        }

        public void setImageView(String publicId) {
            Glide.with(imageView)
                    .load(MediaManager.get().url().transformation(new Transformation().crop("thumb")).generate(publicId))
                    .placeholder(R.drawable.placeholder)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // Log the error if needed
                            Log.e("Glide", "Image load failed", e);

                            // Retry logic
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Glide.with(imageView)
                                        .load(MediaManager.get().url().transformation(new Transformation().crop("thumb")).generate(publicId))
                                        .placeholder(R.drawable.placeholder)
                                        .into(imageView);
                            }, 2000); // Retry after 2 seconds

                            return false; // Return false to allow Glide to handle the error
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            // Image loaded successfully
                            return false; // Return false to allow Glide to handle the resource
                        }
                    })
                    .into(imageView);
        }
    }
}
