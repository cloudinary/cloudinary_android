package com.cloudinary.android.uploadwidget.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) throws FileNotFoundException {
        Bitmap bitmap;
        InputStream justDecodeBoundsStream = null;
        InputStream sampledBitmapStream = null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            justDecodeBoundsStream = getUriInputStream(context, uri);
            BitmapFactory.decodeStream(justDecodeBoundsStream, null, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            sampledBitmapStream = getUriInputStream(context, uri);
            Bitmap sampledBitmap = BitmapFactory.decodeStream(sampledBitmapStream, null, options);

            bitmap = getScaledBitmap(sampledBitmap, reqWidth, reqHeight);
        } finally {
            try {
                if (justDecodeBoundsStream != null) {
                    justDecodeBoundsStream.close();
                }
            } catch (IOException ignored) {
            }
            try {
                if (sampledBitmapStream != null) {
                    sampledBitmapStream.close();
                }
            } catch (IOException ignored) {
            }
        }

        return bitmap;
    }

    public static Dimensions getBitmapDimensions(Context context, Uri uri) throws FileNotFoundException {
        InputStream justDecodeBoundsStream = null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            justDecodeBoundsStream = getUriInputStream(context, uri);
            BitmapFactory.decodeStream(justDecodeBoundsStream, null, options);
        } finally {
            try {
                if (justDecodeBoundsStream != null) {
                    justDecodeBoundsStream.close();
                }
            } catch (IOException ignored) {
            }
        }

        return new Dimensions(options.outWidth, options.outHeight);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static InputStream getUriInputStream(Context context, Uri uri) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(uri);
    }

    private static Bitmap getScaledBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        if (reqWidth > 0 && reqHeight > 0) {
            Bitmap resized;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float scale = Math.max(width / (float) reqWidth, height / (float) reqHeight);

            resized = Bitmap.createScaledBitmap(bitmap, (int) (width / scale), (int) (height / scale), false);
            if (resized != null) {
                if (resized != bitmap) {
                    bitmap.recycle();
                }
                return resized;
            }
        }

        return bitmap;
    }

}
