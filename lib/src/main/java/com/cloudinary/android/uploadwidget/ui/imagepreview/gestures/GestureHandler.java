package com.cloudinary.android.uploadwidget.ui.imagepreview.gestures;

import android.view.MotionEvent;

interface GestureHandler {

    void setNext(GestureHandler nextHandler);

    void handleGesture(MotionEvent event, boolean isAspectRatioLocked);
}
