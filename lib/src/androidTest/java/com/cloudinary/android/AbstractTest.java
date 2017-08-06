package com.cloudinary.android;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.support.test.InstrumentationRegistry;

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
            MediaManager.init(InstrumentationRegistry.getTargetContext(), new SignatureProvider() {
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
        return InstrumentationRegistry.getContext().getAssets().open(filename);
    }

    protected static long getAssetFileSize(String filename) {
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = InstrumentationRegistry.getContext().getAssets().openFd(filename);
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
        Context context = InstrumentationRegistry.getContext();
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
