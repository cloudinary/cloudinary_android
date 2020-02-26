package com.cloudinary.android.uploadwidget.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cloudinary.android.ui.R;
import com.cloudinary.android.uploadwidget.model.BitmapManager;
import com.cloudinary.android.uploadwidget.model.Dimensions;
import com.cloudinary.android.uploadwidget.utils.MediaType;
import com.cloudinary.android.uploadwidget.utils.UriUtils;

import java.util.ArrayList;

/**
 * Displays the images' thumbnails.
 */
class ThumbnailsAdapter extends RecyclerView.Adapter<ThumbnailsAdapter.ThumbnailViewHolder> {

    private int selectedThumbnailPosition;
    private ArrayList<Uri> imagesUris;
    private Callback callback;

    public ThumbnailsAdapter(ArrayList<Uri> imagesUris, Callback callback) {
        this.imagesUris = imagesUris;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_list_item, parent, false);
        return new ThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ThumbnailViewHolder holder, final int position) {
        final Uri uri = imagesUris.get(position);

        if (position == selectedThumbnailPosition) {
            holder.imageView.setBackgroundResource(R.drawable.selected_thumbnail_border);
        } else {
            holder.imageView.setBackgroundResource(0);
        }
        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.imageView.setImageBitmap(null);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyItemChanged(selectedThumbnailPosition);
                selectedThumbnailPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedThumbnailPosition);
                callback.onThumbnailClicked(uri);
            }
        });

        Context context = holder.itemView.getContext();
        int thumbnailSize = (int) context.getResources().getDimension(R.dimen.thumbnail_size);

        MediaType mediaType = UriUtils.getMediaType(context, uri);
        if (mediaType == MediaType.IMAGE) {
            holder.mediaTypeIcon.setVisibility(View.GONE);

            BitmapManager.get().load(context, uri, thumbnailSize, thumbnailSize, new BitmapManager.LoadCallback() {
                @Override
                public void onSuccess(Bitmap bitmap, Dimensions originalDimensions) {
                    if (holder.getAdapterPosition() == position) {
                        holder.imageView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFailure() { }
            });
        } else if (mediaType == MediaType.VIDEO) {
            holder.mediaTypeIcon.setVisibility(View.VISIBLE);
            holder.mediaTypeIcon.setImageResource(R.drawable.video);

            BitmapManager.get().thumbnail(context, uri, thumbnailSize, thumbnailSize, new BitmapManager.LoadCallback() {
                @Override
                public void onSuccess(Bitmap bitmap, Dimensions originalDimensions) {
                    if (holder.getAdapterPosition() == position) {
                        holder.imageView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFailure() { }
            });
        }
    }

    @Override
    public int getItemCount() {
        return imagesUris.size();
    }

    /**
     * Set the selected thumbnail.
     *
     * @param position Position of the new selected thumbnail.
     */
    public void setSelectedThumbnail(int position) {
        notifyItemChanged(selectedThumbnailPosition);
        notifyItemChanged(position);
        selectedThumbnailPosition = position;
    }


    static class ThumbnailViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private ImageView mediaTypeIcon;

        ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            mediaTypeIcon = itemView.findViewById(R.id.mediaTypeIcon);
        }
    }

    /**
     * Callback for interacting with the thumbnail list.
     */
    public interface Callback {

        /**
         * Called when a thumbnail is clicked.
         *
         * @param uri Uri of the clicked thumbnail.
         */
        void onThumbnailClicked(Uri uri);
    }
}
