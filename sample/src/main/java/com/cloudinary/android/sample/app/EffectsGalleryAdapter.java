package com.cloudinary.android.sample.app;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudinary.android.sample.R;
import com.squareup.picasso.Picasso;

import java.util.List;

class EffectsGalleryAdapter extends RecyclerView.Adapter<EffectsGalleryAdapter.ImageViewHolder> {
    private final int requiredSize;
    private List<String> images;
    private Context context;

    EffectsGalleryAdapter(Context context, List<String> images, int requiredSize) {
        this.context = context;
        this.images = images;
        this.requiredSize = requiredSize;
    }

    @Override
    public EffectsGalleryAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        viewGroup.getLayoutParams().height = requiredSize;
        viewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = v.getTag().toString();
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                ClipData.newPlainText("Cloudinary Url", url);
                Toast.makeText(context, "Url copied to clipboard!", Toast.LENGTH_LONG).show();
            }
        });
        return new EffectsGalleryAdapter.ImageViewHolder(viewGroup, (ImageView) viewGroup.findViewById(R.id.image_view));
    }

    @Override
    public void onBindViewHolder(final EffectsGalleryAdapter.ImageViewHolder holder, int position) {
        String image = images.get(position);
        holder.itemView.setTag(image);
        Picasso.with(context).load(image).placeholder(R.drawable.ic_launcher).into(holder.imageView);
        holder.imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        ImageViewHolder(final View itemView, final ImageView imageView) {
            super(itemView);
            this.imageView = imageView;
        }
    }
}