package com.cloudinary.android.sample.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.model.EffectData;

import java.util.List;

import static com.cloudinary.android.ResponsiveUrl.Preset.AUTO_FILL;

class EffectsGalleryAdapter extends RecyclerView.Adapter<EffectsGalleryAdapter.ImageViewHolder> {
    private final int requiredSize;
    private final ItemClickListener listener;
    private List<EffectData> images;
    private EffectData selected = null;
    private String resourceType;

    EffectsGalleryAdapter(List<EffectData> images, String resourceType, int requiredSize, ItemClickListener listener) {
        this.resourceType = resourceType;
        this.images = images;
        this.requiredSize = requiredSize;
        this.listener = listener;

        if (images.size() > 0) {
            selected = images.get(0);
        }
    }

    @Override
    public EffectsGalleryAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_effects_gallery, parent, false);
        viewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = (EffectData) v.getTag();
                if (listener != null) {
                    listener.onClick(selected);
                }

                notifyDataSetChanged();
            }
        });

        ImageView imageView = (ImageView) viewGroup.findViewById(R.id.image_view);
        imageView.getLayoutParams().height = requiredSize;
        return new EffectsGalleryAdapter.ImageViewHolder(viewGroup, imageView, viewGroup.findViewById(R.id.selected_indicator), (TextView) viewGroup.findViewById(R.id.effectName));
    }

    @Override
    public void onBindViewHolder(final EffectsGalleryAdapter.ImageViewHolder holder, int position) {
        EffectData data = images.get(position);
        holder.itemView.setTag(images.get(position));
        holder.nameTextView.setText(data.getName());

        // force image format (webp in this case) so that both video and images are downloaded as images.
        Url baseUrl = MediaManager.get().url().format("webp").resourceType(resourceType).publicId(data.getPublicId()).transformation(data.getTransformation());
        MediaManager.get().responsiveUrl(AUTO_FILL)
                .stepSize(50)
                .generate(baseUrl, holder.imageView, new ResponsiveUrl.Callback() {
                    @Override
                    public void onUrlReady(Url url) {
                        Glide.with(holder.imageView).load(url.generate()).placeholder(R.drawable.placeholder).into(holder.imageView);
                    }
                });

        if (selected != null && selected.equals(data)) {
            holder.selection.setVisibility(View.VISIBLE);
        } else {
            holder.selection.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public interface ItemClickListener {
        void onClick(EffectData data);
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final View selection;
        private final TextView nameTextView;

        ImageViewHolder(final View itemView, final ImageView imageView, View selection, TextView nameTextView) {
            super(itemView);
            this.imageView = imageView;
            this.selection = selection;
            this.nameTextView = nameTextView;
        }
    }
}