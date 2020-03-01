package com.cloudinary.android;

import android.net.Uri;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.payload.ByteArrayPayload;
import com.cloudinary.android.payload.FilePayload;
import com.cloudinary.android.payload.LocalUriPayload;
import com.cloudinary.android.payload.ResourcePayload;
import com.cloudinary.strategies.AbstractUploaderStrategy;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4ClassRunner.class)
public class RequestProcessorTest extends AbstractTest {

    @Test
    public void testValidUploadWithParams() throws IOException {
        RequestParams params = provideRequestParams();
        params.putString("uri", buildPayload().toUri());
        HashMap<String, Object> options = new HashMap<>();
        // verify that the parameter reaches all the way to the uploader inside:
        final String id = UUID.randomUUID().toString();
        options.put("public_id", id);
        options.put("unsigned", true);
        options.put("upload_preset", TEST_PRESET);
        params.putString("options", UploadRequest.encodeOptions(options));
        params.putInt("maxErrorRetries", 0);
        DefaultCallbackDispatcher callbackDispatcher = provideCallbackDispatcher();
        RequestProcessor processor = provideRequestProcessor(callbackDispatcher);
        final StatefulCallback statefulCallback = new StatefulCallback();
        callbackDispatcher.registerCallback(statefulCallback);

        processor.processRequest(InstrumentationRegistry.getInstrumentation().getTargetContext(), params);

        // wait for result
        Awaitility.await().atMost(Duration.TEN_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return statefulCallback.hasResponse();
            }
        });

        assertNotNull(statefulCallback.lastSuccess);
        assertEquals(id, statefulCallback.lastSuccess.get("public_id"));
    }

    @Test
    public void testTimeout() throws IOException {
        RequestParams params = provideRequestParams();
        params.putString("uri", buildPayload().toUri());
        HashMap<String, Object> options = new HashMap<>();
        // verify that the parameter reaches all the way to the uploader inside:
        final String id = UUID.randomUUID().toString();
        options.put("public_id", id);
        options.put("read_timeout", 1);
        options.put("unsigned", true);
        options.put("upload_preset", TEST_PRESET);
        params.putString("options", UploadRequest.encodeOptions(options));
        params.putInt("maxErrorRetries", 3);
        DefaultCallbackDispatcher callbackDispatcher = provideCallbackDispatcher();
        RequestProcessor processor = provideRequestProcessor(callbackDispatcher);
        final StatefulCallback statefulCallback = new StatefulCallback();
        callbackDispatcher.registerCallback(statefulCallback);

        processor.processRequest(InstrumentationRegistry.getInstrumentation().getTargetContext(), params);

        // wait for result
        Awaitility.await().atMost(Duration.TEN_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return statefulCallback.hasResponse();
            }
        });

        assertNotNull(statefulCallback.lastReschedule);
        assertEquals(ErrorInfo.NETWORK_ERROR, statefulCallback.lastReschedule.getCode());
    }

    @Test
    public void testInvalidOptions() throws IOException {
        RequestParams params = provideRequestParams();
        params.putString("options", "bad options string");
        params.putString("uri", buildPayload().toUri());

        verifyError(params, ErrorInfo.OPTIONS_FAILURE);
    }

    @Test
    public void testNoPayload() throws IOException {
        RequestParams params = provideRequestParams();

        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        verifyError(params, ErrorInfo.PAYLOAD_EMPTY);
    }

    @Test
    public void testInvalidPayload() throws IOException {
        RequestParams params = provideRequestParams();

        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        params.putString("uri", "bad uri!");

        verifyError(params, ErrorInfo.PAYLOAD_LOAD_FAILURE);
    }

    @Test
    public void testInvalidUriPayload() throws IOException {
        RequestParams params = provideRequestParams();

        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        params.putString("uri", new LocalUriPayload(Uri.parse("bad uri!")).toUri());

        verifyError(params, ErrorInfo.URI_DOES_NOT_EXIST);
    }

    @Test
    public void testInvalidFilePayload() throws IOException {
        RequestParams params = provideRequestParams();


        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        params.putString("uri", new FilePayload("bad path!").toUri());

        verifyError(params, ErrorInfo.FILE_DOES_NOT_EXIST);
    }

    @Test
    public void testInvalidByteArrayPayload() throws IOException {
        RequestParams params = provideRequestParams();


        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        params.putString("uri", new ByteArrayPayload(new byte[]{}).toUri());

        verifyError(params, ErrorInfo.BYTE_ARRAY_PAYLOAD_EMPTY);
    }

    @Test
    public void testInvalidResourcePayload() throws IOException {
        RequestParams params = provideRequestParams();


        params.putString("options", UploadRequest.encodeOptions(new HashMap<String, Object>()));
        params.putString("uri", new ResourcePayload(-10).toUri());

        verifyError(params, ErrorInfo.RESOURCE_DOES_NOT_EXIST);
    }

    @Test
    public void testSignatureFailure() throws IOException {
        RequestParams params = provideRequestParams();
        HashMap<String, Object> options = new HashMap<>();

        params.putString("options", UploadRequest.encodeOptions(options));
        params.putString("uri", buildPayload().toUri());

        verifyError(params, ErrorInfo.SIGNATURE_FAILURE);
    }

    /**
     * Utility method to handle error checking. Throws exception if the expected error does not occur.
     *
     * @param params            Params setup to generate a specific error
     * @param expectedErrorCode The expected error code that should be generated by the given params.
     */
    private void verifyError(RequestParams params, final int expectedErrorCode) {
        DefaultCallbackDispatcher callbackDispatcher = provideCallbackDispatcher();
        RequestProcessor processor = provideRequestProcessor(callbackDispatcher);
        final StatefulCallback statefulCallback = new StatefulCallback();
        callbackDispatcher.registerCallback(statefulCallback);
        processor.processRequest(InstrumentationRegistry.getInstrumentation().getTargetContext(), params);

        Awaitility.await().atMost(Duration.TWO_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return statefulCallback.lastErrorObject != null && statefulCallback.lastErrorObject.getCode() == expectedErrorCode;
            }
        });
    }

    @Test
    public void testMaxRetries() throws IOException, NoSuchFieldException, IllegalAccessException {
        RequestParams params = provideRequestParams();
        params.putString("uri", buildPayload().toUri());
        HashMap<String, Object> options = new HashMap<>();
        // verify that the parameter reaches all the way to the uploader inside:
        final String id = UUID.randomUUID().toString();
        options.put("public_id", id);
        options.put("unsigned", true);
        options.put("upload_preset", TEST_PRESET);
        params.putString("options", UploadRequest.encodeOptions(options));
        params.putInt("errorCount", 5);
        params.putInt("maxErrorRetries", 6);

        DefaultCallbackDispatcher callbackDispatcher = provideCallbackDispatcher();
        RequestProcessor processor = provideRequestProcessor(callbackDispatcher);
        final StatefulCallback statefulCallback = new StatefulCallback();
        callbackDispatcher.registerCallback(statefulCallback);

        AbstractUploaderStrategy prev = TestUtils.replaceWithTimeoutStrategy(MediaManager.get().getCloudinary());

        // run once, expecting the request to fail but 'reschedule'
        processor.processRequest(InstrumentationRegistry.getInstrumentation().getTargetContext(), params);

        // wait for result
        Awaitility.await().atMost(Duration.TEN_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return statefulCallback.lastReschedule != null;
            }
        });

        assertNotNull(statefulCallback.lastReschedule);
        assertNull(statefulCallback.lastSuccess);
        assertEquals(6, params.getInt("errorCount", 0));

        StatefulCallback anotherStateful = new StatefulCallback();
        callbackDispatcher.registerCallback(anotherStateful);

        // run a second time, expecting a failure (too many errors):
        processor.processRequest(InstrumentationRegistry.getInstrumentation().getTargetContext(), params);

        // wait for result
        Awaitility.await().atMost(Duration.TEN_SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return statefulCallback.hasResponse();
            }
        });

        assertNotNull(statefulCallback.lastErrorObject);
        assertEquals(ErrorInfo.TOO_MANY_ERRORS, statefulCallback.lastErrorObject.getCode());


        // put it back:
        TestUtils.replaceStrategyForIntsance(MediaManager.get().getCloudinary(), prev);
    }

}
