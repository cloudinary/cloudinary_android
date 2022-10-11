Cloudinary Android SDK
======================
[![Build Status](https://api.travis-ci.com/cloudinary/cloudinary_android.svg?branch=master)](https://app.travis-ci.com/github/cloudinary/cloudinary_android)

## About
The Cloudinary Android SDK allows you to quickly and easily integrate your application with Cloudinary.
Effortlessly optimize and transform your cloud's assets.

### Additional documentation
This Readme provides basic installation and usage information.
For the complete documentation, see the [Android SDK Guide](https://cloudinary.com/documentation/android_integration).

## Table of Contents
- [Key Features](#Key-Features)
- [Compatibility](#Version-Support)
- [Installation](#Installation)
- [Usage](#Usage)
    - [Setup](#Setup)
    - [Transform and Optimize Assets](#Transforming-and-Optimizing-Assets)
    - [Uploading Asset](#Uploading-Assets)

## Key Features
* [Image Transformation](https://cloudinary.com/documentation/android_image_manipulation)
* [Video Transformation](https://cloudinary.com/documentation/android_video_manipulation)
* [Direct File Upload](https://cloudinary.com/documentation/android_image_and_video_upload)
* [Preprocess](https://cloudinary.com/documentation/android_image_and_video_upload#preprocess_uploads)
* [Callbacks](https://cloudinary.com/documentation/android_image_and_video_upload#callbacks)
* [Upload Policy](https://cloudinary.com/documentation/android_image_and_video_upload#upload_policy)
* [Error Handling](https://cloudinary.com/documentation/advanced_url_delivery_options#error_handling)

## Version Support
| Cloudinary SDK | Android SDK | 
|----------------|-------------|
|      2.x       |    > 19     |
|      1.x       |    > 14     |


## Installation

### Gradle Integration
Add the following dependency to your build.gradle:

`implementation 'com.cloudinary:cloudinary-android:2.3.0'`
### Other Options ######################################################################
The cloudinary_android library is available in [Maven Central](http://repo1.maven.org/maven/). To use it, add the following dependency to your pom.xml:

    <dependency>
        <groupId>com.cloudinary</groupId>
        <artifactId>cloudinary-android</artifactId>
        <version>2.3.0</version>
    </dependency>

Download the latest cloudinary-android from [here](https://mvnrepository.com/artifact/com.cloudinary/cloudinary-android-core) and the latest cloudinary-core from [here](https://mvnrepository.com/artifact/com.cloudinary/cloudinary-core) and put them in your libs folder.


## Usage

### Setup

Each request for building a URL of a remote cloud resource must have the `cloud_name` parameter set.
You can set the `cloud_name` parameter either when initializing the library, or by using the CLOUDINARY_URL meta-data property in `AndroidManifest.xml`.

The entry point of the library is the `MediaManager` object. `MediaManager.init()` must be called before using the library, preferably in `Application.onCreate()`.
Here's an example of setting the configuration parameters programmatically in your `Applicaion.onCreate(`:

     Map config = new HashMap();
     config.put("cloud_name", "myCloudName");
     MediaManager.init(this, config);

Alternatively, you can use the meta-data property. In that case, no configuration is required:

    MediaManager.init(this);

Only the cloud_name should be included. Your API key and secret aren't necessary.
Note: Your API secret should never be exposed in the application.

    <manifest>
        ...
        <application>
            ...
            <meta-data android:name="CLOUDINARY_URL" android:value="cloudinary://@myCloudName"/>
        </application>
    <manifest>

### Transforming and Optimizing Assets

Any image uploaded to Cloudinary can be transformed and embedded using powerful view helper methods:

The following example generates the url for accessing an uploaded `sample` image while transforming it to fill a 100x150 rectangle:

    MediaManager.get().url().transformation(new Transformation().width(100).height(150).crop("fill")).generate("sample.jpg")

Another example, embedding a smaller version of an uploaded image while generating a 90x90 face detection based thumbnail:

    MediaManager.get().url().transformation(new Transformation().width(90).height(90).crop("thumb").gravity("face")).generate("woman.jpg")

If your application is written in Kotlin you can use the syntax below:

    MediaManager.get().url().transformation(Transformation<Transformation<*>>().width(90).height(90).crop("thumb").gravity("face")).generate("woman.jpg")

You can provide either a Facebook name or a numeric ID of a Facebook profile or a fan page.

Embedding a Facebook profile to match your graphic design is very simple:

    MediaManager.get().url().type("facebook").transformation(new Transformation().width(130).height(130).crop("fill").gravity("north_west")).generate("billclinton.jpg")

### Uploading Assets

The entry point for upload operations is the `MediaManager.get().upload()` call. All upload operations are dispatched to a background queue, with
a set of fully customizable rules and limits letting you choose when each upload request should actually run. Requests are automatically rescheduled to be
retried later if a recoverable error is encountered (e.g. network disconnections, timeouts).

The upload results are dispatched asynchronously using `UploadCallback`. Global callbacks can be defined, as well as specific callbacks per request.
Note: In order to receive global callbacks even when the app is already shut down, or in the background, the `ListenerService` class can be extended and registered in the manifest (see the class for further instructions).

The following examples uploads a `File`  using the default settings, a request upload callback, and an upload preset (more about upload presets below):

    String requestId = MediaManager.get().upload(imageFile).unsigned("sample_preset").callback(callback).dispatch();

The returned `requestId` is used to identify the request in global callbacks and to cancel the request if needed. The callback should be any implementation of `UploadCallback`.

The uploaded image is assigned a randomly generated public Id. As soon as `onSuccess` is called, the image is immediately available for download through a CDN:

    MediaManager.get().url().generate("abcfrmo8zul1mafopawefg.jpg")
      
    http://res.cloudinary.com/demo/image/upload/abcfrmo8zul1mafopawefg.jpg

You can also specify your own public ID:

    String requestId = MediaManager.get().upload(uri).unsigned("sample_preset").option("public_id", "sample_remote").dispatch();

Using `RequestUploadPolicy`, an upload request can be configured to run under specific circumstance, or within a chosen time window:

The following examples uploads local Uri resource, configured to run immediately (the default), with a maximum of 7 retries, and only on an unmetered network (e.g. wifi):

    String requestId = MediaManager.get().upload(uri)
        .unsigned("sample_app_preset")
        .constrain(TimeWindow.immediate())
        .policy(new RequestUploadPolicy.Builder().maxRetries(7).networkPolicy(RequestUploadPolicy.NetworkType.UNMETERED).build())
        .dispatch();

For security reasons, mobile applications cannot contain the full account credentials, and so they cannot freely upload resources to the cloud.
Cloudinary provides two different mechanisms to enable end-users to upload resources without providing full credentials.

##### 1. Unsigned uploads using [Upload Presets.](https://cloudinary.com/documentation/android_image_and_video_upload)
You can create an upload preset in your Cloudinary account console, defining rules that limit the formats, transformations, dimensions and more.
Once the preset is defined, it's name is supplied when calling upload. An upload call will only succeed if the preset name is used and the resource is within the preset's pre-defined limits.

The following example uploads a local resource, available as a Uri, assuming a preset named 'sample_preset' already exists in the account:

    String requestId = MediaManager.get().upload(uri).unsigned("sample_preset").dispatch();

##### 2. Signed uploads with server-based signature
Another way to allow uploading without credentials is using signed uploads.
It is recommended to generate the upload authentication signature on the server side, where it's safe to store the `api_secret`.

Cloudinary's Android SDK allows providing server-generated signature and any additional parameters that were generated on the server side (instead of signing using `api_secret` locally).

Your server can use any Cloudinary libraries (Ruby on Rails, PHP, Python & Django, Java, Perl, .Net, etc.) for generating the signature. The following JSON in an example of a response of an upload authorization request to your server:

	{
	  "signature": "sgjfdoigfjdgfdogidf9g87df98gfdb8f7d6gfdg7gfd8",
	  "public_id": "abdbasdasda76asd7sa789",
	  "timestamp": 1346925631,
	  "api_key": "123456789012345"
	}

When initializing `MediaManager`, a `SignatureProvider` can be sent. Whenever an upload requires signing, the library will call the provider's `provideSignature()` method,
where you should implement the call to your server's signing endpoint. This callback runs on a background a thread so there's no need to handle threading:

    MediaManager.init(this, new SignatureProvider() {
        @Override
        public Signature provideSignature(Map options) {
            // call server signature endpoint
        }
    }, null);

## Contributions
See [contributing guidelines](/CONTRIBUTING.md).

## Get Help
If you run into an issue or have a question, you can either:
- [Open a Github issue](https://github.com/cloudinary/cloudinary_android/issues) (for issues related to the SDK)
- [Open a support ticket](https://cloudinary.com/contact) (for issues related to your account)

## About Cloudinary
Cloudinary is a powerful media API for websites and mobile apps alike, Cloudinary enables developers to efficiently manage, transform, optimize, and deliver images and videos through multiple CDNs. Ultimately, viewers enjoy responsive and personalized visual-media experiences—irrespective of the viewing device.

## Additional resources

- [Cloudinary Transformation and REST API References](https://cloudinary.com/documentation/cloudinary_references): Comprehensive references, including syntax and examples for all SDKs.
- [MediaJams.dev](https://mediajams.dev/): Bite-size use-case tutorials written by and for Cloudinary Developers
- [DevJams](https://www.youtube.com/playlist?list=PL8dVGjLA2oMr09amgERARsZyrOz_sPvqw): Cloudinary developer podcasts on YouTube.
- [Cloudinary Academy](https://training.cloudinary.com/): Free self-paced courses, instructor-led virtual courses, and on-site courses.
- [Code Explorers and Feature Demos](https://cloudinary.com/documentation/code_explorers_demos_index): A one-stop shop for all code explorers, Postman collections, and feature demos found in the docs.
- [Cloudinary Roadmap](https://cloudinary.com/roadmap): Your chance to follow, vote, or suggest what Cloudinary should develop next.
- [Cloudinary Facebook Community](https://www.facebook.com/groups/CloudinaryCommunity): Learn from and offer help to other Cloudinary developers.
- [Cloudinary Account Registration](https://cloudinary.com/users/register/free): Free Cloudinary account registration.
- [Cloudinary Website](https://cloudinary.com): Learn about Cloudinary's products, partners, customers, pricing, and more.

## Licence
Released under the MIT license.
