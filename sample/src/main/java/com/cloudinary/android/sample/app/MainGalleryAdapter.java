package com.cloudinary.android.sample.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.core.CloudinaryHelper;
import com.cloudinary.android.sample.model.Image;
import com.cloudinary.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

class MainGalleryAdapter extends RecyclerView.Adapter<MainGalleryAdapter.ImageViewHolder> {
    private final int requiredSize;
    private final ImageClickedListener listener;
    private List<Image> images;
    private Context context;

    MainGalleryAdapter(Context context, List<Image> images, int requiredSize, ImageClickedListener listener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
        this.requiredSize = requiredSize;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        viewGroup.getLayoutParams().height = requiredSize;
        viewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Image image = (Image) v.getTag();
                    listener.onImageClicked(image);
                }
            }
        });

        return new ImageViewHolder(viewGroup, (ImageView) viewGroup.findViewById(R.id.image_view), viewGroup.findViewById(R.id.syncCheckmark));
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Image image = images.get(position);
        holder.itemView.setTag(image);
        String imageId = image.getCloudinaryPublicId();
        if (StringUtils.isNotBlank(imageId)) {
            // get the image from cloudinary
            Picasso.with(context).load(CloudinaryHelper.getCroppedThumbnailUrl(requiredSize, imageId)).placeholder(R.drawable.ic_launcher).into(holder.imageView);
            holder.checkmarkView.setVisibility(View.VISIBLE);
        } else {
            // get the local original
            Picasso.with(context).load(image.getLocalUri()).into(holder.imageView);
            holder.checkmarkView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void addImage(Image image) {
        images.add(0, image);
        notifyItemInserted(0);
    }

    public void replaceImages(List<Image> images) {
        this.images.clear();
        this.images.addAll(images);
        notifyDataSetChanged();
    }

    interface ImageClickedListener {
        void onImageClicked(Image image);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final View checkmarkView;

        ImageViewHolder(final View itemView, final ImageView imageView, View checkmarkView) {
            super(itemView);
            this.imageView = imageView;
            this.checkmarkView = checkmarkView;
        }
    }
}
