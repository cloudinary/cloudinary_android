package com.cloudinary.android.uploadwidget.ui.imageview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

class CropBottomSideHandler extends CropOverlayGestureHandler {

    private final CropOverlayGestureCallback listener;

    CropBottomSideHandler(Rect overlay, CropOverlayGestureCallback listener) {
        super(overlay);
        this.listener = listener;
    }

    @Override
    public void handleTouchEvent(MotionEvent event, boolean isAspectRatioLocked) {
        bounds.set(overlay.left + getGestureRegionWidth(), overlay.bottom - getGestureRegionHeight(), overlay.right - getGestureRegionWidth(), overlay.bottom + getGestureRegionHeight());

        super.handleTouchEvent(event, isAspectRatioLocked);
    }

    @Override
    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        int left = overlay.left;
        int top = overlay.top;
        int right = overlay.right;
        int bottom = overlay.bottom + (int) (event.getY() - prevTouchEventPoint.y);

        if (isAspectRatioLocked) {
            left -= ((float) bottom - overlay.bottom) / 2;
            right += ((float) bottom - overlay.bottom) / 2;
        }

        if (listener != null) {
            listener.onOverlaySizeChanged(left, top, right, bottom);
        }
    }

}
