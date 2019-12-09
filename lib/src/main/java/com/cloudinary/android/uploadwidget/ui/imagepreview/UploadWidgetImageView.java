package com.cloudinary.android.uploadwidget.ui.imagepreview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cloudinary.android.uploadwidget.CropPoints;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Previews the Upload Widget's image with editing capabilities.
 */
public class UploadWidgetImageView extends FrameLayout {

    private CropOverlayView cropOverlayView;
    private ImageView imageView;
    private Uri imageUri;
    private Bitmap bitmap;
    private Rect bitmapBounds = new Rect();
    private int originalWidth;
    private boolean isCropStarted;
    private boolean isCroppedBitmapDisplayed;
    private int rotationAngle;

    public UploadWidgetImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public UploadWidgetImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UploadWidgetImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        imageView = new ImageView(context);
        imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        addView(imageView);

        cropOverlayView = new CropOverlayView(context);
        cropOverlayView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(cropOverlayView);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (imageUri != null && !isCroppedBitmapDisplayed) {
            setBitmap(w, h);
        }
    }

    private void setBitmap(int w, int h) {
        try {
            bitmap = decodeSampledBitmapFromUri(imageUri, w, h);
            if (rotationAngle != 0) {
                rotateBitmapBy(rotationAngle);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        updateImageViewBitmap();
    }

    private void updateImageViewBitmap() {
        imageView.setImageBitmap(bitmap);
        setBitmapBounds();
        cropOverlayView.reset();
    }

    /**
     * Returns true if the aspect ratio is locked.
     */
    public boolean isAspectRatioLocked() {
        return cropOverlayView.isAspectRatioLocked();
    }

    /**
     * Set the crop overlay's aspect ratio locking.
     *
     * @param aspectRatioLocked Whether the aspect ratio should be locked.
     */
    public void setAspectRatioLocked(boolean aspectRatioLocked) {
        cropOverlayView.setAspectRatioLocked(aspectRatioLocked);
    }

    /**
     * Set an image from the given Uri.
     *
     * @param imageUri Uri of the image to be displayed.
     */
    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    /**
     * Start the cropping, showing the crop overlay fitted to the image bounds.
     */
    public void startCropping() {
        cropOverlayView.setVisibility(VISIBLE);
        isCropStarted = true;
    }

    /**
     * Stop the cropping, hiding the crop overlay and setting the original bitmap.
     */
    public void stopCropping() {
        isCropStarted = false;
        setAspectRatioLocked(false);
        cropOverlayView.setVisibility(INVISIBLE);

        if (isCroppedBitmapDisplayed) {
            setBitmap(getWidth(), getHeight());
            isCroppedBitmapDisplayed = false;
        }
        if (rotationAngle != 0) {
            rotateBitmapBy(-rotationAngle);
        }
        rotationAngle = 0;
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        updateImageViewBitmap();
    }

    /**
     * Whether it is during cropping or not.
     *
     * @return true if {@link UploadWidgetImageView#startCropping()} was called, and false if cropping was never started or {@link UploadWidgetImageView#stopCropping()} was called.
     */
    public boolean isCropStarted() {
        return isCropStarted;
    }

    /**
     * Get the current crop overlay's cropping points.
     *
     * @return Crop points that make the crop overlay diagonal.
     */
    public CropPoints getCropPoints() {
        float ratio = (float) originalWidth / bitmap.getWidth();
        if (rotationAngle % 180 != 0) {
            ratio = (float) originalWidth / bitmap.getHeight();
        }

        CropPoints cropPoints = cropOverlayView.getCropPoints();
        Point p1 = cropPoints.getPoint1();
        Point p2 = cropPoints.getPoint2();
        p1.x = (int) ((p1.x - bitmapBounds.left) * ratio);
        p1.y = (int) ((p1.y - bitmapBounds.top) * ratio);
        p2.x = (int) ((p2.x - bitmapBounds.left) * ratio);
        p2.y = (int) ((p2.y - bitmapBounds.top) * ratio);

        return cropPoints;
    }

    /**
     * Crop the image, hiding the crop overlay.
     */
    public void cropImage() {
        CropPoints cropPoints = cropOverlayView.getCropPoints();
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap,
                cropPoints.getPoint1().x - bitmapBounds.left,
                cropPoints.getPoint1().y - bitmapBounds.top,
                cropPoints.getPoint2().x - cropPoints.getPoint1().x,
                cropPoints.getPoint2().y - cropPoints.getPoint1().y);

        imageView.setImageBitmap(croppedBitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        cropOverlayView.setVisibility(INVISIBLE);
        isCroppedBitmapDisplayed = true;
    }

    /**
     * Rotate the image by 90 degrees.
     */
    public void rotateImage() {
        rotationAngle += 90 % 360;
        rotateBitmapBy(90);
        updateImageViewBitmap();
    }

    /**
     * Returns the current image rotation angle.
     */
    public int getRotationAngle() {
        return rotationAngle;
    }

    private void rotateBitmapBy(int degrees) {
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);

        float scale;
        if (degrees % 180 != 0) {
            scale = Math.max(bitmap.getWidth() / (float) getHeight(), bitmap.getHeight() / (float) getWidth());
        } else {
            scale = Math.max(bitmap.getWidth() / (float) getWidth(), bitmap.getHeight() / (float) getHeight());
        }

        float dstWidth = bitmap.getWidth() / scale;
        float dstHeight = bitmap.getHeight() / scale;
        if (bitmap.getWidth() != dstWidth || bitmap.getHeight() != dstHeight) {
            final float sx = dstWidth / (float) bitmap.getWidth();
            final float sy = dstHeight / (float) bitmap.getHeight();
            matrix.postScale(sx, sy);
        }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    private void setBitmapBounds() {
        int left = (getWidth() - bitmap.getWidth()) / 2;
        int top = (getHeight() - bitmap.getHeight()) / 2;
        bitmapBounds.set(left, top, left + bitmap.getWidth(), top + bitmap.getHeight());
        cropOverlayView.set(bitmapBounds);
    }

    private Bitmap getScaledBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
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

    private Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) throws FileNotFoundException {
        Bitmap bitmap;
        InputStream justDecodeBoundsStream = null;
        InputStream sampledBitmapStream = null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            justDecodeBoundsStream = getUriInputStream(uri);
            BitmapFactory.decodeStream(justDecodeBoundsStream, null, options);
            originalWidth = options.outWidth;

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            sampledBitmapStream = getUriInputStream(uri);
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

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

    private InputStream getUriInputStream(Uri uri) throws FileNotFoundException {
        return getContext().getContentResolver().openInputStream(uri);
    }

}
