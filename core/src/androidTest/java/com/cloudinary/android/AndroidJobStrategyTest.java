package com.cloudinary.android;


import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.work.WorkRequest;

import com.cloudinary.android.payload.FilePayload;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4ClassRunner.class)
public class AndroidJobStrategyTest extends AbstractTest {

    @Test
    public void testAdapter() throws InterruptedException, IOException {
        FilePayload payload = buildPayload();

        int tenMinutes = 10 * 60 * 1000;
        UploadRequest<FilePayload> request = buildUploadRequest(payload, tenMinutes);

        WorkRequest adapted = AndroidJobStrategy.adapt(request);

        Assert.assertNotNull(adapted.getId());
    }
}
