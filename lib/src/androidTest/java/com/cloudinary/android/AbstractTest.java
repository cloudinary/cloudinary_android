package com.cloudinary.android;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.support.test.InstrumentationRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AbstractTest {
    public static final String TEST_IMAGE = "images/old_logo.png";
    public static final String TEST_PRESET = "cloudinary_java_test";

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
