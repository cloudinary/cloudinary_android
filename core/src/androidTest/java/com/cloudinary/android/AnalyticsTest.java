package com.cloudinary.android;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4ClassRunner.class)
public class AnalyticsTest extends AbstractTest {

    @BeforeClass
    public synchronized static void setup() {
        MediaManager.get().getCloudinary().analytics.setSDKSemver("2.0.0");
        MediaManager.get().getCloudinary().analytics.setTechVersion("33.0");
        MediaManager.get().getCloudinary().analytics.osVersion = "33.0";
        MediaManager.get().getCloudinary().config.secure = true;
    }

    @Test
    public void testAnalyticsURL() {
        MediaManager.get().getCloudinary().config.analytics = true;
        String url = MediaManager.get().getCloudinary().url().generate("sample");
        Assert.assertTrue(url.contains("a=DAFAACAhAhA0"));
    }

    @Test
    public void testAnalyticsFalseURL() {
        MediaManager.get().getCloudinary().config.analytics = false;
        String url = MediaManager.get().getCloudinary().url().generate("sample");
        Assert.assertFalse(url.contains("a=AFAACAN0"));
    }
}
