package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.view.MotionEvent;

interface CropGestureHandler {

    void setNext(CropGestureHandler nextHandler);

    void handleTouchEvent(MotionEvent event, boolean isAspectRatioLocked);
}
