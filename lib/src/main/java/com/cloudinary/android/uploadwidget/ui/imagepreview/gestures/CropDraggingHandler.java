package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;

class CropDraggingHandler extends CropOverlayGestureHandler {

    private final Rect overlay;
    private final CropOverlayGestureCallback listener;
    private final PointF prevPoint = new PointF();

    CropDraggingHandler(Rect overlay, CropOverlayGestureCallback listener) {
        this.overlay = overlay;
        this.listener = listener;
    }

    @Override
    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        bounds.set(overlay);
        bounds.inset(RESIZING_OFFSET, RESIZING_OFFSET);

        super.handleGesture(event, isAspectRatioLocked);
    }

    @Override
    public void handleCropGesture(MotionEvent event, boolean isAspectRatioLocked) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevPoint.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceX = event.getX() - prevPoint.x;
                float distanceY = event.getY() - prevPoint.y;
                prevPoint.set(event.getX(), event.getY());

                if (listener != null) {
                    listener.onOverlayDragged((int) distanceX, (int) distanceY);
                }
                break;
        }
    }

}