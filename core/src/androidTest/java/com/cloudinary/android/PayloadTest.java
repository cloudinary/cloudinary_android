package com.cloudinary.android;

import android.net.Uri;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.android.payload.ByteArrayPayload;
import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.payload.LocalUriPayload;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.payload.PayloadFactory;
import com.cloudinary.android.payload.PayloadNotFoundException;
import com.cloudinary.android.payload.ResourcePayload;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4ClassRunner.class)
public class PayloadTest extends AbstractTest {
    private static final int UNKNOWN_SIZE = 0;
    static File assetFile;

    @BeforeClass
    public static void setup() throws IOException {
        assetFile = assetToFile(TEST_IMAGE);
    }

    @Test
    public void testFilePayload() throws PayloadNotFoundException {
        FilePayload filePayload = new FilePayload(assetFile.getAbsolutePath());
        verifyLengthAndRecreation(filePayload, assetFile.length());
    }

    @Test
    public void testUriPayload() throws PayloadNotFoundException {
        LocalUriPayload filePayload = new LocalUriPayload(Uri.fromFile(assetFile));
        // note: can't test uri size unless it's an actual media on the device
        verifyLengthAndRecreation(filePayload, UNKNOWN_SIZE);
    }

    @Test
    public void testBytesPayload() throws PayloadNotFoundException, IOException {
        FileInputStream fileInputStream = null;
        try {
            byte[] buffer = new byte[(int) assetFile.length()];
            fileInputStream = new FileInputStream(assetFile);
            fileInputStream.read(buffer);
            fileInputStream.close();
            ByteArrayPayload byteArrayPayload = new ByteArrayPayload(buffer);
            verifyLengthAndRecreation(byteArrayPayload, assetFile.length());
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    @Test
    public void testResourcePayload() throws PayloadNotFoundException {
        ResourcePayload payload = new ResourcePayload(com.cloudinary.android.core.test.R.raw.old_logo);
        verifyLengthAndRecreation(payload, 3381);
    }

    private void verifyLengthAndRecreation(Payload payload, long expectedLength) throws PayloadNotFoundException {
        assertEquals(expectedLength, payload.getLength(InstrumentationRegistry.getInstrumentation().getContext()));

        String asUri = payload.toUri();
        Payload newPayload = PayloadFactory.fromUri(asUri);
        assertEquals(newPayload, payload);
    }
}
