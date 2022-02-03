package com.cloudinary.android;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;
import com.cloudinary.utils.StringUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

@RunWith(AndroidJUnit4ClassRunner.class)
public class AnalyticsTest extends AbstractTest {

    private static boolean initialized = false;

    @BeforeClass
    public synchronized static void setup() {
        MediaManager.get().getCloudinary().analytics.setSDKSemver("2.0.0");
        MediaManager.get().getCloudinary().analytics.setTechVersion("13.0.0");
    }

    @Test
    public void testAnalyticsURL() {
        String url = MediaManager.get().getCloudinary().url().generate("sample");
        Assert.assertEquals(url, "https://res.cloudinary.com/sdk-test/image/upload/sample?_a=AFAACAN0");
    }

    @Test
    public void testAnalyticsFalseURL() {
        MediaManager.get().getCloudinary().config.analytics = false;
        String url = MediaManager.get().getCloudinary().url().generate("sample");
        Assert.assertEquals(url, "https://res.cloudinary.com/sdk-test/image/upload/sample");
    }
}
