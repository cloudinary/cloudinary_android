@AGENTS.md

# CLAUDE.md — cloudinary_android

## What this repo is

Native Android client SDK (Java/Kotlin) for Cloudinary: on-device media upload via a background, retrying queue, plus in-app transformation and delivery URL building via `MediaManager`. Current release: **3.1.2** (minSdk 21, compileSdk 34).

## Key constraints / gotchas

- **`MediaManager.init()` must be called exactly once in `Application.onCreate()`** — not per-Activity. Calling it twice throws `IllegalStateException: MediaManager is already initialized`.
- **Never ship `api_secret` in the app.** Uploads use unsigned upload presets or a `SignatureProvider` that calls your server. This SDK has no Admin API and cannot sign locally.
- `upload()` accepts a file-path `String`, `Uri`, `byte[]`, or raw-resource `int` — **there is no `File` overload**. Use `imageFile.getAbsolutePath()` for files.
- `dispatch()` is async — returns a `requestId` immediately; results arrive through `UploadCallback` (`onStart`, `onProgress`, `onSuccess`, `onError`, `onReschedule`).
- Multi-module Gradle project: `:core` (URL building + upload engine), `:preprocess`, `:ui`, `:glide-integration`, `:download`, `:all` (aggregate), `:sample`. Public entry point is `MediaManager`.
- `CLOUDINARY_URL` for tests: `cloudinary://<api_key>:<api_secret>@<cloud_name>`. In the manifest, only the cloud name goes in — `cloudinary://@<cloud_name>` (no secret).
- CI does not wire a lint task — `./gradlew lint` exists but is not part of the project's checks.

## Verified build / test commands

Requires **JDK 17** (per CI). Use the Gradle wrapper (`./gradlew` on Linux/macOS, `gradlew.bat` on Windows).

```bash
./gradlew assemble              # build all modules
./gradlew :core:assemble        # build a single module
```

Tests are **instrumented** (`androidTest`) and require a running emulator or attached device:

```bash
export CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>
./gradlew clean connectedCheck --stacktrace
```

CI runs `./gradlew clean connectedCheck` and exports `CLOUDINARY_URL` via `tools/get_test_cloud.sh` before the run. Plain `./gradlew test` does not run the instrumented suite.
