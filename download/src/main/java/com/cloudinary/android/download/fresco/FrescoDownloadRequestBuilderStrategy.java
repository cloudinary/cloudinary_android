package com.cloudinary.android.download.fresco;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.cloudinary.android.download.DownloadRequestBuilderStrategy;
import com.cloudinary.android.download.DownloadRequestStrategy;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

class FrescoDownloadRequestBuilderStrategy implements DownloadRequestBuilderStrategy {

    private GenericDraweeHierarchyBuilder genericDraweeHierarchyBuilder;
    private ImageRequestBuilder imageRequestBuilder;

    FrescoDownloadRequestBuilderStrategy(Context context) {
        genericDraweeHierarchyBuilder = GenericDraweeHierarchyBuilder.newInstance(context.getResources());
    }

    @Override
    public DownloadRequestBuilderStrategy load(String url) {
        imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url));
        return this;
    }

    @Override
    public DownloadRequestBuilderStrategy load(int resourceId) {
        imageRequestBuilder = ImageRequestBuilder.newBuilderWithResourceId(resourceId);
        return this;
    }

    @Override
    public DownloadRequestBuilderStrategy placeholder(int resourceId) {
        genericDraweeHierarchyBuilder.setPlaceholderImage(resourceId);
        return this;
    }

    @Override
    public DownloadRequestStrategy into(ImageView imageView) {
        if (!(imageView instanceof DraweeView)) {
            throw new IllegalArgumentException("ImageView must be an instance of DraweeView.");
        }

        ImageRequest imageRequest = imageRequestBuilder.build();
        GenericDraweeHierarchy genericDraweeHierarchy = genericDraweeHierarchyBuilder.build();

        DraweeView draweeView = (DraweeView) imageView;
        DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .build();
        draweeController.setHierarchy(genericDraweeHierarchy);
        draweeView.setController(draweeController);

        return new FrescoDownloadRequestStrategy();
    }
}
