package com.cloudinary.android;


import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.internal.util.ReflectionUtil;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.impl.model.WorkSpec;

import com.cloudinary.android.payload.FilePayload;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

@RunWith(AndroidJUnit4ClassRunner.class)
public class AndroidJobStrategyTest extends AbstractTest {

    @Test
    public void testAdapter() throws InterruptedException, IOException, NoSuchFieldException, IllegalAccessException {
        FilePayload payload = buildPayload();

        int tenMinutes = 10 * 60 * 1000;
        UploadRequest<FilePayload> request = buildUploadRequest(payload, tenMinutes);

        WorkRequest adapted = AndroidJobStrategy.adapt(request);
        Class obj = adapted.getClass().getSuperclass();
        Field field = obj.getDeclaredField("mWorkSpec");
        field.setAccessible(true);
        Assert.assertEquals(true, adapted.getWorkSpec().constraints.requiresCharging());
        Assert.assertEquals(false, adapted.getWorkSpec().constraints.requiresDeviceIdle());
        Assert.assertEquals(10000, adapted.getWorkSpec().backoffDelayDuration);
        Assert.assertEquals(BackoffPolicy.LINEAR, adapted.getWorkSpec().backoffPolicy);

    }
}
