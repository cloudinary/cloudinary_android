package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * Base class for the crop overlay handlers.
 */
abstract class CropOverlayGestureHandler implements CropGestureHandler {

    protected static final float GESTURE_REGION = 0.25f;
    protected static final int MIN_GESTURE_REGION = 30;

    protected final Rect overlay;
    protected final Rect bounds = new Rect();
    protected final PointF prevTouchEventPoint = new PointF();
    private CropGestureHandler nextHandler;
    private boolean isStartedGesture;

    public CropOverlayGestureHandler(Rect overlay) {
        this.overlay = overlay;
    }

    public void setNext(CropGestureHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void handleTouchEvent(MotionEvent event, boolean isAspectRatioLocked) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (bounds.contains((int) event.getX(), (int) event.getY())) {
                    isStartedGesture = true;
                    prevTouchEventPoint.set(event.getX(), event.getY());
                    handleGesture(event, isAspectRatioLocked);
                } else {
                    isStartedGesture = false;
                    if (nextHandler != null) {
                        nextHandler.handleTouchEvent(event, isAspectRatioLocked);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isStartedGesture) {
                    handleGesture(event, isAspectRatioLocked);
                    prevTouchEventPoint.set(event.getX(), event.getY());
                } else {
                    if (nextHandler != null) {
                        nextHandler.handleTouchEvent(event, isAspectRatioLocked);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isStartedGesture = false;
                break;
        }
    }

    protected abstract void handleGesture(MotionEvent event, boolean isAspectRatioLocked);

    protected int getGestureRegionWidth() {
        return Math.max((int) (GESTURE_REGION * overlay.width()), MIN_GESTURE_REGION);
    }

    protected int getGestureRegionHeight() {
        return Math.max((int) (GESTURE_REGION * overlay.height()), MIN_GESTURE_REGION);
    }
}
