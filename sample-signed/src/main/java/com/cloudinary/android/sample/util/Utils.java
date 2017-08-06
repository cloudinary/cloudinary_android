package com.cloudinary.android.sample.util;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public class Utils {
    public static int getScreenWidth(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        window.getDefaultDisplay().getSize(point);
        return point.x;
    }
}
