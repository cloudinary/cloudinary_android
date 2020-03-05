package com.cloudinary.android.uploadwidget.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cloudinary.android.ui.R;
import com.cloudinary.android.uploadwidget.model.Media;
import com.cloudinary.android.uploadwidget.ui.imageview.UploadWidgetImageView;
import com.cloudinary.android.uploadwidget.utils.MediaType;
import com.cloudinary.android.uploadwidget.utils.UriUtils;

import java.util.ArrayList;

/**
 * Displays media files or their results.
 */
class MediaPagerAdapter extends PagerAdapter {

    private SparseArray<View> views;
    private ArrayList<Media> mediaList;
    private int currentPagePosition;

    public MediaPagerAdapter(ArrayList<Uri> uris, ViewPager mediaViewPager) {
        mediaList = new ArrayList<>(uris.size());
        views = new SparseArray<>(uris.size());

        for (Uri sourceUri : uris) {
            mediaList.add(new Media(sourceUri));
        }

        MediaPageChangedListener pageChangeListener = new MediaPageChangedListener();
        if (mediaViewPager != null) {
            mediaViewPager.addOnPageChangeListener(pageChangeListener);
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Context context = container.getContext();
        Media media = mediaList.get(position);

        MediaType mediaType = MediaType.IMAGE;
        Uri uri = media.getResultUri();
        if (uri == null) {
            uri = media.getSourceUri();
            MediaType fromUri = UriUtils.getMediaType(context, uri);
            if (fromUri != null) {
                mediaType = fromUri;
            }
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
        return mediaList.size();
    }

    /**
     * Update the result media
     *
     * @param sourceUri The Uri of the source media that's being updated.
     * @param resultUri The Uri of the result media.
     */
    public void updateMediaResult(Uri sourceUri, Uri resultUri) {
        for (Media media : mediaList) {
            if (media.getSourceUri().toString().equals(sourceUri.toString())) {
                media.setResultUri(resultUri);
                notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * Reset the result media
     *
     * @param sourceUri The Uri of the source media to reset.
     */
    public void resetMediaResult(Uri sourceUri) {
        updateMediaResult(sourceUri, null);
    }

    /**
     * Get the media uri's position within the adapter.
     *
     * @param uri Uri of the media.
     * @return Position of the media within the adapter, or -1 of it doesn't exist.
     */
    public int getMediaPosition(Uri uri) {
        for (int i = 0; i < mediaList.size(); i++) {
            Media media = mediaList.get(i);
            if (media.getSourceUri().toString().equals(uri.toString())) {
                return i;
            }
        }

        return -1;
    }

    public class MediaPageChangedListener extends ViewPager.SimpleOnPageChangeListener {

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
}
