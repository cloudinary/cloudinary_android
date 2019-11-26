package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.view.MotionEvent;

interface CropGestureHandler {

    void setNext(CropGestureHandler nextHandler);

    void handleGesture(MotionEvent event, boolean isAspectRatioLocked);
}
