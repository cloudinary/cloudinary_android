package com.cloudinary.android.uploadwidget.ui.imageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cloudinary.android.uploadwidget.model.CropPoints;
import com.cloudinary.android.uploadwidget.ui.imageview.gestures.CropOverlayGestureCallback;
import com.cloudinary.android.uploadwidget.ui.imageview.gestures.CropOverlayGestureDetector;

/**
 * Represents the crop overlay which covers the image with the cropping rectangle, while dimming the surrounding area.
 */
class CropOverlayView extends View implements CropOverlayGestureCallback {

    private static final int NUMBER_OF_GUIDELINES = 4;
    private static final int CORNER_HANDLE_LENGTH = 50;
    private static final int SIDE_HANDLE_LENGTH = 40;
    private static final int HANDLE_OFFSET_FROM_OVERLAY = 5;
    private static final int HANDLE_THICKNESS = 10;
    private static final int MIN_OVERLAY_SIZE = 5;

    private final Path dottedPath = new Path();

    // Side handles
    private final Path leftHandlePath = new Path();
    private final Path topHandlePath = new Path();
    private final Path rightHandlePath = new Path();
    private final Path bottomHandlePath = new Path();

    // Corner handles
    private final Path topLeftHandlePath = new Path();
    private final Path topRightHandlePath = new Path();
    private final Path bottomRightHandlePath = new Path();
    private final Path bottomLeftHandlePath = new Path();

    private final Paint dottedPaint = new Paint();
    private final Paint guidelinesPaint = new Paint();
    private final Paint handlePaint = new Paint();
    private final Paint dimBackgroundPaint = new Paint();

    private final Rect overlay = new Rect();
    private CropOverlayGestureDetector gestureDetector;
    private Rect imageBounds;
    private boolean isAspectRatioLocked;

    public CropOverlayView(Context context) {
        super(context);
        init();
    }

    public CropOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gestureDetector = new CropOverlayGestureDetector(overlay, this);

        dottedPaint.setColor(Color.WHITE);
        dottedPaint.setStyle(Paint.Style.STROKE);
        dottedPaint.setStrokeWidth(5);
        dottedPaint.setPathEffect(new DashPathEffect(new float[]{5, 10}, 0));

