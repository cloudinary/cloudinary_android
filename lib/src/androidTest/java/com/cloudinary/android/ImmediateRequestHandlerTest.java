package com.cloudinary.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.cloudinary.android.payload.FilePayload;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ImmediateRequestHandlerTest extends AbstractTest {
    private StatefulCallback statefulCallback = new StatefulCallback();

    @Test
    public void testStuff() throws IOException {
        Context appContext = InstrumentationRegistry.getTargetContext();
        DefaultCallbackDispatcher callbackDispatcher = new DefaultCallbackDispatcher(appContext);
        callbackDispatcher.registerCallback(statefulCallback);

        RequestProcessor processor = provideRequestProcessor(callbackDispatcher);
        ImmediateRequestsRunner requestsRunner = new DefaultImmediateRequestsRunner(processor);
        RequestDispatcher dispatcher = new DefaultRequestDispatcher(new NOPStrategy(), requestsRunner);
        UploadRequest<FilePayload> uploadRequest = buildUploadRequest(buildPayload(), 1000);
        uploadRequest.serializeOptions();
        dispatcher.startNow(appContext, uploadRequest);

        // wait for result
        Awaitility.await().atMost(Duration.TEN_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return statefulCallback.hasResponse();
            }
        });

        assertNotNull(statefulCallback.lastSuccess);
    }
}
