package com.cloudinary.android.uploadwidget.ui;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cloudinary.android.uploadwidget.model.Image;
import com.cloudinary.android.uploadwidget.ui.imageview.UploadWidgetImageView;

import java.util.ArrayList;

/**
 * Displays images or their results.
 */
class ImagesPagerAdapter extends PagerAdapter {

    private ArrayList<Image> images;

    public ImagesPagerAdapter(ArrayList<Uri> imagesUris) {
        images = new ArrayList<>(imagesUris.size());

        for (Uri uri : imagesUris) {
            images.add(new Image(uri));
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Image image = images.get(position);
        Uri uri = image.getResultUri();
        if (uri == null) {
            uri = image.getSourceUri();
        }

        final UploadWidgetImageView imageView = new UploadWidgetImageView(container.getContext());
        imageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setImageUri(uri);

        container.addView(imageView);

        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    /**
     * Update the result image
     *
     * @param sourceUri The Uri of the source image that's being updated.
     * @param resultUri The Uri of the result image.
     */
    public void updateResultImage(Uri sourceUri, Uri resultUri) {
        for (Image image : images) {
            if (image.getSourceUri().toString().equals(sourceUri.toString())) {
                image.setResultUri(resultUri);
                notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * Reset the result image
     *
     * @param sourceUri The Uri of the image to reset.
     */
    public void resetResultImage(Uri sourceUri) {
        updateResultImage(sourceUri, null);
    }

    /**
     * Get the image uri's position within the adapter.
     *
     * @param uri Uri of the image.
     * @return Position of the image within the adapter, or -1 of it doesn't exist.
     */
    public int getImagePosition(Uri uri) {
        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            if (image.getSourceUri().toString().equals(uri.toString())) {
                return i;
            }
        }

        return -1;
    }
}
