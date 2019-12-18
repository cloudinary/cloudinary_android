package com.cloudinary.android.uploadwidget.ui.imageview.gestures;

import android.view.MotionEvent;

interface CropGestureHandler {

    void setNext(CropGestureHandler nextHandler);

    void handleTouchEvent(MotionEvent event, boolean isAspectRatioLocked);
}
