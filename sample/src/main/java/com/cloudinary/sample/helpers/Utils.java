package com.cloudinary.sample.helpers;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.cloudinary.sample.R;

public final class Utils {
    private Utils() {}

    public static String UPLOAD_PRESET = "android_sample";
    public static String getImageWidhtAndHeightString(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            // If the drawable is not a BitmapDrawable, you can use other methods to convert it to a Bitmap
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        return width + "x" + height;
    }

    public static String getImageSize(int size) {
        double megabytes = size / (1024.0);

        return String.format("%.2fKB", megabytes);
    }
}
