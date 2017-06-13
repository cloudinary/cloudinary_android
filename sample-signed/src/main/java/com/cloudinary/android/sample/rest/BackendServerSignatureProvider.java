package com.cloudinary.android.sample.rest;

import android.support.v4.util.Pair;

import com.cloudinary.android.SignatureProvider;
import com.cloudinary.android.sample.rest.model.JsonMap;
import com.cloudinary.android.sample.rest.model.SignResult;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.Map;

public class BackendServerSignatureProvider implements SignatureProvider {
    private static MyApi myApiService = null;

    private synchronized SignResult signUpload(JsonMap options) {
        if (myApiService == null) {  // Only do this once
            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            // end options for devappserver

            myApiService = builder.build();
        }

        try {
            return myApiService.sign(options).execute();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Pair<String, Long> provideSignature(Map options) {
        JsonMap map = new JsonMap();
        for (Object key : options.keySet()) {
            map.put((String) key, options.get(key));
        }

        SignResult result = signUpload(map);

        if (result != null) {
            return new Pair<>(result.getSignature(), result.getTimestamp());
        }

        return null;
    }
}
