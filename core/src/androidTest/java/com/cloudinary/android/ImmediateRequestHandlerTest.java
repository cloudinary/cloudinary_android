package com.cloudinary.android;

import android.content.Context;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.android.payload.FilePayload;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4ClassRunner.class)
public class ImmediateRequestHandlerTest extends AbstractTest {

    @Test
    public void testImmediateRequest() throws IOException {
        final StatefulCallback statefulCallback = new StatefulCallback();

        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DefaultCallbackDispatcher callbackDispatcher = new DefaultCallbackDispatcher(appContext);
        callbackDispatcher.registerCallback(statefulCallback);

        RequestProcessor processor = provideRequestProcessor(callbackDispatcher);
        ImmediateRunnerForTests requestsRunner = new ImmediateRunnerForTests(processor);
        RequestDispatcher dispatcher = new DefaultRequestDispatcher(new NOPStrategy(), requestsRunner);
        UploadRequest<FilePayload> uploadRequest = buildUploadRequest(buildPayload(), 1000);
        uploadRequest.serializeOptions();

        assertEquals(0, requestsRunner.taskRan);
        dispatcher.startNow(appContext, uploadRequest);

        // wait for result
        Awaitility.await().atMost(Duration.TEN_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return statefulCallback.hasResponse();
            }
        });

        // verify the upload succeeded, and actually went through the immediate channels.
        assertNotNull(statefulCallback.lastSuccess);
        assertEquals(1, requestsRunner.taskRan);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullContext() {
        MediaManager.get().upload("path").startNow(null);
    }

    private class ImmediateRunnerForTests extends DefaultImmediateRequestsRunner {
        int taskRan = 0;

        ImmediateRunnerForTests(RequestProcessor requestProcessor) {
            super(requestProcessor);
        }

        @Override
        public synchronized void runRequest(Context context, UploadRequest uploadRequest) {
            super.runRequest(context, uploadRequest);
            taskRan++;
        }
    }
}
