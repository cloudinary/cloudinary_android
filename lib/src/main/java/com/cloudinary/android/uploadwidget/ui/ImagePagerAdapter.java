package com.cloudinary.android.uploadwidget.ui;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cloudinary.android.uploadwidget.ui.imagepreview.UploadWidgetImageView;

import java.util.ArrayList;

public class ImagePagerAdapter extends PagerAdapter {

    private ArrayList<Image> images;

    public ImagePagerAdapter(ArrayList<Uri> imagesUris) {
        images = new ArrayList<>(imagesUris.size());

        for (Uri uri : imagesUris) {
            images.add(new Image(uri));
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Image image = images.get(position);
        Uri uri = image.resultUri;
        if (uri == null) {
            uri = image.sourceUri;
        }

        final UploadWidgetImageView imageView = new UploadWidgetImageView(container.getContext());
        imageView.setTag(image.sourceUri);
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

    public void updateResultUri(Uri sourceUri, Uri resultUri) {
        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);

            if (image.sourceUri.toString().equals(sourceUri.toString())) {
                image.resultUri = resultUri;
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void resetResultUri(Uri sourceUri) {
        updateResultUri(sourceUri, null);
    }

    public int getImageIndex(Uri uri) {
        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            if (image.sourceUri.toString().equals(uri.toString())) {
                return i;
            }
        }

        return -1;
    }
}
