package com.cloudinary.android.sample.app;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.model.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.cloudinary.android.ResponsiveUrl.Preset.AUTO_FILL;

class ResourcesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_REGULAR = 0;
    private static final int TYPE_ERROR = 1;

    private final int requiredSize;
    private final ImageClickedListener listener;
    private final List<Resource.UploadStatus> validStatuses;
    private List<ResourceWithMeta> resources;
    ResourcesAdapter(List<Resource> resources, int requiredSize, List<Resource.UploadStatus> validStatuses, ImageClickedListener listener) {
        this.listener = listener;
        this.requiredSize = requiredSize;
        this.validStatuses = validStatuses;

        this.resources = new ArrayList<>(resources.size());
        for (Resource resource : resources) {
            this.resources.add(new ResourceWithMeta(resource));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_REGULAR) {
            return createRegularViewHolder(parent);
        } else {
            return createFailedViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_REGULAR) {
            bindRegularView((ResourceViewHolder) holder, position);
        } else {
            bindErrorView((FailedResourceViewHolder) holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Resource.UploadStatus status = resources.get(0).resource.getStatus();
        if (status == Resource.UploadStatus.FAILED || status == Resource.UploadStatus.RESCHEDULED) {
            return TYPE_ERROR;
        }

        return TYPE_REGULAR;
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    private RecyclerView.ViewHolder createFailedViewHolder(ViewGroup parent) {
        ViewGroup viewGroup;
        viewGroup = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_gallery_error, parent, false);
        viewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Resource resource = (Resource) v.getTag();
                    listener.onImageClicked(resource);
                }
            }
        });

        View retryButton = viewGroup.findViewById(R.id.retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Resource resource = (Resource) v.getTag();
                    listener.onRetryClicked(resource);
                }
            }
        });

        View cancelButton = viewGroup.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Resource resource = (Resource) v.getTag();
                    listener.onDeleteClicked(resource, false);
                }
            }
        });

        return new FailedResourceViewHolder(viewGroup, (TextView) viewGroup.findViewById(R.id.filename), (ImageView) viewGroup.findViewById(R.id.image_view), retryButton, cancelButton, viewGroup.findViewById(R.id.rescheduleLabel), (TextView) viewGroup.findViewById(R.id.errorDescription));
    }

    private RecyclerView.ViewHolder createRegularViewHolder(ViewGroup parent) {
        ViewGroup viewGroup;
        viewGroup = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_gallery, parent, false);
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
                    listener.onDeleteClicked(resource, false);
                }
            }
        });

        View cancelRequest = viewGroup.findViewById(R.id.buttonClear);
        cancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Resource resource = (Resource) v.getTag();
                    listener.onCancelClicked(resource);
                }
            }
        });
        return new ResourceViewHolder(viewGroup, (ImageView) viewGroup.findViewById(R.id.image_view), (TextView) viewGroup.findViewById(R.id.statusText),
                deleteButton, viewGroup.findViewById(R.id.buttonsContainer), viewGroup.findViewById(R.id.videoIcon), (ProgressBar) viewGroup.findViewById(R.id.uploadProgress),
                viewGroup.findViewById(R.id.black_overlay), (TextView) viewGroup.findViewById(R.id.filename), cancelRequest);
    }

    private void bindErrorView(FailedResourceViewHolder holder, int position) {
        ResourceWithMeta resourceWithMeta = resources.get(position);
        final Resource resource = resourceWithMeta.resource;
        holder.errorDescription.setText(resource.getLastErrorDesc());
        boolean isVideo = resource.getResourceType().equals("video");
        int placeHolder = isVideo ? R.drawable.video_placeholder : R.drawable.placeholder;

        // if we need some specific glide api (override local file dimensions) we can always access it the regular way:
        Glide.with(holder.imageView).load(resource.getLocalUri()).placeholder(placeHolder).centerCrop().override(R.dimen.card_image_width, R.dimen.card_height).into(holder.imageView);

        holder.retryButton.setTag(resource);
        holder.cancelButton.setTag(resource);
        holder.rescheduleLabel.setVisibility(resource.getStatus() == Resource.UploadStatus.RESCHEDULED ? View.VISIBLE : View.GONE);
        holder.filename.setText(resource.getName());
    }

    private void bindRegularView(final ResourceViewHolder holder, int position) {
        ResourceWithMeta resourceWithMeta = resources.get(position);
        final Resource resource = resourceWithMeta.resource;

        // setup default values for more readable code:
        holder.itemView.setTag(resource);
        holder.deleteButton.setTag(resource);
        holder.cancelRequest.setTag(resource);
        holder.deleteButton.setVisibility(View.VISIBLE);
        holder.progressBar.setProgress(0);
        holder.progressBar.setVisibility(View.INVISIBLE);
        holder.buttonsContainer.setVisibility(View.GONE);
        holder.cancelRequest.setVisibility(View.GONE);
        holder.statusText.setText(null);
        holder.name.setText(null);
        boolean local = true;
        boolean isVideo = resource.getResourceType().equals("video");

        switch (resource.getStatus()) {

            case QUEUED:
                holder.blackOverlay.animate().cancel();
                holder.blackOverlay.setVisibility(View.GONE);
                holder.cancelRequest.setVisibility(View.VISIBLE);
                break;
            case UPLOADING:
                holder.cancelRequest.setVisibility(View.VISIBLE);
                setProgress(holder, resourceWithMeta);
                if (isVideo) {
                    holder.name.setText(resource.getName());
                }
                break;
            case UPLOADED:
                holder.blackOverlay.animate().cancel();
                holder.blackOverlay.setVisibility(View.GONE);
                holder.videoIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);
                holder.buttonsContainer.setVisibility(View.VISIBLE);
                local = false;
                break;
            case RESCHEDULED:
                holder.blackOverlay.animate().cancel();
                holder.blackOverlay.setVisibility(View.GONE);
                break;
            case FAILED:
                holder.blackOverlay.animate().cancel();
                holder.blackOverlay.setVisibility(View.GONE);
                break;
        }

        final int placeholder = resource.getResourceType().equals("image") ? R.drawable.placeholder : R.drawable.video_placeholder;

        if (local) {
            Glide.with(holder.imageView).load(resource.getLocalUri()).placeholder(placeholder).centerCrop().override(requiredSize).into(holder.imageView);
        } else {
            String publicId = resource.getCloudinaryPublicId();
            Url url = MediaManager.get().url().publicId(publicId).resourceType(resource.getResourceType()).format("webp");
            MediaManager.get().responsiveUrl(AUTO_FILL)
                    .generate(url, holder.imageView, new ResponsiveUrl.Callback() {
                        @Override
                        public void onUrlReady(Url url) {
                            Glide.with(holder.imageView).load(url.generate()).placeholder(placeholder).into(holder.imageView);
                        }
                    });
        }
    }

    private void setProgress(ResourceViewHolder holder, ResourceWithMeta resourceWithMeta) {
        holder.progressBar.setVisibility(View.VISIBLE);
        ((ViewGroup)holder.progressBar.getParent()).findViewById(R.id.progressContainer).setVisibility(View.VISIBLE);

        if (holder.blackOverlay.getVisibility() != View.VISIBLE) {
            holder.blackOverlay.setAlpha(0);
            holder.blackOverlay.setVisibility(View.VISIBLE);
            holder.blackOverlay.animate().alpha(1f);
        }

        final String progressStr;
        double bytesAsDouble = resourceWithMeta.bytes;
        if (resourceWithMeta.totalBytes > 0) {
            double progressFraction = bytesAsDouble / resourceWithMeta.totalBytes;
            int progress = (int) Math.round(progressFraction * 1000);
            holder.progressBar.setIndeterminate(false);
            holder.progressBar.setMax(1000);
            holder.progressBar.setProgress(progress);
            progressStr = String.valueOf(Math.round(progressFraction * 100)) + "%";
        } else {
            holder.progressBar.setIndeterminate(true);
            progressStr = String.format("%.2f[MB]", bytesAsDouble/ 1000000);
        }

        String text = holder.statusText.getContext().getString(R.string.uploading, progressStr);
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new ForegroundColorSpan(holder.statusText.getContext().getResources().getColor(R.color.buttonColor)), text.indexOf(progressStr), text.length(), 0);
        holder.statusText.setText(spannableString);
    }

    private void addResource(Resource resource) {
        resources.add(0, new ResourceWithMeta(resource));
        notifyItemInserted(0);
    }

    void replaceImages(List<Resource> resources) {
        this.resources.clear();
        for (Resource resource : resources) {
            this.resources.add(new ResourceWithMeta(resource));
        }

        notifyDataSetChanged();
    }

    Resource removeResource(String resourceId) {
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

    void resourceUpdated(Resource updated) {
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

    void progressUpdated(String requestId, long bytes, long totalBytes) {
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

        void onDeleteClicked(Resource resource, Boolean recent);

        void onRetryClicked(Resource resource);

        void onCancelClicked(Resource resource);
    }

    private static class ResourceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView statusText;
        private final View deleteButton;
        private final View buttonsContainer;
        private final ProgressBar progressBar;
        private final View videoIcon;
        private final View blackOverlay;
        private final TextView name;
        private final View cancelRequest;

        ResourceViewHolder(final View itemView, final ImageView imageView, TextView statusText, View deleteButton, View buttonsContainer, View videoIcon, ProgressBar progressBar, View blackOverlay, TextView name, View cancelRequest) {
            super(itemView);
            this.imageView = imageView;
            this.statusText = statusText;
            this.deleteButton = deleteButton;
            this.buttonsContainer = buttonsContainer;
            this.videoIcon = videoIcon;
            this.progressBar = progressBar;
            this.blackOverlay = blackOverlay;
            this.name = name;
            this.cancelRequest = cancelRequest;
        }
    }

    private static class FailedResourceViewHolder extends RecyclerView.ViewHolder {
        private final TextView filename;
        private final ImageView imageView;
        private final View retryButton;
        private final View rescheduleLabel;
        private final TextView errorDescription;
        private final View cancelButton;

        FailedResourceViewHolder(View itemView, TextView filename, ImageView imageView, View cancelButton, View retryButton, View rescheduleLabel, TextView errorDescription) {
            super(itemView);
            this.filename = filename;
            this.imageView = imageView;
            this.retryButton = retryButton;
            this.cancelButton = cancelButton;
            this.rescheduleLabel = rescheduleLabel;
            this.errorDescription = errorDescription;
        }
    }

    private static class ResourceWithMeta {
        final Resource resource;
        long bytes = 0;
        long totalBytes;

        ResourceWithMeta(Resource resource) {
            this.resource = resource;
        }
    }
}