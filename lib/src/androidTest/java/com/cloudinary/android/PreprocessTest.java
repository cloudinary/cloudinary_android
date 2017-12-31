package com.cloudinary.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;

import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.payload.PayloadNotFoundException;
import com.cloudinary.android.preprocess.DimensionsValidator;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.preprocess.Limit;
import com.cloudinary.android.preprocess.PreprocessException;
import com.cloudinary.android.preprocess.ValidationException;

import junit.framework.Assert;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;


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

        request.preprocess(ImagePreprocessChain.limitDimensionsChain(15, 15)).dispatch(InstrumentationRegistry.getTargetContext());
        Awaitility.await().atMost(Duration.FIVE_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (requests[0] == null) {
                    return false;
                }

                File file = (File) requests[0].getPayload().prepare(InstrumentationRegistry.getTargetContext());
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
        String filePath = chain.execute(InstrumentationRegistry.getTargetContext(), payload);
        FileInputStream fileInputStream = InstrumentationRegistry.getTargetContext().openFileInput(filePath);
        Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
        Assert.assertEquals(40, bitmap.getWidth());
        Assert.assertEquals(8, bitmap.getHeight());
    }

    @Test
    public void testLimit() {
        final Context context = InstrumentationRegistry.getTargetContext();
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
    public void testDimensionsValidator() {
        int errors = 0;
        DimensionsValidator validator = new DimensionsValidator(100, 100, 1000, 1000);
        final Context context = InstrumentationRegistry.getTargetContext();

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

    private int getError(Context context, DimensionsValidator validator, Bitmap bitmap) {
        try {
            validator.execute(context, bitmap);
        } catch (ValidationException e) {
            return 1;
        }

        return 0;
    }
}
