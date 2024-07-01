package com.cloudinary.sample.helpers;

import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {

    public static void setMediaManager(Context context, String cloudName) {
        Map config = new HashMap();
        config.put("cloud_name", cloudName);
        config.put("secure", true);
        MediaManager.init(context, config);
    }
}
