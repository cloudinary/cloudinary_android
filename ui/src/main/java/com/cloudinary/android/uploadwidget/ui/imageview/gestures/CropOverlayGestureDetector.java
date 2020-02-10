package com.cloudinary.android.uploadwidget.ui.imageview.gestures;

import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * Detects various gestures on the crop overlay, firing a {@link CropOverlayGestureCallback} for the corresponding gestures.
 */
public class CropOverlayGestureDetector {

    private final CropGestureHandler cropGestureHandler;

    /**
     * Creates a CropOverlayGestureDetector with the supplied crop overlay rectangle, and a listener
     * to respond for the detected gestures.
     *
     * @param overlay  Crop overlay rectangle.
     * @param listener Notified with the callbacks for the corresponding gestures.
     */
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
     * Handles touch events on the crop overlay.
     * @param event Motion event which triggered the event.
     * @param isAspectRatioLocked Whether the crop overlay's aspect ratio is locked or not.
     */
    public void onTouchEvent(MotionEvent event, boolean isAspectRatioLocked) {
        cropGestureHandler.handleTouchEvent(event, isAspectRatioLocked);
    }
}
