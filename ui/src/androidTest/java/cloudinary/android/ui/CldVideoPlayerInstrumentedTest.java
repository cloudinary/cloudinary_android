package cloudinary.android.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.cldvideoplayer.CldVideoPlayer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CldVideoPlayerInstrumentedTest {

    private Context context;

    @BeforeClass
    public synchronized static void initLibrary() {
        MediaManager.init(InstrumentationRegistry.getInstrumentation().getTargetContext());
        MediaManager.get().getCloudinary().config.cloudName = "demo";
    }

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void testPlayerInitialization() throws InterruptedException {
        // Create a CountDownLatch with a count of 1
        CountDownLatch latch = new CountDownLatch(1);
        final CldVideoPlayer[] cldVideoPlayer = {null};
        new Handler(Looper.getMainLooper()).post(() -> {
            cldVideoPlayer[0] = new CldVideoPlayer(context, "publicId");
            latch.countDown();
        });

        boolean latchReleased = latch.await(10, TimeUnit.SECONDS);
        assert latchReleased : "CountDownLatch was not released within the timeout.";
        assert cldVideoPlayer[0] != null;
        assert cldVideoPlayer[0].getPlayer() != null;
        assert cldVideoPlayer[0].getUrl().contains("publicId.m3u8");
        assert cldVideoPlayer[0].getUrl().contains("sp_auto");
    }

    @Test
    public void testDisableStreamProfileAutoWithTransformation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final CldVideoPlayer[] cldVideoPlayer = {null};
        new Handler(Looper.getMainLooper()).post(() -> {
            cldVideoPlayer[0] = new CldVideoPlayer(context, "publicId", new Transformation().effect("sepia"));
            latch.countDown();
        });

        boolean latchReleased = latch.await(10, TimeUnit.SECONDS);
    Log.d("Test", cldVideoPlayer[0].getUrl());
        assert latchReleased : "CountDownLatch was not released within the timeout.";
        assert cldVideoPlayer[0] != null;
        assert !cldVideoPlayer[0].getUrl().contains("publicId.m3u8");
        assert !cldVideoPlayer[0].getUrl().contains("sp_auto");
    }

    @Test
    public void testInitializePlayerWithURL() throws InterruptedException, MalformedURLException {
        URL testUrl = new URL("https://res.cloudinary.com/test/image/upload/sample");
        CountDownLatch latch = new CountDownLatch(1);
        final CldVideoPlayer[] cldVideoPlayer = {null};
        new Handler(Looper.getMainLooper()).post(() -> {
            cldVideoPlayer[0] = new CldVideoPlayer(context, testUrl);
            latch.countDown();
        });

        boolean latchReleased = latch.await(10, TimeUnit.SECONDS);
        Log.d("Test", cldVideoPlayer[0].getUrl());
        assert latchReleased : "CountDownLatch was not released within the timeout.";
        assert cldVideoPlayer[0] != null;
        assert Objects.equals(cldVideoPlayer[0].getUrl(), testUrl.toString());
    }
    
    @Test
    public void testInitializePlayerAutoStreamingProfileDisabled() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final CldVideoPlayer[] cldVideoPlayer = {null};
        new Handler(Looper.getMainLooper()).post(() -> {
            cldVideoPlayer[0] = new CldVideoPlayer(context, "publicId", null, false);
            latch.countDown();
        });

        boolean latchReleased = latch.await(10, TimeUnit.SECONDS);
        Log.d("Test", cldVideoPlayer[0].getUrl());
        assert latchReleased : "CountDownLatch was not released within the timeout.";
        assert cldVideoPlayer[0] != null;
        assert !cldVideoPlayer[0].getUrl().contains("publicId.m3u8");
        assert !cldVideoPlayer[0].getUrl().contains("sp_auto");
    }

    @Test
    public void testInitializePlayerAutoStreamingProfileEnabled() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final CldVideoPlayer[] cldVideoPlayer = {null};
        new Handler(Looper.getMainLooper()).post(() -> {
            cldVideoPlayer[0] = new CldVideoPlayer(context, "publicId", null, true);
            latch.countDown();
        });

        boolean latchReleased = latch.await(10, TimeUnit.SECONDS);
        Log.d("Test", cldVideoPlayer[0].getUrl());
        assert latchReleased : "CountDownLatch was not released within the timeout.";
        assert cldVideoPlayer[0] != null;
        assert cldVideoPlayer[0].getUrl().contains("publicId.m3u8");
        assert cldVideoPlayer[0].getUrl().contains("sp_auto");
    }
    
    @Test
    public void testInitializePlayerAutoStreamingProfileEnabledAndTransformation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final CldVideoPlayer[] cldVideoPlayer = {null};
        new Handler(Looper.getMainLooper()).post(() -> {
            cldVideoPlayer[0] = new CldVideoPlayer(context, "publicId", new Transformation().effect("loop"), true);
            latch.countDown();
        });

        boolean latchReleased = latch.await(10, TimeUnit.SECONDS);
        Log.d("Test", cldVideoPlayer[0].getUrl());
        assert latchReleased : "CountDownLatch was not released within the timeout.";
        assert cldVideoPlayer[0] != null;
        assert cldVideoPlayer[0].getUrl().contains("e_loop");
        assert !cldVideoPlayer[0].getUrl().contains("publicId.m3u8");
        assert !cldVideoPlayer[0].getUrl().contains("sp_auto");
    }
}