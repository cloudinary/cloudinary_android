package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

class CropBottomRightCornerHandler extends CropOverlayGestureHandler {

    private final CropOverlayGestureCallback listener;

    CropBottomRightCornerHandler(Rect overlay, CropOverlayGestureCallback listener) {
        super(overlay);
        this.listener = listener;
    }

    @Override
    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        bounds.set(overlay.right - getGestureRegionWidth(), overlay.bottom - getGestureRegionHeight(), overlay.right + getGestureRegionWidth(), overlay.bottom + getGestureRegionHeight());

        super.handleGesture(event, isAspectRatioLocked);
    }

    @Override
    public void handleCropGesture(MotionEvent event, boolean isAspectRatioLocked) {
        int left = overlay.left;
        int top = overlay.top;
        int right = (int) event.getX();
        int bottom = (int) event.getY();

        if (isAspectRatioLocked) {
            left -= bottom - overlay.bottom;
            top -= right - overlay.right;
        }

        if (listener != null) {
            listener.onOverlaySizeChanged(left, top, right, bottom);
        }
    }

}
