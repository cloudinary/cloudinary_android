package com.cloudinary.android.uploadwidget.ui.imageview.gestures;

/**
 * Callback for overlay gestures.
 */
public interface CropOverlayGestureCallback {

    /**
     * Called when the overlay is dragged.
     * @param distanceX Distance the overlay was dragged on the x-axis.
     * @param distanceY Distance the overlay was dragged on the y-axis.
     */
    void onOverlayDragged(int distanceX, int distanceY);

    /**
     * Called when the overlay is resized.
     * @param left Left value of the resized overlay.
     * @param top Top value of the resized overlay.
     * @param right Right value of the resized overlay.
     * @param bottom Bottom value of the resized overlay.
     */
    void onOverlaySizeChanged(int left, int top, int right, int bottom);

}
