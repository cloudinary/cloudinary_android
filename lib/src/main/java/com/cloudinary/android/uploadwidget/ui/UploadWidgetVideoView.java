package com.cloudinary.android.uploadwidget.ui;

import android.content.Context;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.VideoView;

public class UploadWidgetVideoView extends VideoView {

    private VideoListener mListener;
    private GestureDetectorCompat gestureDetector;

    public UploadWidgetVideoView(Context context) {
        super(context);
        init();
    }

    public UploadWidgetVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UploadWidgetVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        gestureDetector = new GestureDetectorCompat(getContext(), new GestureListener());
    }

    public void setListener(VideoListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return true;
    }

    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onPlay();
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isPlaying()) {
                pause();
            } else {
                start();
            }

            return super.onSingleTapUp(e);
        }

    }

    public interface VideoListener {
        void onPlay();
        void onPause();
    }
}
