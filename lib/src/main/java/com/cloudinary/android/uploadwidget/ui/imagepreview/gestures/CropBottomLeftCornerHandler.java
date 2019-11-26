package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

class CropBottomLeftCornerHandler extends CropOverlayGestureHandler {

    private final CropOverlayGestureCallback listener;

    public CropBottomLeftCornerHandler(Rect overlay, CropOverlayGestureCallback listener) {
        super(overlay);
        this.listener = listener;
    }

    @Override
    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        bounds.set(overlay.left - getGestureRegionWidth(), overlay.bottom - getGestureRegionHeight(), overlay.left + getGestureRegionWidth(), overlay.bottom + getGestureRegionHeight());

        super.handleGesture(event, isAspectRatioLocked);
    }

    @Override
    public void handleCropGesture(MotionEvent event, boolean isAspectRatioLocked) {
        int left = (int) event.getX();
        int top = overlay.top;
        int right = overlay.right;
        int bottom = (int) event.getY();

        if (isAspectRatioLocked) {
            top += left - overlay.left;
            right += bottom - overlay.bottom;
        }

        if (listener != null) {
            listener.onOverlaySizeChanged(left, top, right, bottom);
        }
    }

}