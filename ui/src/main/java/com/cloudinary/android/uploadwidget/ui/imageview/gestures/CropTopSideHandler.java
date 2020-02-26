package com.cloudinary.android.uploadwidget.ui.imageview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

class CropTopSideHandler extends CropOverlayGestureHandler {

    private final CropOverlayGestureCallback listener;

    CropTopSideHandler(Rect overlay, CropOverlayGestureCallback listener) {
        super(overlay);
        this.listener = listener;
    }

    @Override
    public void handleTouchEvent(MotionEvent event, boolean isAspectRatioLocked) {
        bounds.set(overlay.left + getGestureRegionWidth(), overlay.top - getGestureRegionHeight(), overlay.right - getGestureRegionWidth(), overlay.top + getGestureRegionHeight());

        super.handleTouchEvent(event, isAspectRatioLocked);
    }

    @Override
    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        int left = overlay.left;
        int top = overlay.top + (int) (event.getY() - prevTouchEventPoint.y);
        int right = overlay.right;
        int bottom = overlay.bottom;

        if (isAspectRatioLocked) {
            left += ((float) top - overlay.top) / 2;
            right -= ((float) top - overlay.top) / 2;
        }

        if (listener != null) {
            listener.onOverlaySizeChanged(left, top, right, bottom);
        }
    }

}
