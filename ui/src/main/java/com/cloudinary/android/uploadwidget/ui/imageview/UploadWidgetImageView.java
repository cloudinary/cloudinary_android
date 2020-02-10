package com.cloudinary.android.uploadwidget.ui.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cloudinary.android.uploadwidget.model.CropPoints;
import com.cloudinary.android.uploadwidget.model.BitmapManager;
import com.cloudinary.android.uploadwidget.model.Dimensions;

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
    private int rotationAngle;
    private boolean sizeChanged;

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

        if (imageUri != null) {
            setBitmap(w, h);
        }

        sizeChanged = true;
    }

    private void setBitmap(int w, int h) {
        BitmapManager.get().load(getContext(), imageUri, w, h, new BitmapManager.LoadCallback() {
            @Override
            public void onSuccess(Bitmap bitmap, Dimensions originalDimensions) {
                UploadWidgetImageView.this.bitmap = bitmap;
                if (rotationAngle != 0) {
                    rotateBitmapBy(rotationAngle);
                }
                updateImageViewBitmap();

                originalWidth = originalDimensions.getWidth();
            }

            @Override
            public void onFailure() { }
        });
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

        if (imageUri != null && sizeChanged) {
            setBitmap(getWidth(), getHeight());
        }
    }

    /**
     * Show the crop overlay
     */
    public void showCropOverlay() {
        cropOverlayView.setVisibility(VISIBLE);
    }

    /**
     * Hide the crop overlay
     */
    public void hideCropOverlay() {
        cropOverlayView.setVisibility(INVISIBLE);
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
     * Return a result bitmap of the editing changes, or the source if none was made.
     */
    public Bitmap getResultBitmap() {
        CropPoints cropPoints = cropOverlayView.getCropPoints();
        Point p1 = cropPoints.getPoint1();
        Point p2 = cropPoints.getPoint2();

        Bitmap resultBitmap;
        if (p2.x - p1.x != bitmap.getWidth() || p2.y - p1.y != bitmap.getHeight()) {
            resultBitmap = Bitmap.createBitmap(bitmap,
                    p1.x - bitmapBounds.left,
                    p1.y - bitmapBounds.top,
                    p2.x - p1.x,
                    p2.y - p1.y);
        } else {
            resultBitmap = bitmap;
        }

        return resultBitmap;
    }

    /**
     * Rotate the image by 90 degrees.
     */
    public void rotateImage() {
        rotationAngle = (rotationAngle + 90) % 360;
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
}
