package cloudinary.android.sample;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.cloudinary.android.cldvideoplayer.CldVideoPlayer;
import com.google.android.exoplayer2.ExoPlayer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CldVideoPlayerInstrumentedTest {

    private Context context;

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
        assert cldVideoPlayer[0].getUrl().contains("publicId.m3u8");
    }
}