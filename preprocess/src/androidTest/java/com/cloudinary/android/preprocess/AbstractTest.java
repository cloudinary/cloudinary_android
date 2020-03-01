package com.cloudinary.android.preprocess;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.UploadRequest;
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

    protected static InputStream getAssetStream(String filename) throws IOException {
        return InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(filename);
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

}
