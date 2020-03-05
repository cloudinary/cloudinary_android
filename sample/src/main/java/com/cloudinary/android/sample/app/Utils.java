package com.cloudinary.android.sample.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.DocumentsContract;
import androidx.core.util.Pair;
import android.view.WindowManager;

import com.cloudinary.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    public static Bitmap decodeBitmapStream(Context context, Uri uri, int reqWidth, int reqHeight) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(uri);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        is.close();
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        is = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
        is.close();
        return bitmap == null ? null : getCroppedBitmap(bitmap);
    }

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        int dimension = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        int horizontalDiff = (bitmap.getWidth() - dimension) / 2;
        int verticalDiff = (bitmap.getHeight() - dimension) / 2;
        final Rect rect = new Rect(-horizontalDiff, -verticalDiff, bitmap.getWidth() - horizontalDiff, bitmap.getHeight() - verticalDiff);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(dimension / 2, dimension / 2, dimension / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight && width > reqWidth) {

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

    public static Uri toUri(Context context, int resourceId) {
        Resources res = context.getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(resourceId)
                + '/' + res.getResourceTypeName(resourceId) + '/' + res.getResourceEntryName(resourceId));
    }

    public static int getScreenWidth(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        window.getDefaultDisplay().getSize(point);
        return point.x;
    }

    public static Pair<String, String> getResourceNameAndType(Context context, Uri uri) {
        Cursor cursor = null;
        String type = null;
        String name = null;

        try {
            cursor = context.getContentResolver().query(uri, new String[]{DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                type = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (StringUtils.isNotBlank(type)) {
                    type = type.substring(0, type.indexOf('/'));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (StringUtils.isBlank(type)) {
            type = "image";
        }
        return new Pair<>(name, type);
    }

    public static void openMediaChooser(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/jpg", "image/png", "video/*"});
        intent.setType("(*/*");
        activity.startActivityForResult(intent, requestCode);
    }
}
