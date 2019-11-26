package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

class CropBottomSideHandler extends CropOverlayGestureHandler {

    private final CropOverlayGestureCallback listener;

    CropBottomSideHandler(Rect overlay, CropOverlayGestureCallback listener) {
        super(overlay);
        this.listener = listener;
    }

    @Override
    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        bounds.set(overlay.left + getGestureRegionWidth(), overlay.bottom - getGestureRegionHeight(), overlay.right - getGestureRegionWidth(), overlay.bottom + getGestureRegionHeight());

        super.handleGesture(event, isAspectRatioLocked);
    }

    @Override
    public void handleCropGesture(MotionEvent event, boolean isAspectRatioLocked) {
        int left = overlay.left;
        int top = overlay.top;
        int right = overlay.right;
        int bottom = (int) event.getY();

        if (isAspectRatioLocked) {
            left -= bottom - overlay.bottom;
            right += bottom - overlay.bottom;
        }

        if (listener != null) {
            listener.onOverlaySizeChanged(left, top, right, bottom);
        }
    }

}