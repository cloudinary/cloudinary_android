/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.cloudinary.android.sample.backend;

import com.cloudinary.Cloudinary;
import com.cloudinary.Util;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import java.util.Map;

@Api(
        name = "myApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.sample.android.cloudinary.com",
                ownerName = "backend.sample.android.cloudinary.com",
                packagePath = ""
        )
)

public class MyEndpoint {

    @ApiMethod(name = "sign")
    public SignResult sign(Map<String, Object> options) {
        long timestamp = System.currentTimeMillis() / 1000L;
        String timestampStr = Long.toString(timestamp);
        Map<String, Object> toSign = Util.buildUploadParams(options);
        toSign.put("timestamp", timestampStr);

        // The secret is retrieved from an environment variable CLOUDINARY_URL (see console at cloudinary.com/console to get the cloudinary url)
        // *** IMPORTANT *** Do not type your api secret here directly to avoid checking it into source control.
        Cloudinary cloudinary = new Cloudinary();
        String signature = cloudinary.apiSignRequest(toSign, cloudinary.config.apiSecret);
        return new SignResult(signature, timestamp);
    }
}
