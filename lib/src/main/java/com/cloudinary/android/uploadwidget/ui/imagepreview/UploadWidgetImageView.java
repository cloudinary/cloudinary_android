package com.cloudinary.android.uploadwidget.ui.imagepreview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cloudinary.android.uploadwidget.CropPoints;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Previews the Upload Widget's image with editing capabilities.
 */
public class UploadWidgetImageView extends FrameLayout {

    private CropOverlayView cropOverlayView;
    private ImageView imageView;
    private Uri imageUri;
    private Bitmap bitmap;
    private Rect scaledBitmapBounds;
    private int originalWidth;
    private int originalHeight;
    private boolean isCropping;

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
            Bitmap originalBitmap = decodeBitmapFromUri(getContext(), imageUri);
            originalWidth = originalBitmap.getWidth();
            originalHeight = originalBitmap.getHeight();

            bitmap = scaleBitmap(originalBitmap, w, h);
            imageView.setImageBitmap(bitmap);

            adjustCropOverlayBounds();
        }
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
        requestLayout();
    }

    /**
     * Start the cropping, showing the crop overlay fitted to the image bounds.
     */
    public void startCropping() {
        cropOverlayView.setVisibility(VISIBLE);
        isCropping = true;
    }

    /**
     * Stop the cropping, hiding the crop overlay and setting the original bitmap.
     */
    public void stopCropping() {
        isCropping = false;
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageBitmap(bitmap);
        setAspectRatioLocked(false);
        cropOverlayView.setVisibility(INVISIBLE);
        cropOverlayView.reset();
    }

    /**
     * Whether it is during cropping or not.
     *
     * @return true if {@link UploadWidgetImageView#startCropping()} was called, and false if cropping was never started or {@link UploadWidgetImageView#stopCropping()} was called.
     */
    public boolean isCropping() {
        return isCropping;
    }

    /**
     * Get the current crop overlay's cropping points.
     *
     * @return Crop points that make the crop overlay diagonal.
     */
    public CropPoints getCropPoints() {
        float widthRatio = (float) originalWidth / bitmap.getWidth();
        float heightRatio = (float) originalHeight / bitmap.getHeight();

        CropPoints cropPoints = cropOverlayView.getCropPoints();
        Point p1 = cropPoints.getPoint1();
        Point p2 = cropPoints.getPoint2();
        p1.x = (int) ((p1.x - scaledBitmapBounds.left) * widthRatio);
        p1.y = (int) ((p1.y - scaledBitmapBounds.top) * heightRatio);
        p2.x = (int) ((p2.x - scaledBitmapBounds.left) * widthRatio);
        p2.y = (int) ((p2.y - scaledBitmapBounds.top) * heightRatio);

        return cropPoints;
    }

    /**
     * Performs the crop, displaying the cropped image, hiding the crop overlay.
     */
    public void cropImage() {
        CropPoints cropPoints = cropOverlayView.getCropPoints();
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap,
                cropPoints.getPoint1().x - scaledBitmapBounds.left,
                cropPoints.getPoint1().y - scaledBitmapBounds.top,
                cropPoints.getPoint2().x - cropPoints.getPoint1().x,
                cropPoints.getPoint2().y - cropPoints.getPoint1().y);
        imageView.setImageBitmap(croppedBitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        cropOverlayView.setVisibility(INVISIBLE);
    }

    private void adjustCropOverlayBounds() {
        int left = (getWidth() - bitmap.getWidth()) / 2;
        int top = (getHeight() - bitmap.getHeight()) / 2;
        scaledBitmapBounds = new Rect(left, top, left + bitmap.getWidth(), top + bitmap.getHeight());
        cropOverlayView.set(scaledBitmapBounds);
    }

    private Bitmap decodeBitmapFromUri(Context context, Uri uri) {
        InputStream is = null;

        try {
            is = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(is);
    }


    private Bitmap scaleBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        try {
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
        } catch (Exception e) {
            Log.w("AIC", "Failed to resize cropped image, return bitmap before resize", e);
        }
        return bitmap;
    }

}
