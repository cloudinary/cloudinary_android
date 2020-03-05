package com.cloudinary.android.preprocess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.RequestDispatcher;
import com.cloudinary.android.UploadContext;
import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.payload.PayloadNotFoundException;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

@RunWith(AndroidJUnit4ClassRunner.class)
public class PreprocessTest extends AbstractTest {
    private static File assetFile;

    @BeforeClass
    public static void setup() throws IOException {
        assetFile = assetToFile(TEST_IMAGE);
    }

    /**
     * This tests verifies that the preprocessing chain was correctly activated in the dispatch flow
     */
    @Test
    public void testUploadRequestPreprocessIntegration() {

        FilePayload filePayload = new FilePayload(assetFile.getAbsolutePath());
        final UploadRequest[] requests = new UploadRequest[1];
        final UploadRequest<FilePayload> request = new UploadRequest<>(new UploadContext<>(filePayload, new RequestDispatcher() {
            @Override
            public String dispatch(UploadRequest request) {
                requests[0] = request;
                return null;
            }

            @Override
            public String startNow(@NonNull Context context, UploadRequest request) {
                return null;
            }

            @Override
            public boolean cancelRequest(String requestId) {
                return false;
            }

            @Override
            public void queueRoomFreed() {

            }

            @Override
            public int cancelAllRequests() {
                return 0;
            }
        }));

        request.preprocess(ImagePreprocessChain.limitDimensionsChain(15, 15)).dispatch(InstrumentationRegistry.getInstrumentation().getTargetContext());
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (requests[0] == null) {
                    return false;
                }

                File file = (File) requests[0].getPayload().prepare(InstrumentationRegistry.getInstrumentation().getTargetContext());
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                return bitmap.getWidth() <= 15 && bitmap.getHeight() <= 15;
            }
        });
    }

    /**
     * Test that the full decode->steps->encode chain mechanism works
     *
     * @throws PreprocessException
     * @throws PayloadNotFoundException
     * @throws IOException
     */
    @Test
    public void testChain() throws PreprocessException, PayloadNotFoundException, IOException {
        FilePayload payload = new FilePayload(assetFile.getAbsolutePath());
        ImagePreprocessChain chain = ImagePreprocessChain.limitDimensionsChain(40, 40);
        String filePath = chain.execute(InstrumentationRegistry.getInstrumentation().getTargetContext(), payload);
        FileInputStream fileInputStream = InstrumentationRegistry.getInstrumentation().getTargetContext().openFileInput(filePath);
        Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
        Assert.assertEquals(40, bitmap.getWidth());
        Assert.assertEquals(8, bitmap.getHeight());
    }

    @Test
    public void testLimit() {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(assetFile.getAbsolutePath()), 1000, 500, false);
        bitmap = new Limit(100, 100).execute(context, bitmap);
        Assert.assertEquals(100, bitmap.getWidth());
        Assert.assertEquals(50, bitmap.getHeight());

        bitmap = Bitmap.createScaledBitmap(bitmap, 500, 1000, false);
        bitmap = new Limit(100, 100).execute(context, bitmap);
        Assert.assertEquals(50, bitmap.getWidth());
        Assert.assertEquals(100, bitmap.getHeight());

        bitmap = Bitmap.createScaledBitmap(bitmap, 500, 600, false);
        bitmap = new Limit(1000, 1000).execute(context, bitmap);
        Assert.assertEquals(500, bitmap.getWidth());
        Assert.assertEquals(600, bitmap.getHeight());

        bitmap = Bitmap.createScaledBitmap(bitmap, 500, 600, false);
        bitmap = new Limit(510, 360).execute(context, bitmap);
        Assert.assertEquals(300, bitmap.getWidth());
        Assert.assertEquals(360, bitmap.getHeight());

        bitmap = Bitmap.createScaledBitmap(bitmap, 500, 600, false);
        bitmap = new Limit(300, 630).execute(context, bitmap);
        Assert.assertEquals(300, bitmap.getWidth());
        Assert.assertEquals(360, bitmap.getHeight());
    }

    @Test
    public void testCrop() throws PreprocessException {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bitmap originalBitmap = Bitmap.createBitmap(BitmapFactory.decodeFile(assetFile.getAbsolutePath()));

        Point p1 = new Point(0, 0);
        Point p2 = new Point(originalBitmap.getWidth(), originalBitmap.getHeight());
        Bitmap bitmap = new Crop(p1, p2).execute(context, originalBitmap);
        Assert.assertEquals(bitmap.getWidth(), originalBitmap.getWidth());
        Assert.assertEquals(bitmap.getHeight(), originalBitmap.getHeight());
        Assert.assertEquals(bitmap, originalBitmap);

        p1 = new Point(100, 50);
        p2 = new Point(originalBitmap.getWidth(), originalBitmap.getHeight());
        bitmap = new Crop(p1, p2).execute(context, originalBitmap);
        Assert.assertEquals(bitmap.getWidth(), originalBitmap.getWidth() - 100);
        Assert.assertEquals(bitmap.getHeight(), originalBitmap.getHeight() - 50);

        p1 = new Point(0, 0);
        p2 = new Point(originalBitmap.getWidth() + 1, originalBitmap.getHeight());
        try {
            new Crop(p1, p2).execute(context, originalBitmap);
            Assert.fail("Out of bound exception should have been thrown");
        } catch (Throwable t) {
            Assert.assertTrue(t instanceof PreprocessException);
        }

        p1 = new Point(0, 0);
        p2 = new Point(0, originalBitmap.getHeight());
        try {
            new Crop(p1, p2).execute(context, originalBitmap);
            Assert.fail("Points do not make a diagonal exception should have been thrown");
        } catch (Throwable t) {
            Assert.assertTrue(t instanceof PreprocessException);
        }
    }

    @Test
    public void testRotate() {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bitmap originalBitmap = Bitmap.createBitmap(BitmapFactory.decodeFile(assetFile.getAbsolutePath()));

        Bitmap bitmap = new Rotate(90).execute(context, originalBitmap);
        Assert.assertEquals(bitmap.getWidth(), originalBitmap.getHeight());
        Assert.assertEquals(bitmap.getHeight(), originalBitmap.getWidth());

        bitmap = new Rotate(180).execute(context, originalBitmap);
        Assert.assertEquals(bitmap.getWidth(), originalBitmap.getWidth());
        Assert.assertEquals(bitmap.getHeight(), originalBitmap.getHeight());

        bitmap = new Rotate(360).execute(context, originalBitmap);
        Assert.assertEquals(bitmap.getGenerationId(), originalBitmap.getGenerationId());
    }

    @Test
    public void testDimensionsValidator() {
        int errors = 0;
        DimensionsValidator validator = new DimensionsValidator(100, 100, 1000, 1000);
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(assetFile.getAbsolutePath()), 1000, 500, false);
        errors += getError(context, validator, bitmap);
        Assert.assertEquals(0, errors);

        bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
        errors += getError(context, validator, bitmap);
        Assert.assertEquals(0, errors);

        bitmap = Bitmap.createScaledBitmap(bitmap, 1000, 1000, false);
        errors += getError(context, validator, bitmap);
        Assert.assertEquals(0, errors);

        bitmap = Bitmap.createScaledBitmap(bitmap, 50, 500, false);
        errors += getError(context, validator, bitmap);
        Assert.assertEquals(1, errors);

        bitmap = Bitmap.createScaledBitmap(bitmap, 50, 500, false);
        errors += getError(context, validator, bitmap);
        Assert.assertEquals(2, errors);

        bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
        errors += getError(context, validator, bitmap);
        Assert.assertEquals(3, errors);

        bitmap = Bitmap.createScaledBitmap(bitmap, 1500, 500, false);
        errors += getError(context, validator, bitmap);
        Assert.assertEquals(4, errors);

        bitmap = Bitmap.createScaledBitmap(bitmap, 500, 1500, false);
        errors += getError(context, validator, bitmap);
        Assert.assertEquals(5, errors);

        bitmap = Bitmap.createScaledBitmap(bitmap, 1500, 1500, false);
        errors += getError(context, validator, bitmap);
        Assert.assertEquals(6, errors);
    }



    @Test
    public void testImmediateWithPreprocess() throws IOException {
        final StatefulCallback statefulCallback = new StatefulCallback();

        UploadRequest<FilePayload> request = buildUploadRequest(buildPayload(), 1000).preprocess(ImagePreprocessChain.limitDimensionsChain(16, 16));
        MediaManager.get().registerCallback(statefulCallback);
        request.startNow(InstrumentationRegistry.getInstrumentation().getTargetContext());

        // wait for result
        Awaitility.await().atMost(Duration.TEN_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return statefulCallback.hasResponse();
            }
        });

        // verify the upload succeeded, and actually went through the immediate channels.
        Assert.assertNotNull(statefulCallback.lastSuccess);
        Assert.assertEquals(16, statefulCallback.lastSuccess.get("width"));
        MediaManager.get().unregisterCallback(statefulCallback);
    }

    // TODO: Fix transcoding tests in travis
    @Ignore
    @Test
    public void testTranscode() throws IOException, PreprocessException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File file = assetToFile("videos/test_video.mp4");
        Uri videoUri = Uri.fromFile(file);

        Parameters parameters = new Parameters();
        parameters.setRequestId("test_request_id");
        parameters.setFrameRate(30);
        parameters.setWidth(1280);
        parameters.setHeight(720);
        parameters.setKeyFramesInterval(3);
        parameters.setTargetAudioBitrateKbps(128);
        parameters.setTargetVideoBitrateKbps((int) (3.3 * 1024 * 1024));

        Uri outputVideoUri = new Transcode(parameters).execute(context, videoUri);
        File targetVideoFile = new File(outputVideoUri.getPath());

        Assert.assertTrue(targetVideoFile.length() > 0);
    }

    // TODO: Fix transcoding tests in travis
    @Ignore
    @Test
    public void testVideoChain() throws IOException, PreprocessException, PayloadNotFoundException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File file = assetToFile("videos/test_video.mp4");
        FilePayload payload = new FilePayload(file.getAbsolutePath());

        Parameters parameters = new Parameters();
        parameters.setRequestId("test_request_id");
        parameters.setFrameRate(30);
        parameters.setWidth(1280);
        parameters.setHeight(720);
        parameters.setKeyFramesInterval(3);
        parameters.setTargetAudioBitrateKbps(128);
        parameters.setTargetVideoBitrateKbps((int) (3.3 * 1024 * 1024));

        VideoPreprocessChain chain = VideoPreprocessChain.videoTranscodingChain(parameters);
        String outputVideoPath = chain.execute(context, payload);
        File targetVideoFile = new File(outputVideoPath);

        Assert.assertTrue(targetVideoFile.length() > 0);
    }

    private int getError(Context context, DimensionsValidator validator, Bitmap bitmap) {
        try {
            validator.execute(context, bitmap);
        } catch (ValidationException e) {
            return 1;
        }

        return 0;
    }
}
