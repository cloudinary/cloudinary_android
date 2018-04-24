package com.cloudinary.android;

import android.support.test.runner.AndroidJUnit4;

import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.policy.TimeWindow;
import com.cloudinary.android.policy.UploadPolicy;
import com.evernote.android.job.JobRequest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AndroidJobStrategyTest extends AbstractTest {

    @Test
    public void testAdapter() throws InterruptedException, IOException {
        FilePayload payload = new FilePayload(assetToFile(TEST_IMAGE).getAbsolutePath());

        int tenMinutes = 10 * 60 * 1000;
        UploadRequest<FilePayload> request =
                new UploadRequest<>(new UploadContext<>(payload, null), null)
                        .constrain(new TimeWindow.Builder().minLatencyMillis(20).maxExecutionDelayMillis(tenMinutes).build())
                        .policy(new UploadPolicy.Builder()
                                .networkPolicy(UploadPolicy.NetworkType.UNMETERED)
                                .requiresCharging(true)
                                .requiresIdle(false)
                                .backoffCriteria(100, UploadPolicy.BackoffPolicy.LINEAR)
                                .maxRetries(9)
                                .build());

        JobRequest adapted = AndroidJobStrategy.adapt(request);

        assertEquals(20, adapted.getStartMs());
        assertEquals(tenMinutes, adapted.getEndMs());
        assertEquals(true, adapted.requiresCharging());
        assertEquals(false, adapted.requiresDeviceIdle());
        assertEquals(100, adapted.getBackoffMs());
        assertEquals(JobRequest.BackoffPolicy.LINEAR, adapted.getBackoffPolicy());
        assertEquals(9, adapted.getExtras().get("maxErrorRetries"));

        UploadRequest<FilePayload> exactRequest =
                new UploadRequest<>(new UploadContext<>(payload, null), null)
                        .constrain(TimeWindow.immediate());

        JobRequest adaptedExact = AndroidJobStrategy.adapt(exactRequest);
        assertTrue(adaptedExact.isExact());
        assertEquals(adaptedExact.getStartMs(), 1);
        assertEquals(adaptedExact.getEndMs(), 1);
    }
}
