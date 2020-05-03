package com.cloudinary.android.download;

import android.content.Context;
import android.widget.ImageView;

import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.cloudinary.utils.StringUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;

/**
 * A {@link DownloadRequestBuilder} implementation that holds all the logic of turning
 * the cloudinary params into an actual url, passing it along to a {@link DownloadRequestBuilderStrategy}.
 */
public class DownloadRequestBuilderImpl implements DownloadRequestBuilder {

    private Context context;
    private DownloadRequestBuilderStrategy downloadRequestBuilderStrategy;
    private Object source;
    private Transformation transformation;
    private ResponsiveUrl responsive;
    private int placeholder;
    private boolean isCloudinaryPublicIdSource;
    private DownloadRequestCallback callback;

    public DownloadRequestBuilderImpl(Context context, DownloadRequestBuilderStrategy downloadRequestBuilderStrategy) {
        this.context = context;
        this.downloadRequestBuilderStrategy = downloadRequestBuilderStrategy;
    }

    @Override
    public DownloadRequestBuilder load(String source) {
        isCloudinaryPublicIdSource = !StringUtils.isRemoteUrl(source);
        this.source = source;
        return this;
    }

    @Override
    public DownloadRequestBuilder load(@IdRes int resource) {
        this.source = resource;
        return this;
    }

    @Override
    public DownloadRequestBuilder transformation(Transformation transformation) {
        this.transformation = transformation;
        return this;
    }

    @Override
    public DownloadRequestBuilder responsive(ResponsiveUrl responsiveUrl) {
        this.responsive = responsiveUrl;
        return this;
    }

    @Override
    public DownloadRequestBuilder responsive(ResponsiveUrl.Preset responsivePreset) {
        this.responsive = MediaManager.get().responsiveUrl(responsivePreset);
        return this;
    }

    @Override
    public DownloadRequestBuilder placeholder(@DrawableRes int resourceId) {
        this.placeholder = resourceId;
        return this;
    }

    @Override
    public DownloadRequestBuilder callback(DownloadRequestCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public DownloadRequest into(ImageView imageView) {
        final DownloadRequestImpl downloadRequestImpl = new DownloadRequestImpl(downloadRequestBuilderStrategy, imageView);

        if (source == null) {
            throw new IllegalStateException("Source is null.");
        }
        if (source instanceof String) {
            if (isCloudinaryPublicIdSource) {
                Url url = MediaManager.get().url().publicId(source).transformation(transformation);

                if (responsive != null) {
                    responsive.generate(url, imageView, new ResponsiveUrl.Callback() {
                        @Override
                        public void onUrlReady(Url url) {
                            downloadRequestImpl.setSource(url.generate());
                        }
                    });
                } else {
                    downloadRequestImpl.setSource(url.generate());
                }
            } else {
                downloadRequestImpl.setSource(source);
            }
        } else if (source instanceof Integer) {
            downloadRequestImpl.setSource(source);
        } else {
            throw new IllegalArgumentException("Load source is not an instance of a correct type.");
        }

        if (placeholder != 0) {
            downloadRequestBuilderStrategy.placeholder(placeholder);
        }
        if (callback != null) {
            downloadRequestBuilderStrategy.callback(callback);
        }

        return downloadRequestImpl.start();
    }
}
