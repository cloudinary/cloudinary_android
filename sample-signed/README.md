
Cloudinary server-signed uploads sample app
==========

This samples demonstrates the mechanism of signed uploads using a Google cloud endpoints backend server. The sample app will call the server to generate an upload signature, allowing the end users to uploads images without having to store credentials  in the mobile client (You can read more about it in the library's readme file). 
## Configuration
Once you clone this repository you need to open the advanced-samples folder (not the root!) and follow these steps:
1. Configure your Cloudinary cloud name for the sample app:
    * Once you open the project a file named `gradle-local.properies` should be automatically created in the repository root (you can manually create it if it's not there). Note: This file is ignored by git and should never be checked in. 
    * The file should contain a single property with your cloudinary url, stripped to cloud name only: `cloudinaryUrl=cloudinary://@myCloudName`
    * Run `./gradlew clean` (`gradlew clean` on Windows) from the repository root before proceeding. 
2. Configure the full credentials for the backend module. The best practice is to create an environment variable with your Cloudinary credentials:

    `CLOUDINARY_URL=cloudinary://1234567890:abcdefghijklmno@myCloudName`
    
    This will be automatically picked by the backend module if setup correctly. You can find your cloudinary url in the [console](https://cloudinary.com/console)

To see the app in action you should run the backend module, and once it's up run the sample-signed module. 
Note: The default configuration assumes the sample-app runs on an emulator on the same machine that runs the backend. To test it on a real device you need to update the network configuration in the `BackendServerSignatureProvider` class. 

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
