package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * Gesture detector for the crop overlay. Fires a {@link CropOverlayGestureCallback} for the corresponding gestures.
 */
public class CropOverlayGestureDetector {

    private final GestureHandler gestureHandler;

    public CropOverlayGestureDetector(Rect overlay, CropOverlayGestureCallback listener) {
        GestureHandler cropLeftSideHandler = new CropLeftSideHandler(overlay, listener);
        GestureHandler cropTopLeftCornerHandler = new CropTopLeftCornerHandler(overlay, listener);
        GestureHandler cropTopSideHandler = new CropTopSideHandler(overlay, listener);
        GestureHandler cropTopRightCornerHandler = new CropTopRightCornerHandler(overlay, listener);
        GestureHandler cropRightSideHandler = new CropRightSideHandler(overlay, listener);
        GestureHandler cropBottomRightCornerHandler = new CropBottomRightCornerHandler(overlay, listener);
        GestureHandler cropBottomSideHandler = new CropBottomSideHandler(overlay, listener);
        GestureHandler cropBottomLeftCornerHandler = new CropBottomLeftCornerHandler(overlay, listener);
        GestureHandler cropDraggingHandler = new CropDraggingHandler(overlay, listener);

        cropLeftSideHandler.setNext(cropTopLeftCornerHandler);
        cropTopLeftCornerHandler.setNext(cropTopSideHandler);
        cropTopSideHandler.setNext(cropTopRightCornerHandler);
        cropTopRightCornerHandler.setNext(cropRightSideHandler);
        cropRightSideHandler.setNext(cropBottomRightCornerHandler);
        cropBottomRightCornerHandler.setNext(cropBottomSideHandler);
        cropBottomSideHandler.setNext(cropBottomLeftCornerHandler);
        cropBottomLeftCornerHandler.setNext(cropDraggingHandler);

        gestureHandler = cropLeftSideHandler;
    }

    /**
     *
     * @param event
     * @param isAspectRatioLocked
     */
    public void onTouchEvent(MotionEvent event, boolean isAspectRatioLocked) {
        gestureHandler.handleGesture(event, isAspectRatioLocked);
    }
}
