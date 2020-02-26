package com.cloudinary.android.uploadwidget.ui.imageview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

class CropLeftSideHandler extends CropOverlayGestureHandler {

    private final CropOverlayGestureCallback listener;

    CropLeftSideHandler(Rect overlay, CropOverlayGestureCallback listener) {
        super(overlay);
        this.listener = listener;
    }

    @Override
    public void handleTouchEvent(MotionEvent event, boolean isAspectRatioLocked) {
        bounds.set(overlay.left - getGestureRegionWidth(), overlay.top + getGestureRegionHeight(), overlay.left + getGestureRegionWidth(), overlay.bottom - getGestureRegionHeight());

        super.handleTouchEvent(event, isAspectRatioLocked);
    }

    @Override
    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        int left = overlay.left + (int) (event.getX() - prevTouchEventPoint.x);
        int top = overlay.top;
        int right = overlay.right;
        int bottom = overlay.bottom;

        if (isAspectRatioLocked) {
            top += ((float) left - overlay.left) / 2;
            bottom -= ((float) left - overlay.left) / 2;
        }

        if (listener != null) {
            listener.onOverlaySizeChanged(left, top, right, bottom);
        }
    }

}
