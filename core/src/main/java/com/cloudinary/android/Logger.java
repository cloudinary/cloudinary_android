package com.cloudinary.android;

import android.util.Log;

public class Logger {
    static LogLevel logLevel = LogLevel.ERROR;

    public static void i(String tag, String message) {
        if (shouldLog(LogLevel.INFO)) {
            Log.i(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (shouldLog(LogLevel.DEBUG)) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (shouldLog(LogLevel.ERROR)) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable t) {
        if (shouldLog(LogLevel.ERROR)) {
            Log.e(tag, message, t);
        }
    }

    private static boolean shouldLog(LogLevel level) {
        return level.ordinal() <= logLevel.ordinal();
    }
}
