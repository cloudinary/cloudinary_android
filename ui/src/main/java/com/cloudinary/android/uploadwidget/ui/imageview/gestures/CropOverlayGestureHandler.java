package com.cloudinary.android.uploadwidget.ui.imageview.gestures;

import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * Base class for the crop overlay handlers.
 */
abstract class CropOverlayGestureHandler implements CropGestureHandler {

    private static final float GESTURE_REGION = 0.25f;
    private static final int MIN_GESTURE_REGION = 30;

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

    /**
     * Detects whether the handler should handle this event or pass it along the chain.
     * Child handlers should call this method after updating its bounds in order to avoid inconsistencies.
     *
     * @param event Motion event which triggered the event.
     * @param isAspectRatioLocked Whether the crop overlay's aspect ratio is locked or not.
     */
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

    /**
     * Override this method to handle the touch gesture that occurred on the crop overlay.
     * This method is invoked only if the {@link MotionEvent#ACTION_DOWN} event was handled by the handler.
     *
     * @param event Motion event which triggered the gesture's touch event.
     * @param isAspectRatioLocked Whether the crop overlay's aspect ratio is locked or not.
     */
    protected abstract void handleGesture(MotionEvent event, boolean isAspectRatioLocked);

    protected int getGestureRegionWidth() {
        return Math.max((int) (GESTURE_REGION * overlay.width()), MIN_GESTURE_REGION);
    }

    protected int getGestureRegionHeight() {
        return Math.max((int) (GESTURE_REGION * overlay.height()), MIN_GESTURE_REGION);
    }
}
