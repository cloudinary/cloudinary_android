Cloudinary Android SDK
======================

Cloudinary is a cloud service that offers a solution to a web application's entire image management pipeline.

Easily upload images to the cloud. Automatically perform smart image resizing, cropping and conversion without installing any complex software.
Integrate Facebook or Twitter profile image extraction in a snap, in any dimension and style to match your website’s graphics requirements.
Images are seamlessly delivered through a fast CDN, and much much more.

Cloudinary offers comprehensive APIs and administration capabilities and is easy to integrate with any web application, existing or new.

Cloudinary provides URL and HTTP based APIs that can be easily integrated with any Web development framework.

## Requirements
The library requires Android version 4.0.3 (Ice Cream Sandwich) and up.

## Gradle Integration
Add the following dependency to your build.gradle:

`implementation group: 'com.cloudinary', name: 'cloudinary-android', version: '1.30.0'`
## Manual Setup ######################################################################
Download cloudinary-android-1.30.0.jar from [here](http://central.maven.org/maven2/com/cloudinary/cloudinary-android/1.30.0/cloudinary-android-1.30.0.aar) and cloudinary-core-1.26.0.jar from [here](http://central.maven.org/maven2/com/cloudinary/cloudinary-core/1.26.0/cloudinary-core-1.26.0.jar) and put them in your libs folder.

## Maven Integration ######################################################################
The cloudinary_android library is available in [Maven Central](http://repo1.maven.org/maven/). To use it, add the following dependency to your pom.xml:

    <dependency>
        <groupId>com.cloudinary</groupId>
        <artifactId>cloudinary-android</artifactId>
        <version>1.30.0</version>
    </dependency>


## Try it right away

Sign up for a [free account](https://cloudinary.com/users/register/free) so you can try out image transformations and seamless image delivery through CDN.

*Note: Replace `demo` in all the following examples with your Cloudinary's `cloud name`.*  

Accessing an uploaded image with the `sample` public ID through a CDN:

    http://res.cloudinary.com/demo/image/upload/sample.jpg

![Sample](https://res.cloudinary.com/demo/image/upload/w_0.4/sample.jpg "Sample")

Generating a 150x100 version of the `sample` image and downloading it through a CDN:

    http://res.cloudinary.com/demo/image/upload/w_150,h_100,c_fill/sample.jpg

![Sample 150x100](https://res.cloudinary.com/demo/image/upload/w_150,h_100,c_fill/sample.jpg "Sample 150x100")

Converting to a 150x100 PNG with rounded corners of 20 pixels:

    http://res.cloudinary.com/demo/image/upload/w_150,h_100,c_fill,r_20/sample.png

![Sample 150x150 Rounded PNG](https://res.cloudinary.com/demo/image/upload/w_150,h_100,c_fill,r_20/sample.png "Sample 150x150 Rounded PNG")

For plenty more transformation options, see our [image transformations documentation](http://cloudinary.com/documentation/image_transformations).

Generating a 120x90 thumbnail based on automatic face detection of the Facebook profile picture of Bill Clinton:

    http://res.cloudinary.com/demo/image/facebook/c_thumb,g_face,h_90,w_120/billclinton.jpg

![Facebook 90x120](https://res.cloudinary.com/demo/image/facebook/c_thumb,g_face,h_90,w_120/billclinton.jpg "Facebook 90x200")

For more details, see our documentation for embedding [Facebook](http://cloudinary.com/documentation/facebook_profile_pictures) and [Twitter](http://cloudinary.com/documentation/twitter_profile_pictures) profile pictures.


## Usage

### Configuration

Each request for building a URL of a remote cloud resource must have the `cloud_name` parameter set. 
Setting the `cloud_name` parameter can be done either when initializing the library, or by using the CLOUDINARY_URL meta-data property in `AndroidManifest.xml`.

The entry point of the library is the `MediaManager` object. `MediaManager.init()` must be called before using the library, preferably in `Application.onCreate()`.
Here's an example of setting the configuration parameters programmatically in your `Applicaion.onCreate(`:
    
     Map config = new HashMap();
     config.put("cloud_name", "myCloudName");
     MediaManager.init(this, config);
    
Alternatively, When using the meta-data property, no configuration is required:
    
    MediaManager.init(this);

The added property `AndroidManifest.xml`. Note: You should only include the `cloud_name` in the value, the api secret and key should be left out of the application.

    <manifest>
        ...
        <application>
            ...
            <meta-data android:name="CLOUDINARY_URL" android:value="cloudinary://@myCloudName"/>
        </application>
    <manifest>



### Embedding and transforming images

Any image uploaded to Cloudinary can be transformed and embedded using powerful view helper methods:

The following example generates the url for accessing an uploaded `sample` image while transforming it to fill a 100x150 rectangle:

    MediaManager.get().url().transformation(new Transformation().width(100).height(150).crop("fill")).generate("sample.jpg")

Another example, embedding a smaller version of an uploaded image while generating a 90x90 face detection based thumbnail: 

    MediaManager.get().url().transformation(new Transformation().width(90).height(90).crop("thumb").gravity("face")).generate("woman.jpg")

You can provide either a Facebook name or a numeric ID of a Facebook profile or a fan page.  
             
Embedding a Facebook profile to match your graphic design is very simple:

    MediaManager.get().url().type("facebook").transformation(new Transformation().width(130).height(130).crop("fill").gravity("north_west")).generate("billclinton.jpg")
                           
Same goes for Twitter:

    MediaManager.get().url().type("twitter_name").generate("billclinton.jpg")

### Uploading

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

##### 1. Unsigned uploads using [Upload Presets.](http://cloudinary.com/blog/centralized_control_for_image_upload_image_size_format_thumbnail_generation_tagging_and_more) 
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
        

## Additional resources ##########################################################

Additional resources are available at:

* [Website](http://cloudinary.com)
* [Documentation](http://cloudinary.com/documentation)
* [Image transformations documentation](http://cloudinary.com/documentation/image_transformations)
* [Upload API documentation](http://cloudinary.com/documentation/upload_images)

## Support

You can [open an issue through GitHub](https://github.com/cloudinary/cloudinary_android/issues).

Contact us at [support@cloudinary.com](mailto:support@cloudinary.com)

Or via Twitter: [@cloudinary](https://twitter.com/#!/cloudinary)

## Join the Community ##########################################################

Impact the product, hear updates, test drive new features and more! Join [here](https://www.facebook.com/groups/CloudinaryCommunity).

## License #######################################################################

Released under the MIT license. 
