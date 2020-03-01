package com.cloudinary.android;

import android.content.Context;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.UploadResult;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4ClassRunner.class)
public class CallbackDispatcherTest extends AbstractTest {

    private static final int DISPATCH_SLEEP_MILLIS = 100;

    @Test
    public void testCallbacks() throws InterruptedException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        CallbackDispatcher dispatcher = new DefaultCallbackDispatcher(appContext);
        CallbackCounter callbackCounter = new CallbackCounter();
        CallbackCounter oneRequestCallbackCounter = new CallbackCounter();
        dispatcher.registerCallback(callbackCounter);
        dispatcher.registerCallback("a", oneRequestCallbackCounter);

        dispatcher.dispatchStart("a");
        dispatcher.dispatchProgress("a", 10, 100);
        dispatcher.dispatchSuccess(appContext, "a", Collections.singletonMap("test1", "result1"));
        dispatcher.dispatchError(appContext, "a", new ErrorInfo(ErrorInfo.RESOURCE_DOES_NOT_EXIST, null));
        dispatcher.dispatchReschedule(appContext, "a", new ErrorInfo(ErrorInfo.FILE_DOES_NOT_EXIST, null));

        // callbacks are posted to dedicated thread, give it time to propagate
        Thread.sleep(DISPATCH_SLEEP_MILLIS);

        assertEquals(1, oneRequestCallbackCounter.start);
        assertEquals(1, oneRequestCallbackCounter.progress);
        assertEquals(1, oneRequestCallbackCounter.success);
        assertEquals(1, oneRequestCallbackCounter.error);
        assertEquals(1, oneRequestCallbackCounter.reschedule);

        assertEquals(1, callbackCounter.start);
        assertEquals(1, callbackCounter.progress);
        assertEquals(1, callbackCounter.success);
        assertEquals(1, callbackCounter.error);
        assertEquals(1, callbackCounter.reschedule);

        dispatcher.dispatchProgress("b", 10, 100);
        dispatcher.dispatchSuccess(appContext, "b", Collections.singletonMap("test2", "result2"));
        dispatcher.dispatchError(appContext, "b", new ErrorInfo(ErrorInfo.RESOURCE_DOES_NOT_EXIST, null));

        Thread.sleep(DISPATCH_SLEEP_MILLIS);
        assertEquals(2, callbackCounter.progress);
        assertEquals(2, callbackCounter.success);
        assertEquals(2, callbackCounter.error);

        dispatcher.unregisterCallback(callbackCounter);

        dispatcher.dispatchProgress("c", 10, 100);
        dispatcher.dispatchSuccess(appContext, "c", Collections.singletonMap("test3", "result3"));
        dispatcher.dispatchError(appContext, "c", new ErrorInfo(ErrorInfo.RESOURCE_DOES_NOT_EXIST, null));

        Thread.sleep(DISPATCH_SLEEP_MILLIS);

        assertEquals(1, oneRequestCallbackCounter.start);
        assertEquals(1, oneRequestCallbackCounter.progress);
        assertEquals(1, oneRequestCallbackCounter.success);
        assertEquals(1, oneRequestCallbackCounter.error);
        assertEquals(1, oneRequestCallbackCounter.reschedule);

        assertEquals(2, callbackCounter.progress);
        assertEquals(2, callbackCounter.success);
        assertEquals(2, callbackCounter.error);
    }

    @Test
    public void testQueuedResults() throws InterruptedException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        CallbackDispatcher dispatcher = new DefaultCallbackDispatcher(appContext);

        dispatcher.dispatchSuccess(appContext, "a", Collections.singletonMap("test1", "result1"));
        dispatcher.dispatchSuccess(appContext, "b", Collections.singletonMap("test2", "result2"));
        dispatcher.dispatchSuccess(appContext, "c", Collections.singletonMap("test3", "result3"));

        Thread.sleep(DISPATCH_SLEEP_MILLIS);

        UploadResult res1 = dispatcher.popPendingResult("a");
        UploadResult res2 = dispatcher.popPendingResult("b");
        UploadResult res3 = dispatcher.popPendingResult("c");

        assertNotNull(res1);
        assertNotNull(res2);
        assertNotNull(res3);

        Object test1 = res1.getSuccessResultData().get("test1");
        Object test2 = res2.getSuccessResultData().get("test2");
        Object test3 = res3.getSuccessResultData().get("test3");

        assertEquals(test1, "result1");
        assertEquals(test2, "result2");
        assertEquals(test3, "result3");

        res1 = dispatcher.popPendingResult("a");
        res2 = dispatcher.popPendingResult("b");
        res3 = dispatcher.popPendingResult("c");

        assertNull(res1);
        assertNull(res2);
        assertNull(res3);

        dispatcher.dispatchError(appContext, "a", new ErrorInfo(ErrorInfo.RESOURCE_DOES_NOT_EXIST, null));

        Thread.sleep(DISPATCH_SLEEP_MILLIS);

        res1 = dispatcher.popPendingResult("a");
        assertNotNull(res1);
        assertEquals(ErrorInfo.RESOURCE_DOES_NOT_EXIST, res1.getError().getCode());
    }

    private final static class CallbackCounter implements UploadCallback {

        private int start;
        private int success;
        private int progress;
        private int reschedule;
        private int error;

        @Override
        public void onStart(String requestId) {
            this.start++;
        }

        @Override
        public void onProgress(String requestId, long bytes, long totalBytes) {
            this.progress++;
        }

        @Override
        public void onSuccess(String requestId, Map resultData) {
            this.success++;
        }

        @Override
        public void onError(String requestId, ErrorInfo error) {
            this.error++;
        }

        @Override
        public void onReschedule(String requestId, ErrorInfo error) {
            this.reschedule++;
        }
    }
}
