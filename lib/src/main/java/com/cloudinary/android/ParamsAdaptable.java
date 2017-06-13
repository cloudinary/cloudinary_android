package com.cloudinary.android;

/***
 * Classes that implement {@link BackgroundRequestStrategy} will most probably need their own parameter collection to implement this interface
 * so it can be easily adapted to the common params structure.
 */
public interface ParamsAdaptable {
    void putString(String key, String value);
    void putInt (String key, int value);

    String getString(String key, String defaultValue);

    int getInt(String key, int defaultValue);
}
