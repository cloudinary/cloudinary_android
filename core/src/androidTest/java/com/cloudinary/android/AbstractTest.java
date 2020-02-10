package com.cloudinary.android;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.policy.TimeWindow;
import com.cloudinary.android.policy.UploadPolicy;
import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;

import org.junit.BeforeClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

public class AbstractTest {
    public static final String TEST_IMAGE = "images/old_logo.png";
    public static final String TEST_PRESET = "cloudinary_java_test";
    private static boolean initialized = false;

    @BeforeClass
    public synchronized static void initLibrary() {
        if (!initialized) {
            MediaManager.init(InstrumentationRegistry.getInstrumentation().getTargetContext(), new SignatureProvider() {
                @Override
                public Signature provideSignature(Map options) {
                    return null;
                }

                @Override
                public String getName() {
                    return null;
                }
            });
            MediaManager.get().getCloudinary().config.apiSecret = null;
            initialized = true;
        }
    }

    protected static InputStream getAssetStream(String filename) throws IOException {
        return InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(filename);
    }

    protected static long getAssetFileSize(String filename) {
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = InstrumentationRegistry.getInstrumentation().getContext().getAssets().openFd(filename);
            return assetFileDescriptor.getLength();
        } catch (IOException e) {
            return -1;
        } finally {
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected static File assetToFile(String testImage) throws IOException {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        File file = new File(context.getCacheDir() + "/tempFile_" + System.currentTimeMillis());

        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        InputStream is = getAssetStream(testImage);

        byte[] buffer = new byte[16276];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }

        fos.flush();
        is.close();

        return file;
    }

    /**
     * Centralize processor creation in case we want to test different implementations in the future.
     */
    protected RequestProcessor provideRequestProcessor(DefaultCallbackDispatcher callbackDispatcher) {
        return new DefaultRequestProcessor(callbackDispatcher);
    }

    /**
     * Centralize params creation in case we want to test different implementations in the future.
     */
    protected RequestParams provideRequestParams() {
        TestParams testParams = new TestParams();
        testParams.putString("requestId", UUID.randomUUID().toString());
        return testParams;
    }

    /**
     * Centralize callback dispatcher creation in case we want to test different implementations in the future.
     */
    protected DefaultCallbackDispatcher provideCallbackDispatcher() {
        return new DefaultCallbackDispatcher(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @NonNull
    protected FilePayload buildPayload() throws IOException {
        return new FilePayload(assetToFile(TEST_IMAGE).getAbsolutePath());
    }

    protected UploadRequest<FilePayload> buildUploadRequest(FilePayload payload, int maxExecutionDisplay) {
        return MediaManager.get().upload(payload)
                .unsigned(TEST_PRESET)
                .constrain(new TimeWindow.Builder().minLatencyMillis(20).maxExecutionDelayMillis(maxExecutionDisplay).build())
                .policy(new UploadPolicy.Builder()
                        .networkPolicy(UploadPolicy.NetworkType.UNMETERED)
                        .requiresCharging(true)
                        .requiresIdle(false)
                        .backoffCriteria(100, UploadPolicy.BackoffPolicy.LINEAR)
                        .maxRetries(9)
                        .build());
    }

    /**
     * Bundle based implementation for RequestParams, for testing purposes.
     */
    protected static final class TestParams implements RequestParams {
        private final Bundle values = new Bundle();

        @Override
        public void putString(String key, String value) {
            values.putString(key, value);
        }

        @Override
        public void putInt(String key, int value) {
            values.putInt(key, value);
        }

        @Override
        public void putLong(String key, long value) {
            values.putLong(key, value);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            values.putBoolean(key, value);
        }

        @Override
        public String getString(String key, String defaultValue) {
            return values.getString(key, defaultValue);
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return values.getInt(key, defaultValue);
        }

        @Override
        public long getLong(String key, long defaultValue) {
            return values.getLong(key, defaultValue);
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            return values.getBoolean(key, defaultValue);
        }
    }
}
