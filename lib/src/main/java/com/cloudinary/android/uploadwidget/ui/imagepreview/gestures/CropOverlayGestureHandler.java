package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * Base class for the crop overlay handlers.
 */
abstract class CropOverlayGestureHandler implements GestureHandler {

    protected static final int RESIZING_OFFSET = 100;

    private boolean isStartedGesture;
    private GestureHandler nextHandler;
    protected final Rect bounds = new Rect();

    public void setNext(GestureHandler nextHandler) {
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
}
