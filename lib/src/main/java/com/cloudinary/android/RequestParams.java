package com.cloudinary.android;

/**
 * Classes that implement {@link BackgroundRequestStrategy} will most probably need their own parameter collection to implement this interface
 * so it can be easily adapted to the common params structure. This is used as both in and out params.
 */
interface RequestParams {
    void putString(String key, String value);

    void putInt(String key, int value);

    void putLong(String key, long value);

    void putBoolean(String key, boolean value);

    String getString(String key, String defaultValue);

    int getInt(String key, int defaultValue);

    long getLong(String key, long defaultValue);

    boolean getBoolean(String key, boolean defaultValue);
}
