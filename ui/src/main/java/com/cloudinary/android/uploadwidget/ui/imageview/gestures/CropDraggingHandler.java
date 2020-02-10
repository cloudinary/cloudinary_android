package com.cloudinary.android.uploadwidget.ui.imageview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

class CropDraggingHandler extends CropOverlayGestureHandler {

    private final CropOverlayGestureCallback listener;

    CropDraggingHandler(Rect overlay, CropOverlayGestureCallback listener) {
        super(overlay);
        this.listener = listener;
    }

    @Override
    public void handleTouchEvent(MotionEvent event, boolean isAspectRatioLocked) {
        bounds.set(overlay);
        bounds.inset(getGestureRegionWidth(), getGestureRegionHeight());

        super.handleTouchEvent(event, isAspectRatioLocked);
    }

    @Override
    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        float distanceX = event.getX() - prevTouchEventPoint.x;
        float distanceY = event.getY() - prevTouchEventPoint.y;

        if (listener != null) {
            listener.onOverlayDragged((int) distanceX, (int) distanceY);
        }
    }

}