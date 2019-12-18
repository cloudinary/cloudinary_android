package com.cloudinary.android.uploadwidget.ui;

import com.cloudinary.android.uploadwidget.CropPoints;

public class CropRotateResult {
    private int rotationAngle;
    private CropPoints cropPoints;

    public CropRotateResult(int rotationAngle, CropPoints cropPoints) {
        this.rotationAngle = rotationAngle;
        this.cropPoints = cropPoints;
    }

    public int getRotationAngle() {
        return rotationAngle;
    }

    public CropPoints getCropPoints() {
        return cropPoints;
    }
}
