# AGENTS.md — cloudinary_android

## What this package is (one line)
Native Android client SDK (Java/Kotlin) for Cloudinary: on-device media upload via a background, retrying upload queue, plus in-app transformation and delivery URL building. Current release **3.1.2** (minSdk 21, compileSdk 34).

## When to use this / when NOT to use this
- **Use this when:** you are in a native Android app (Java or Kotlin) and need to upload from the device (files, `Uri`, camera captures) through a background queue with retries and network policies, or build delivery/transformation URLs in-app with `MediaManager.get().url()`.
- **Do NOT use this when:** you are building a **cross-platform React Native app** (use [cloudinary-react-native](https://github.com/cloudinary/cloudinary-react-native)) or doing **server-side JVM work** — signed uploads, the Admin API, signature generation (use [cloudinary_java](https://github.com/cloudinary/cloudinary_java); that is also where the server-side signature this SDK consumes is generated).
- **Sibling packages:** `cloudinary_java` = server-side JVM SDK + signature generation; `cloudinary-react-native` = cross-platform mobile; [android-demo](https://github.com/cloudinary/android-demo) = full sample app to copy from. Rule of thumb: runs **on the device** → this SDK; holds the `api_secret` → server only, use `cloudinary_java`.

## Repo layout (multi-module Gradle)
Modules (from `settings.gradle`): `:core` (URL building + upload engine), `:preprocess`, `:ui`, `:glide-integration`, `:download`, `:all` (aggregate), `:sample` (demo app). Public entry point for app code is `MediaManager`.

## Setup
Add the dependency to your app `build.gradle`:
```gradle
implementation 'com.cloudinary:cloudinary-android:3.1.2'
```
Configuration / credentials — only `cloud_name` is needed (no `api_key`/`api_secret`). Either set it programmatically in `MediaManager.init`, or via `AndroidManifest.xml` meta-data:
```xml
<meta-data android:name="CLOUDINARY_URL" android:value="cloudinary://@myCloudName"/>
```

## Minimal runnable example
```java
// In your Application.onCreate():
Map<String, String> config = new HashMap<>();
config.put("cloud_name", "myCloudName");
MediaManager.init(this, config);

// Build a delivery URL anywhere in the app:
String url = MediaManager.get().url()
    .transformation(new Transformation().width(100).height(150).crop("fill"))
    .generate("sample.jpg");

// Queue an unsigned upload (background queue, auto-retry).
// upload() takes a file-path String, Uri, byte[], or raw-resource int — no File overload.
String requestId = MediaManager.get().upload(imageFile.getAbsolutePath())
    .unsigned("sample_preset")
    .callback(callback)   // any UploadCallback impl
    .dispatch();
```

## Build / test commands (run these after editing)
Requires **JDK 17** (per CI). Use the Gradle wrapper (`./gradlew`, `gradlew.bat` on Windows).
```bash
./gradlew assemble        # build all modules
./gradlew :core:assemble  # build a single module
```
Tests are **instrumented** (`androidTest`) and run on a device/emulator — CI runs `connectedCheck`, not plain `test`:
```bash
./gradlew clean connectedCheck --stacktrace   # the exact command CI runs
```
`connectedCheck` requires a running emulator or attached device **and** a configured account: CI exports `CLOUDINARY_URL` before running (`export CLOUDINARY_URL=$(bash tools/get_test_cloud.sh)`). Set `CLOUDINARY_URL` (`cloudinary://<api_key>:<api_secret>@<cloud_name>`) in your environment before running tests locally. CI does not wire a lint task — the stock `./gradlew lint` exists but is not part of the project's checks.

## Conventions & gotchas
- **`MediaManager.init()` must be called before any other SDK call** — do it in `Application.onCreate()`, not per-Activity.
- **Never ship the `api_secret` in the app.** Uploads are either unsigned (upload presets) or signed by your server via a `SignatureProvider` passed to `MediaManager.init`. This SDK has no Admin API and cannot sign locally.
- All uploads are **asynchronous** — `dispatch()` returns a `requestId`; results arrive through `UploadCallback`. Use `UploadPolicy` / `TimeWindow` to constrain retries, network type, and run window.
- **minSdk 21**, compileSdk 34 (verified in `core/build.gradle`). 3.x targets Android SDK 21 and up; do not drop below.
- Multi-module: change code in the owning module (`:core` for URL/upload logic) and rebuild that module.

## Canonical docs (leave the repo for depth)
- Android SDK guide: https://cloudinary.com/documentation/android_integration
- Upload (presets, callbacks, policy): https://cloudinary.com/documentation/android_image_and_video_upload
- Image / video transformation: https://cloudinary.com/documentation/android_image_manipulation
- MCP server (agent/no-code path): https://github.com/cloudinary/mcp-servers

## Agent / MCP note
If a capability is also exposed via the Cloudinary MCP servers, prefer the MCP tool for autonomous task execution and use this SDK for code generation inside an Android app. See cloudinary/mcp-servers.

## Commit / PR conventions
- Open issues/PRs against https://github.com/cloudinary/cloudinary_android. See `CONTRIBUTING.md`.
- CI (`.github/workflows/android-build.yml`) runs `./gradlew clean connectedCheck` on push and PR — ensure instrumented tests pass on an emulator before opening a PR. The repo documents no commit-message convention, so match the existing history.
