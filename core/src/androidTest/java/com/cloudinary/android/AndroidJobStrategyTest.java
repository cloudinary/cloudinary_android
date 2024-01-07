package com.cloudinary.android;


import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.internal.util.ReflectionUtil;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.impl.model.WorkSpec;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.utils.ObjectUtils;

import org.cloudinary.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

@RunWith(AndroidJUnit4ClassRunner.class)
public class AndroidJobStrategyTest extends AbstractTest {

    int success = 0;
    int errors = 0;
    private File payloadFile;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if(payloadFile != null) {
            //noinspection ResultOfMethodCallIgnored
            payloadFile.delete();
        }
    }

    @Test
    public void testAdapter() throws InterruptedException, IOException, NoSuchFieldException, IllegalAccessException {
        FilePayload payload = buildPayload();

        int tenMinutes = 10 * 60 * 1000;
        UploadRequest<FilePayload> request = buildUploadRequest(payload, tenMinutes);

        payloadFile = File.createTempFile("payload", request.getRequestId());

        WorkRequest adapted = AndroidJobStrategy.adapt(request, payloadFile);
        Class obj = adapted.getClass().getSuperclass();
        Field field = obj.getDeclaredField("mWorkSpec");
        field.setAccessible(true);
        Assert.assertEquals(true, adapted.getWorkSpec().constraints.requiresCharging());
        Assert.assertEquals(false, adapted.getWorkSpec().constraints.requiresDeviceIdle());
        Assert.assertEquals(10000, adapted.getWorkSpec().backoffDelayDuration);
        Assert.assertEquals(BackoffPolicy.LINEAR, adapted.getWorkSpec().backoffPolicy);

    }
    @Test
    public void testCancelRequest() throws InterruptedException, IOException, NoSuchFieldException, IllegalAccessException {
        FilePayload payload = buildPayload();
        String requestId = MediaManager.get().upload(payload)
                .unsigned(TEST_PRESET).callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        success++;
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        errors++;
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {

                    }
                })
                .dispatch();
        Thread.sleep(1000);
        MediaManager.get().cancelRequest(requestId);
        Thread.sleep(7000);
        Assert.assertTrue(success == 0);
    }
}
