package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

class CropLeftSideHandler extends CropOverlayGestureHandler {

    private final Rect overlay;
    private final CropOverlayGestureCallback listener;

    CropLeftSideHandler(Rect overlay, CropOverlayGestureCallback listener) {
        this.overlay = overlay;
        this.listener = listener;
    }

    @Override
    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        bounds.set(overlay.left - RESIZING_OFFSET, overlay.top + RESIZING_OFFSET, overlay.left + RESIZING_OFFSET, overlay.bottom - RESIZING_OFFSET);

        super.handleGesture(event, isAspectRatioLocked);
    }

    @Override
    public void handleCropGesture(MotionEvent event, boolean isAspectRatioLocked) {
        int left = (int) event.getX();
        int top = overlay.top;
        int right = overlay.right;
        int bottom = overlay.bottom;

        if (isAspectRatioLocked) {
            top += left - overlay.left;
            bottom -= left - overlay.left;
        }

        if (listener != null) {
            listener.onOverlaySizeChanged(left, top, right, bottom);
        }
    }

}
