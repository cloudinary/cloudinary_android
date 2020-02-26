package com.cloudinary.android.uploadwidget.ui.imageview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

class CropTopRightCornerHandler extends CropOverlayGestureHandler {

    private final CropOverlayGestureCallback listener;

    CropTopRightCornerHandler(Rect overlay, CropOverlayGestureCallback listener) {
        super(overlay);
        this.listener = listener;
    }

    @Override
    public void handleTouchEvent(MotionEvent event, boolean isAspectRatioLocked) {
        bounds.set(overlay.right - getGestureRegionWidth(), overlay.top - getGestureRegionHeight(), overlay.right + getGestureRegionWidth(), overlay.top + getGestureRegionHeight());

        super.handleTouchEvent(event, isAspectRatioLocked);
    }

    @Override
    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        int left = overlay.left;
        int top = overlay.top + (int) (event.getY() - prevTouchEventPoint.y);
        int right = overlay.right + (int) (event.getX() - prevTouchEventPoint.x);
        int bottom = overlay.bottom;

        if (isAspectRatioLocked) {
            left += top - overlay.top;
            bottom += right - overlay.right;
        }

        if (listener != null) {
            listener.onOverlaySizeChanged(left, top, right, bottom);
        }
    }

}
