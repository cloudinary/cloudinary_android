package com.cloudinary.android;


import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.cloudinary.android.payload.FilePayload;
import com.evernote.android.job.JobRequest;

import junit.framework.Assert;

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

        JobRequest adapted = AndroidJobStrategy.adapt(request);

        Assert.assertEquals(20, adapted.getStartMs());
        Assert.assertEquals(tenMinutes, adapted.getEndMs());
        Assert.assertEquals(true, adapted.requiresCharging());
        Assert.assertEquals(false, adapted.requiresDeviceIdle());
        Assert.assertEquals(100, adapted.getBackoffMs());
        Assert.assertEquals(JobRequest.BackoffPolicy.LINEAR, adapted.getBackoffPolicy());
        Assert.assertEquals(9, adapted.getExtras().get("maxErrorRetries"));
    }
}
