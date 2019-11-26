package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

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
    private CropGestureHandler nextHandler;
    private boolean isStartedGesture;

    public CropOverlayGestureHandler(Rect overlay) {
        this.overlay = overlay;
    }

    public void setNext(CropGestureHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void handleGesture(MotionEvent event, boolean isAspectRatioLocked) {
        if (bounds.contains((int) event.getX(), (int) event.getY())) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isStartedGesture = true;
                    handleCropGesture(event, isAspectRatioLocked);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isStartedGesture) {
                        handleCropGesture(event, isAspectRatioLocked);
                    } else {
                        if (nextHandler != null) {
                            nextHandler.handleGesture(event, isAspectRatioLocked);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isStartedGesture = false;
                    break;
            }
        } else {
            isStartedGesture = false;

            if (nextHandler != null) {
                nextHandler.handleGesture(event, isAspectRatioLocked);
            }
        }
    }

    protected abstract void handleCropGesture(MotionEvent event, boolean isAspectRatioLocked);

    protected int getGestureRegionWidth() {
        return Math.max((int) (GESTURE_REGION * overlay.width()), MIN_GESTURE_REGION);
    }

    protected int getGestureRegionHeight() {
        return Math.max((int) (GESTURE_REGION * overlay.height()), MIN_GESTURE_REGION);
    }
}
