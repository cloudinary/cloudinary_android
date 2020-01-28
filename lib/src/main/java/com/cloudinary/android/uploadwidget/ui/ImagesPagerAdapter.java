package com.cloudinary.android.uploadwidget.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cloudinary.android.R;
import com.cloudinary.android.uploadwidget.model.Image;
import com.cloudinary.android.uploadwidget.ui.imageview.UploadWidgetImageView;
import com.cloudinary.android.uploadwidget.utils.MediaType;
import com.cloudinary.android.uploadwidget.utils.UriUtils;

import java.util.ArrayList;

/**
 * TODO: Displays media files or their upload widget results.
 */
class ImagesPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

    private SparseArray<View> views;
    private ArrayList<Image> images;
    private int currentPagePosition;

    public ImagesPagerAdapter(ArrayList<Uri> imagesUris) {
        images = new ArrayList<>(imagesUris.size());
        views = new SparseArray<>(imagesUris.size());

        for (Uri uri : imagesUris) {
            images.add(new Image(uri));
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Context context = container.getContext();
        Image image = images.get(position);

        MediaType mediaType = MediaType.IMAGE;
        Uri uri = image.getResultUri();
        if (uri == null) {
            uri = image.getSourceUri();
            mediaType = UriUtils.getMediaType(context, uri);
        }

        View view = null;
        if (mediaType == MediaType.IMAGE) {
            UploadWidgetImageView imageView = new UploadWidgetImageView(context);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setImageUri(uri);

            view = imageView;
            views.put(position, imageView);
        } else if (mediaType == MediaType.VIDEO) {
            FrameLayout frameLayout = new FrameLayout(context);
            final UploadWidgetVideoView videoView = new UploadWidgetVideoView(context);
            final ImageView playOverlay = new ImageView(context);
            frameLayout.addView(videoView);
            frameLayout.addView(playOverlay);

            FrameLayout.LayoutParams playOverlayLayoutParams = (FrameLayout.LayoutParams) playOverlay.getLayoutParams();
            playOverlayLayoutParams.gravity = Gravity.CENTER;
            int playButtonOverlaySize = (int) context.getResources().getDimension(R.dimen.video_play_button_overlay_size);
            playOverlayLayoutParams.height = playButtonOverlaySize;
            playOverlayLayoutParams.width = playButtonOverlaySize;
            playOverlay.setLayoutParams(playOverlayLayoutParams);
            playOverlay.setImageResource(R.drawable.play_overlay);

            FrameLayout.LayoutParams videoOverlayLayoutParams = (FrameLayout.LayoutParams) videoView.getLayoutParams();
            videoOverlayLayoutParams.gravity = Gravity.CENTER;
            videoView.setLayoutParams(videoOverlayLayoutParams);
            videoView.setVideoURI(uri);
            videoView.setListener(new UploadWidgetVideoView.VideoListener() {
                @Override
                public void onPlay() {
                    playOverlay.setVisibility(View.GONE);
                }

                @Override
                public void onPause() {
                    playOverlay.setVisibility(View.VISIBLE);
                }
            });
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.seekTo(1);
                }
            });

            view = frameLayout;
            views.put(position, videoView);
        }

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        views.remove(position);
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

    public class PageChangedListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            View currentView = views.get(currentPagePosition);
            if (currentView instanceof UploadWidgetVideoView) {
                UploadWidgetVideoView videoView = (UploadWidgetVideoView) currentView;
                videoView.pause();
            }

            currentPagePosition = position;
        }

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        View currentView = views.get(currentPagePosition);
        if (currentView instanceof UploadWidgetVideoView) {
            UploadWidgetVideoView videoView = (UploadWidgetVideoView) currentView;
            videoView.pause();
        }

        currentPagePosition = i;
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
