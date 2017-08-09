package com.cloudinary.android.sample.persist;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cloudinary.android.sample.app.MainApplication;

public class Prefs {
    private static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.get());
    }

    private static SharedPreferences.Editor getEditor() {
        return getPrefs().edit();
    }

    public static boolean firstRun() {
        return getPrefs().getBoolean("firstRun", true);
    }

    public static void setFirstRun(boolean firstRun) {
        getEditor().putBoolean("firstRun", firstRun).apply();
    }
}
