package com.cloudinary.android;

import android.support.test.InstrumentationRegistry;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cloudinary.Cloudinary;
import com.cloudinary.Url;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;


public class ResponsiveTest extends AbstractTest {

    private static Cloudinary cloudinary;

    @BeforeClass
    public static void setUp() throws Exception {
        String url = Utils.cloudinaryUrlFromContext(InstrumentationRegistry.getContext());
        cloudinary = new Cloudinary(url);
    }

    /**
     * Helper method to get an imageview with the given dimensions
     */
    private ImageView getImageView(int width, int height) {
        ImageView imageView = new ImageView(InstrumentationRegistry.getTargetContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        imageView.setLayoutParams(params);
        return imageView;
    }

    @Test
    public void testResponsiveUrl() {
        twoDimensionTest("auto", "fill", 100, 100, 200, 333, 333, "c_fill,g_auto,h_200,w_200/publicId");
        twoDimensionTest("center", "fill", 100, 100, 200, 50, 50, "c_fill,g_center,h_100,w_100/publicId");
        twoDimensionTest("auto", "thumb", 100, 750, 1100, 421, 521, "c_thumb,g_auto,h_750,w_750/publicId");
        twoDimensionTest("north", "fit", 100, 600, 1100, 421, 821, "c_fit,g_north,h_900,w_600/publicId");
        twoDimensionTest("auto", "fill", 50, 600, 1100, 421, 821, "c_fill,g_auto,h_850,w_600/publicId");
        twoDimensionTest("auto", "fill", 50, 0, 1100, 450, 821, "c_fill,g_auto,h_850,w_450/publicId");
        twoDimensionTest("auto", "fill", 50, 0, 1100, 451, 821, "c_fill,g_auto,h_850,w_500/publicId");
        twoDimensionTest("auto", "fill", 50, 0, 1100, 449, 821, "c_fill,g_auto,h_850,w_450/publicId");

        onlyWidthTest("auto", "fill", 50, 0, 1100, 449, 821, "c_fill,g_auto,w_450/publicId");
        onlyWidthTest("auto", "fill", 50, 600, 1100, 449, 821, "c_fill,g_auto,w_600/publicId");
        onlyWidthTest("auto", "fill", 50, 100, 1100, 2000, 821, "c_fill,g_auto,w_1100/publicId");
    }

    private void twoDimensionTest(String gravity, String cropMode, int stepSize, int minDimension, int maxDimension, int viewWidth, int viewHeight, final String expectedUrlSuffix) {
        final StatefulCallback callback = new StatefulCallback();

        new ResponsiveUrl(cloudinary, ResponsiveUrl.Dimension.all)
                .cropMode(cropMode)
                .gravity(gravity)
                .stepSize(stepSize)
                .minDimension(minDimension)
                .maxDimension(maxDimension)
                .generate("publicId", getImageView(viewWidth, viewHeight), callback);

        Awaitility.await().atMost(Duration.ONE_SECOND).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return callback.url.endsWith(expectedUrlSuffix);
            }
        });
    }

    private void onlyWidthTest(String gravity, String cropMode, int stepSize, int minDimension, int maxDimension, int viewWidth, int viewHeight, final String expectedUrlSuffix) {
        final StatefulCallback callback = new StatefulCallback();

        new ResponsiveUrl(cloudinary, ResponsiveUrl.Dimension.width)
                .cropMode(cropMode)
                .gravity(gravity)
                .stepSize(stepSize)
                .minDimension(minDimension)
                .maxDimension(maxDimension)
                .generate("publicId", getImageView(viewWidth, viewHeight), callback);

        Awaitility.await().atMost(Duration.ONE_SECOND).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return callback.url.endsWith(expectedUrlSuffix);
            }
        });
    }

    private final class StatefulCallback implements ResponsiveUrl.Callback {
        public String url;

        @Override
        public void onUrlReady(Url url) {
            this.url = url.generate();
        }
    }
}