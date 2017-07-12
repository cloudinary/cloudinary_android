package com.cloudinary.android.sample.app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.core.CloudinaryHelper;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

class ResourcesAdapter extends RecyclerView.Adapter<ResourcesAdapter.ResourceViewHolder> {
    private final int requiredSize;
    private final ImageClickedListener listener;
    private final List<Resource.UploadStatus> validStatuses;
    private List<ResourceWithMeta> resources;
    private Context context;

    ResourcesAdapter(Context context, List<Resource> resources, int requiredSize, List<Resource.UploadStatus> validStatuses, ImageClickedListener listener) {
        this.context = context;
        this.listener = listener;
        this.requiredSize = requiredSize;
        this.validStatuses = validStatuses;

        this.resources = new ArrayList<>(resources.size());
        for (Resource resource : resources) {
            this.resources.add(new ResourceWithMeta(resource));
        }
    }

    @Override
    public ResourceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_gallery, parent, false);
        viewGroup.getLayoutParams().height = requiredSize;
        viewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Resource resource = (Resource) v.getTag();
                    listener.onImageClicked(resource);
                }
            }
        });

        View deleteButton = viewGroup.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Resource resource = (Resource) v.getTag();
                    listener.onDeleteClicked(resource);
                }
            }
        });

        TextView textView = (TextView) viewGroup.findViewById(R.id.statusText);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, ((TextView) v).getText(), Toast.LENGTH_LONG).show();
            }
        });

        return new ResourceViewHolder(viewGroup, (ImageView) viewGroup.findViewById(R.id.image_view), textView, deleteButton, viewGroup.findViewById(R.id.buttonsContainer), (ProgressBar) viewGroup.findViewById(R.id.uploadProgress));
    }

    @Override
    public void onBindViewHolder(final ResourceViewHolder holder, int position) {
        ResourceWithMeta resourceWithMeta = resources.get(position);
        final Resource resource = resourceWithMeta.resource;
        holder.itemView.setTag(resource);
        holder.deleteButton.setTag(resource);
        holder.statusText.setText(getText(resource));
        if (resource.getStatus() == Resource.UploadStatus.UPLOADED) {
            // get the resource from cloudinary
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER);
            Picasso.with(context).load(CloudinaryHelper.getCroppedThumbnailUrl(requiredSize, resource)).into(holder.imageView);
        } else {
            // get the local original
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.with(context).load(resource.getLocalUri()).resize(requiredSize, requiredSize).centerCrop().into(holder.imageView);
        }

        if (resourceWithMeta.totalBytes > 0) {
            double progressFraction = (double) resourceWithMeta.bytes / resourceWithMeta.totalBytes;
            int progress = (int) Math.round(progressFraction * 1000);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setMax(1000);
            holder.progressBar.setProgress(progress);
        } else {
            holder.progressBar.setProgress(0);
            holder.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    private String getText(Resource resource) {
        String status = resource.getStatus().name().substring(0, 1).toUpperCase() + resource.getStatus().name().substring(1).toLowerCase();
        return StringUtils.isBlank(resource.getLastError()) ? status : status + ": " + resource.getLastError();
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    public void addResource(Resource resource) {
        resources.add(0, new ResourceWithMeta(resource));
        notifyItemInserted(0);
    }

    public void replaceImages(List<Resource> resources) {
        this.resources.clear();
        for (Resource resource : resources) {
            this.resources.add(new ResourceWithMeta(resource));
        }

        notifyDataSetChanged();
    }

    public Resource removeResource(String resourceId) {
        Resource toRemove = null;
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i).resource;
            if (resource.getLocalUri().equals(resourceId)) {
                toRemove = resource;
                resources.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }

        return toRemove;
    }

    public void resourceUpdated(Resource updated) {
        if (!validStatuses.contains(updated.getStatus())) {
            removeResource(updated.getLocalUri());
        } else {
            boolean found = false;
            for (int i = 0; i < resources.size(); i++) {
                ResourceWithMeta resourceWithMeta = resources.get(i);
                Resource resource = resourceWithMeta.resource;
                if (resource.getRequestId().equals(updated.getRequestId())) {
                    Resource.copyFields(updated, resource);
                    resourceWithMeta.bytes = 0;
                    resourceWithMeta.totalBytes = 0;
                    notifyItemChanged(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                // not found but status is valid - it should be added here.
                addResource(updated);
            }
        }
    }

    public void progressUpdated(String requestId, long bytes, long totalBytes) {
        for (int i = 0; i < resources.size(); i++) {
            ResourceWithMeta resource = resources.get(i);
            if (resource.resource.getRequestId().equals(requestId)) {
                resource.bytes = bytes;
                resource.totalBytes = totalBytes;
                notifyItemChanged(i);
                break;
            }
        }
    }

    interface ImageClickedListener {
        void onImageClicked(Resource resource);

        void onDeleteClicked(Resource resource);
    }

    static class ResourceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView statusText;
        private final View deleteButton;
        private final View buttonsContainer;
        private final ProgressBar progressBar;

        ResourceViewHolder(final View itemView, final ImageView imageView, TextView statusText, View deleteButton, View buttonsContainer, ProgressBar progressBar) {
            super(itemView);
            this.imageView = imageView;
            this.statusText = statusText;
            this.deleteButton = deleteButton;
            this.buttonsContainer = buttonsContainer;
            this.progressBar = progressBar;
        }
    }

    static class ResourceWithMeta {
        final Resource resource;
        long bytes = 0;
        long totalBytes;

        ResourceWithMeta(Resource resource) {
            this.resource = resource;
        }
    }
}
