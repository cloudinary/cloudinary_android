package com.cloudinary.android.uploadwidget.model;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Points to be used for cropping preprocessing.
 */
public class CropPoints implements Parcelable {

    private Point p1;
    private Point p2;

    public CropPoints(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public void setPoint1(Point p1) { this.p1 = p1; }

    public Point getPoint1() { return p1; }

    public void setPoint2(Point p2) { this.p2 = p2; }

    public Point getPoint2() { return p2; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(p1, flags);
        dest.writeParcelable(p2, flags);
    }

    public static final Creator<CropPoints> CREATOR = new Creator<CropPoints>() {
        @Override
        public CropPoints createFromParcel(Parcel in) {
            return new CropPoints(in);
        }

        @Override
        public CropPoints[] newArray(int size) {
            return new CropPoints[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected CropPoints(Parcel in) {
        p1 = in.readParcelable(Point.class.getClassLoader());
        p2 = in.readParcelable(Point.class.getClassLoader());
    }
}
