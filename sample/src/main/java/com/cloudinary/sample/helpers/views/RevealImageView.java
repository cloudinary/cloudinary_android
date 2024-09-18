package com.cloudinary.sample.helpers.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class RevealImageView extends androidx.appcompat.widget.AppCompatImageView {

    private Bitmap leftImage;
    private Bitmap rightImage;
    private Bitmap scaledLeftImage;
    private Bitmap scaledRightImage;
    private Paint paint;
    private float pct = 0.5f;

    public RevealImageView(Context context) {
        super(context);
        init();
    }

    public RevealImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RevealImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    public void setLeftImage(Bitmap bitmap) {
        this.leftImage = bitmap;
        scaleBitmaps();
        invalidate();
    }

    public void setRightImage(Bitmap bitmap) {
        this.rightImage = bitmap;
        scaleBitmaps();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                pct = x / getWidth();
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (scaledLeftImage != null && scaledRightImage != null) {
            int width = getWidth();
            int height = getHeight();

            // Draw left image
            Rect leftSrc = new Rect(0, 0, (int) (width * pct), height);
            Rect leftDst = new Rect(0, 0, (int) (width * pct), height);
            canvas.drawBitmap(scaledLeftImage, leftSrc, leftDst, null);

            // Draw right image
            Rect rightSrc = new Rect((int) (width * pct), 0, width, height);
            Rect rightDst = new Rect((int) (width * pct), 0, width, height);
            canvas.drawBitmap(scaledRightImage, rightSrc, rightDst, null);

            // Draw the slider line
            paint.setXfermode(null);
            paint.setColor(0xFFFFFFFF);
            paint.setAlpha(128);
            canvas.drawRect(width * pct - 2, 0, width * pct + 2, height, paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        scaleBitmaps();
    }

    private void scaleBitmaps() {
        if (leftImage != null && rightImage != null) {
            int width = getWidth();
            int height = getHeight();
            if (width > 0 && height > 0) {
                scaledLeftImage = Bitmap.createScaledBitmap(leftImage, width, height, true);
                scaledRightImage = Bitmap.createScaledBitmap(rightImage, width, height, true);
            }
        }
    }
}
