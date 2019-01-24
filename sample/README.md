
Cloudinary Sample App
==========

## Configuration
Once you clone this repository there are two required steps to build the sample app:
1. Configure your Cloudinary cloud name for the app:
    * Once you open the project (from the repository root) a file named `gradle-local.properies` should be automatically created in the repository root (you can manually create it if it's not there). Note: This file is ignored by git and should never be checked in. 
    * The file should contain a single property with your cloudinary url, stripped to cloud name only: `cloudinaryUrl=cloudinary://@myCloudName`
    * Run `./gradlew clean` (`gradlew clean` on Windows) from the repository root before proceeding. 
2. Create an upload preset named 'sample_app_preset' in your cloudinary account console:
    *  Login to your [Cloudinary console](https://cloudinary.com/console), go to settings>upload, scroll down
      to Upload Presets and click `Add upload preset`. Alternatively, head directly to the [new preset page](https://cloudinary.com/console/lui/upload_presets/new).
    * Type in `android_sample` as the name and save, leaving all the fields with their default values.
    
After completing the steps above the sample app should be ready to be deployed and upload resource to your Cloudinary cloud.

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

## License #######################################################################

Released under the MIT license. 
