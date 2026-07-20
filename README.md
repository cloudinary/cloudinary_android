# Cloudinary Android SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.cloudinary/cloudinary-android.svg)](https://central.sonatype.com/artifact/com.cloudinary/cloudinary-android)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE)
[![Test](https://github.com/cloudinary/cloudinary_android/actions/workflows/android-build.yml/badge.svg)](https://github.com/cloudinary/cloudinary_android/actions/workflows/android-build.yml)

The `com.cloudinary:cloudinary-android` package is the native Android client SDK for Cloudinary (Java and Kotlin). Use it inside an installed app to upload media from the device through a background, retrying queue and to build transformation and delivery URLs in-app. The current release (3.1.2) targets Android minSdk 21 and compileSdk 34, and depends on `cloudinary-core` (Java) under the hood.

## Installation

Add the dependency to your app module `build.gradle`, with `mavenCentral()` in your repositories:

```gradle
implementation 'com.cloudinary:cloudinary-android:3.1.2'
```

With Maven, add it to `pom.xml` instead:

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-android</artifactId>
    <version>3.1.2</version>
    <type>aar</type>
</dependency>
```

## Configuration

This SDK ships inside an installed app, so it never holds the API secret. It needs only the cloud name. Uploads run unsigned via upload presets, or signed by a server you control through a `SignatureProvider`. Keep the API key and secret out of the app and out of version control.

Initialize `MediaManager` once, before any other SDK call — in your `Application.onCreate()`, not per-Activity. Set the cloud name programmatically:

```java
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApp extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "my_cloud_name");
        MediaManager.init(this, config);
    }
}
```

Or set a secret-free `CLOUDINARY_URL` as meta-data in `AndroidManifest.xml` and call the no-config overload — the value carries only the cloud name:

```xml
<application android:name=".MyApp">
    <meta-data
        android:name="CLOUDINARY_URL"
        android:value="cloudinary://@my_cloud_name" />
</application>
```

```java
import com.cloudinary.android.MediaManager;

MediaManager.init(this); // reads CLOUDINARY_URL from the manifest meta-data
```

## Quick examples

### Upload from the device

`MediaManager.get().upload(...)` accepts a file-path `String`, a `Uri`, a `byte[]`, or a raw-resource `int` (there is no `File` overload — pass `file.getAbsolutePath()` for a file). The upload is dispatched to a background queue and retried on recoverable errors. `dispatch()` returns a `requestId` immediately; results arrive through the `UploadCallback`, whose `onSuccess` map holds `public_id` and `secure_url`. Requires `MediaManager` initialized (see Configuration).

```java
import android.net.Uri;
import android.util.Log;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import java.util.Map;

String requestId = MediaManager.get().upload(imageUri)
    .unsigned("my_preset")            // an unsigned upload preset defined in your product environment
    .option("public_id", "sample_id")
    .callback(new UploadCallback() {
        @Override public void onStart(String requestId) { }
        @Override public void onProgress(String requestId, long bytes, long totalBytes) { }
        @Override public void onSuccess(String requestId, Map resultData) {
            Log.d("upload", resultData.get("public_id") + " " + resultData.get("secure_url"));
        }
        @Override public void onError(String requestId, ErrorInfo error) {
            Log.e("upload", error.getDescription() + " (" + error.getCode() + ")");
        }
        @Override public void onReschedule(String requestId, ErrorInfo error) { }
    })
    .dispatch();
```

### Build and optimize a delivery URL

`MediaManager.get().url()` is synchronous and returns a string — no network call. This one scales `sample.jpg` to 400 px wide and lets Cloudinary pick the format and quality for the requesting device (`f_auto`, `q_auto`). Requires `MediaManager` initialized (see Configuration).

```java
import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;

String url = MediaManager.get().url()
    .transformation(new Transformation()
        .width(400).crop("scale").quality("auto").fetchFormat("auto"))
    .generate("sample.jpg");
// With cloud_name "demo": https://res.cloudinary.com/demo/image/upload/c_scale,f_auto,q_auto,w_400/sample.jpg
```

### Transform an asset with a face-detection thumbnail

Chain transformation parameters on the same `url()` builder to crop to a face-detected 90×90 thumbnail. Requires `MediaManager` initialized (see Configuration).

```java
import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;

String thumb = MediaManager.get().url()
    .transformation(new Transformation()
        .width(90).height(90).crop("thumb").gravity("face"))
    .generate("woman.jpg");
// With cloud_name "demo": https://res.cloudinary.com/demo/image/upload/c_thumb,g_face,h_90,w_90/woman.jpg
```

## For AI agents

`cloudinary-android` is the native Android client SDK (Java/Kotlin): on-device upload through a background, retrying queue and in-app transformation and delivery URL building via `MediaManager`. It never holds the API secret — uploads are unsigned (upload presets) or signed by a server through a `SignatureProvider`. For tasks this package doesn't cover, choose a different package:

| Task | Package |
|---|---|
| Native iOS app (Swift/Obj-C) | [`cloudinary_ios`](https://github.com/cloudinary/cloudinary_ios) |
| Cross-platform React Native app | [`cloudinary-react-native`](https://github.com/cloudinary/cloudinary-react-native) |
| Server-side JVM: signed uploads, Admin API, signature generation | [`cloudinary_java`](https://github.com/cloudinary/cloudinary_java) |
| Full sample app to copy from | [`android-demo`](https://github.com/cloudinary/android-demo) |
| Run Cloudinary operations as agent tools | [Cloudinary MCP servers](https://github.com/cloudinary/mcp-servers) |

## Links

- [Android SDK guide](https://cloudinary.com/documentation/android_integration)
- [Upload (presets, callbacks, policy)](https://cloudinary.com/documentation/android_image_and_video_upload)
- [Image transformation](https://cloudinary.com/documentation/android_image_manipulation)
- [Video transformation](https://cloudinary.com/documentation/android_video_manipulation)
- [Documentation llms.txt index](https://cloudinary.com/documentation/llms.txt)
- [Package on Maven Central](https://central.sonatype.com/artifact/com.cloudinary/cloudinary-android)

Released under the MIT license.
