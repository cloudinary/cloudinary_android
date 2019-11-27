package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * Gesture detector for the crop overlay. Fires a {@link CropOverlayGestureCallback} for the corresponding gestures.
 */
public class CropOverlayGestureDetector {

    private final CropGestureHandler cropGestureHandler;

    public CropOverlayGestureDetector(Rect overlay, CropOverlayGestureCallback listener) {
        CropGestureHandler cropLeftSideHandler = new CropLeftSideHandler(overlay, listener);
        CropGestureHandler cropTopLeftCornerHandler = new CropTopLeftCornerHandler(overlay, listener);
        CropGestureHandler cropTopSideHandler = new CropTopSideHandler(overlay, listener);
        CropGestureHandler cropTopRightCornerHandler = new CropTopRightCornerHandler(overlay, listener);
        CropGestureHandler cropRightSideHandler = new CropRightSideHandler(overlay, listener);
        CropGestureHandler cropBottomRightCornerHandler = new CropBottomRightCornerHandler(overlay, listener);
        CropGestureHandler cropBottomSideHandler = new CropBottomSideHandler(overlay, listener);
        CropGestureHandler cropBottomLeftCornerHandler = new CropBottomLeftCornerHandler(overlay, listener);
        CropGestureHandler cropDraggingHandler = new CropDraggingHandler(overlay, listener);

        cropLeftSideHandler.setNext(cropTopLeftCornerHandler);
        cropTopLeftCornerHandler.setNext(cropTopSideHandler);
        cropTopSideHandler.setNext(cropTopRightCornerHandler);
        cropTopRightCornerHandler.setNext(cropRightSideHandler);
        cropRightSideHandler.setNext(cropBottomRightCornerHandler);
        cropBottomRightCornerHandler.setNext(cropBottomSideHandler);
        cropBottomSideHandler.setNext(cropBottomLeftCornerHandler);
        cropBottomLeftCornerHandler.setNext(cropDraggingHandler);

        cropGestureHandler = cropLeftSideHandler;
    }

    /**
     *
     * @param event
     * @param isAspectRatioLocked
     */
    public void onTouchEvent(MotionEvent event, boolean isAspectRatioLocked) {
        cropGestureHandler.handleTouchEvent(event, isAspectRatioLocked);
    }
}