        guidelinesPaint.setColor(Color.WHITE);
        guidelinesPaint.setStyle(Paint.Style.STROKE);

        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);

        dimBackgroundPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 125));
        dimBackgroundPaint.setStyle(Paint.Style.FILL);

        setVisibility(INVISIBLE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!overlay.isEmpty()) {
            drawDottedPath(canvas);
            drawGuidelines(canvas);
            drawHandles(canvas);
            dimSurroundingBackground(canvas);
        }
    }

    private void drawDottedPath(Canvas canvas) {
        dottedPath.reset();
        dottedPath.moveTo(overlay.left, overlay.top);
        dottedPath.lineTo(overlay.right, overlay.top);
        dottedPath.lineTo(overlay.right, overlay.bottom);
        dottedPath.lineTo(overlay.left, overlay.bottom);
        dottedPath.lineTo(overlay.left, overlay.top);

        canvas.drawPath(dottedPath, dottedPaint);
    }

    private void drawGuidelines(Canvas canvas) {
        // Vertical guidelines
        int widthDiff = overlay.width() / NUMBER_OF_GUIDELINES;
        for (int guidelineX = overlay.left; guidelineX <= overlay.right; guidelineX += widthDiff) {
            canvas.drawLine(guidelineX, overlay.top, guidelineX, overlay.bottom, guidelinesPaint);
        }

        // Horizontal guidelines
        int heightDiff = overlay.height() / NUMBER_OF_GUIDELINES;
        for (int guidelineY = overlay.top; guidelineY <= overlay.bottom; guidelineY += heightDiff) {
            canvas.drawLine(overlay.left, guidelineY, overlay.right, guidelineY, guidelinesPaint);
        }
    }

    private void drawHandles(Canvas canvas) {
        // Middle handles
        leftHandlePath.reset();
        float[] leftRadiusValues = new float[]{0, 0, 10f, 10f, 10f, 10f, 0, 0};
        leftHandlePath.addRoundRect(new RectF(
                        overlay.left - HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.centerY() - SIDE_HANDLE_LENGTH / 2,
                        overlay.left + HANDLE_THICKNESS,
                        overlay.centerY() + SIDE_HANDLE_LENGTH / 2),
                leftRadiusValues, Path.Direction.CCW);
        canvas.drawPath(leftHandlePath, handlePaint);

        topHandlePath.reset();
        float[] topRadiusValues = new float[]{0, 0, 0, 0, 10f, 10f, 10f, 10f};
        topHandlePath.addRoundRect(new RectF(
                        overlay.centerX() - SIDE_HANDLE_LENGTH / 2,
                        overlay.top - HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.centerX() + SIDE_HANDLE_LENGTH / 2,
                        overlay.top + HANDLE_THICKNESS),
                topRadiusValues, Path.Direction.CCW);
        canvas.drawPath(topHandlePath, handlePaint);

        rightHandlePath.reset();
        float[] rightRadiusValues = new float[]{10f, 10f, 0, 0, 0, 0, 10f, 10f};
        rightHandlePath.addRoundRect(new RectF(
                        overlay.right - HANDLE_THICKNESS,
                        overlay.centerY() - SIDE_HANDLE_LENGTH / 2,
                        overlay.right + HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.centerY() + SIDE_HANDLE_LENGTH / 2),
                rightRadiusValues, Path.Direction.CCW);
        canvas.drawPath(rightHandlePath, handlePaint);

        bottomHandlePath.reset();
        float[] bottomRadiusValues = new float[]{10f, 10f, 10f, 10f, 0, 0, 0, 0};
        bottomHandlePath.addRoundRect(new RectF(
                        overlay.centerX() - SIDE_HANDLE_LENGTH / 2,
                        overlay.bottom - HANDLE_THICKNESS,
                        overlay.centerX() + SIDE_HANDLE_LENGTH / 2,
                        overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY),
                bottomRadiusValues, Path.Direction.CCW);
        canvas.drawPath(bottomHandlePath, handlePaint);

        // Corner handles
        topLeftHandlePath.reset();
        float[] topLeftHandleRadiusValues = new float[]{0, 0, 0, 0, 10f, 10f, 0, 0};
        topLeftHandlePath.addRoundRect(new RectF(
                        overlay.left - HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.top - HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.left + HANDLE_THICKNESS,
                        overlay.top + CORNER_HANDLE_LENGTH),
                topLeftHandleRadiusValues, Path.Direction.CCW);
        topLeftHandlePath.addRoundRect(new RectF(
                        overlay.left - HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.top - HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.left + CORNER_HANDLE_LENGTH,
                        overlay.top + HANDLE_THICKNESS),
                topLeftHandleRadiusValues, Path.Direction.CCW);
        canvas.drawPath(topLeftHandlePath, handlePaint);

        topRightHandlePath.reset();
        float[] topRightRadiusValues = new float[]{0, 0, 0, 0, 0, 0, 10f, 10f};
        topRightHandlePath.addRoundRect(new RectF(
                        overlay.right - CORNER_HANDLE_LENGTH,
                        overlay.top - HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.right + HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.top + HANDLE_THICKNESS),
                topRightRadiusValues, Path.Direction.CCW);
        topRightHandlePath.addRoundRect(new RectF(
                        overlay.right - HANDLE_THICKNESS,
                        overlay.top - HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.right + HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.top + CORNER_HANDLE_LENGTH),
                topRightRadiusValues, Path.Direction.CCW);
        canvas.drawPath(topRightHandlePath, handlePaint);

        bottomRightHandlePath.reset();
        float[] bottomRightRadiusValues = new float[]{10f, 10f, 0, 0, 0, 0, 0, 0};
        bottomRightHandlePath.addRoundRect(new RectF(
                        overlay.right - HANDLE_THICKNESS,
                        overlay.bottom - CORNER_HANDLE_LENGTH,
                        overlay.right + HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY),
                bottomRightRadiusValues, Path.Direction.CCW);
        bottomRightHandlePath.addRoundRect(new RectF(overlay.right - CORNER_HANDLE_LENGTH,
                        overlay.bottom - HANDLE_THICKNESS,
                        overlay.right + HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY),
                bottomRightRadiusValues, Path.Direction.CCW);
        canvas.drawPath(bottomRightHandlePath, handlePaint);

        bottomLeftHandlePath.reset();
        float[] bottomLeftRadiusValues = new float[]{0, 0, 10f, 10f, 0, 0, 0, 0};
        bottomLeftHandlePath.addRoundRect(new RectF(
                        overlay.left - HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.bottom - CORNER_HANDLE_LENGTH,
                        overlay.left + HANDLE_THICKNESS,
                        overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY),
                bottomLeftRadiusValues, Path.Direction.CCW);
        bottomLeftHandlePath.addRoundRect(new RectF(
                        overlay.left - HANDLE_OFFSET_FROM_OVERLAY,
                        overlay.bottom - HANDLE_THICKNESS,
                        overlay.left + CORNER_HANDLE_LENGTH,
                        overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY),
                bottomLeftRadiusValues, Path.Direction.CCW);
        canvas.drawPath(bottomLeftHandlePath, handlePaint);
    }

    private void dimSurroundingBackground(Canvas canvas) {
        // left
        canvas.drawRect(0, overlay.top - HANDLE_OFFSET_FROM_OVERLAY, overlay.left - HANDLE_OFFSET_FROM_OVERLAY, overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY, dimBackgroundPaint);
        // top
        canvas.drawRect(0, 0, getWidth(), overlay.top - HANDLE_OFFSET_FROM_OVERLAY, dimBackgroundPaint);
        // right
        canvas.drawRect(overlay.right + HANDLE_OFFSET_FROM_OVERLAY, overlay.top - HANDLE_OFFSET_FROM_OVERLAY, getWidth(), overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY, dimBackgroundPaint);
        // bottom
        canvas.drawRect(0, overlay.bottom + HANDLE_OFFSET_FROM_OVERLAY, getWidth(), getHeight(), dimBackgroundPaint);
    }

    /**
     * Returns true if the aspect ratio is locked.
     */
    public boolean isAspectRatioLocked() {
        return isAspectRatioLocked;
    }

    /**
     * Set the crop overlay's aspect ratio locking.
     * @param aspectRatioLocked Whether the aspect ratio should be locked.
     */
    public void setAspectRatioLocked(boolean aspectRatioLocked) {
        isAspectRatioLocked = aspectRatioLocked;
    }

    /**
     * Get the current crop overlay's cropping points.
     * @return Crop points that make the crop overlay diagonal.
     */
    public CropPoints getCropPoints() {
        return new CropPoints(new Point(overlay.left, overlay.top), new Point(overlay.right, overlay.bottom));
    }

    /**
     * Reset the crop overlay to cover its entire bounds.
     */
    public void reset() {
        overlay.set(imageBounds);
        invalidate();
    }

    /**
     * Set the crop overlay bounds, resetting the crop overlay to fit the given bounds.
     * @param imageBounds Bounds for the crop overlay.
     */
    public void set(Rect imageBounds) {
        this.imageBounds = imageBounds;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event, isAspectRatioLocked);
        return true;
    }

    @Override
    public void onOverlayDragged(int distanceX, int distanceY) {
        if (overlay.left + distanceX <= imageBounds.left || overlay.right + distanceX >= imageBounds.right) {
            distanceX = 0;
        }
        if (overlay.top + distanceY <= imageBounds.top || overlay.bottom + distanceY >= imageBounds.bottom) {
            distanceY = 0;
        }

        overlay.offset(distanceX, distanceY);
        invalidate();
    }

    @Override
    public void onOverlaySizeChanged(int left, int top, int right, int bottom) {
        if (left >= imageBounds.left && top >= imageBounds.top && right <= imageBounds.right && bottom <= imageBounds.bottom
                && right - left > MIN_OVERLAY_SIZE && bottom - top > MIN_OVERLAY_SIZE) {
            overlay.set(left, top, right, bottom);
            invalidate();
        }
    }
}
