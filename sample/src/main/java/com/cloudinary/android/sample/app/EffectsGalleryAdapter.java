package com.cloudinary.android.sample.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.model.EffectData;
import com.squareup.picasso.Picasso;

import java.util.List;

class EffectsGalleryAdapter extends RecyclerView.Adapter<EffectsGalleryAdapter.ImageViewHolder> {
    private final int requiredSize;
    private final ItemClickListener listener;
    private final int margins;
    private List<EffectData> images;
    private Context context;
    private EffectData selected = null;

    EffectsGalleryAdapter(Context context, List<EffectData> images, int requiredSize, ItemClickListener listener) {
        this.context = context;
        this.images = images;
        this.requiredSize = requiredSize;
        this.listener = listener;

        if (images.size() > 0) {
            selected = images.get(0);
        }

        margins = context.getResources().getDimensionPixelSize(R.dimen.effect_item_margin);
    }

    @Override
    public EffectsGalleryAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_effects_gallery, parent, false);
        viewGroup.getLayoutParams().height = requiredSize;
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

        return new EffectsGalleryAdapter.ImageViewHolder(viewGroup, (ImageView) viewGroup.findViewById(R.id.image_view), viewGroup.findViewById(R.id.selected_indicator));
    }

    @Override
    public void onBindViewHolder(final EffectsGalleryAdapter.ImageViewHolder holder, int position) {
        EffectData data = images.get(position);
        String url = data.getThumbUrl();
        holder.itemView.setTag(images.get(position));
        Picasso.with(context).load(url).into(holder.imageView);

        if (selected != null && selected.equals(data)) {
            holder.selection.setVisibility(View.VISIBLE);
        } else {
            holder.selection.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    interface ItemClickListener {
        void onClick(EffectData data);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final View selection;

        ImageViewHolder(final View itemView, final ImageView imageView, View selection) {
            super(itemView);
            this.imageView = imageView;
            this.selection = selection;
        }
    }
}