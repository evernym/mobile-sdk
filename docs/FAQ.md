# Frequenly Asked Questions

- **Q: My connection is successfully established but I can't see any message?**

    Sometimes changing connection state can be a bit longer, usually a couple of seconds. Please have that in consideration and count this delay in your user flow, without proceeding further in the flow before `conection state` is not successfully changed to **4**.

- **Q: MobileSDK app did not received Push Notification message?**

    Make sure you have enabled Push notification capabilities in your project. 
    For more details how to set them properly, please follow this guide: 
    
    - [iOS](https://developer.apple.com/library/archive/documentation/Miscellaneous/Reference/EntitlementKeyReference/Chapters/EnablingLocalAndPushNotifications.html)
    
    - [Android](https://developers.google.com/web/ilt/pwa/introduction-to-push-notifications)

- **Q: What is CAS?**

     CAS stands for Consumer Agency Service. This service is hosted by Evernym. It creates and hosts cloud agents for individual app installs. Evernym's production CAS is hosted at `https://agency.evernym.com`. To communicate with CAS, developers would also need to know CAS's `DID` and `verification key`. To know `DID` and `verification key` of Evernym's CAS or EAS, developers can make a GET API call to `https://agency.evernym.com/agency/`.

- **Q: What is EAS?**

     Enterprise Agency Service (EAS) hosted by Evernym is responsible for creating and hosting cloud agent for Enterprise wallets. Mobile SDK, almost always interact with CAS, not EAS.

- **Q: What is the difference between Evernym Agency Service and the cloud agent?**

     Evernym's Agency Service creates and hosts individual cloud agents for each unique app install of Connect.Me, or each instance of our mobile SDK.
     Cloud agents primary value is for storing and forwarding messages, while the edge agent (the app on the phone) comes on and offline. It also provides push notification services.

- **Q: Can I use mobile SDK in Xamarin?**

     Yes, mobile SDK can be used in Xamarin. However, Evernym does not provide .NET wrapper for mobile SDK. Mobile SDK contains native Android and iOS wrappers. These native Android and iOS wrappers can be used in Xamarin in same way as other native plugin/modules of Android and iOS are used.

- **Q: Can I use mobile SDK in Xamarin?**

     Yes, mobile SDK can be used in Flutter. However, mobile SDK does not contain flutter bindings for mobile SDK. Mobile SDK can be integrated in Flutter as other native Android and iOS plugins are used.

- **Q: How to get started with mobile SDK?**

    - Get access to mobile SDK. It should be available to download from `release tab`
    - Familiarize yourself with [basic concepts](./0.Base%20Concepts.md)
    - Integrate mobile SDK in your app using this [guide](./1.ProjectSetup.md)
    - Run through other markdown files from #2 to #9

- **Q: How to send a credential with an image/photo?**

    Check this [guide](CredentialsWithAttachments.md)

- **Q: How to send a message with custom action?**

    Check this [guide](8.StructuredMessages.md)

- **Q: Do connections and credentials are stored between wallet re-installations?**

    No, when you delete the application wallet all data get lost. 
    When you install the application again it does provisioning of a new Cloud Agent (`provision_with_token`).
    The newly created Cloud Agent does not know anything about previous. So all previous connections and credentials will be lost. 

- **Q: What does the `Item not found on ledger` error means?**

  This error occurs when the Holder mobile application and Issue service are connected to different Pool Ledger Networks.

- **Q: APK file size is too huge after integrating SDK**

  We can split build by ABI. Here is one link that describes the process for [ABI split](https://developer.android.com/studio/build/configure-apk-splits).
  
  We would suggest to use 4 ABIs (arm64, arm32, x86_64 and x86). Assign version code to each split ABI. This version code gets appended to apk version number and let us identify the architecture of apk.  You can configure gradle with ABI split as shown in below code

```gradle
    splits {
       abi {
           reset()
           enable enableSeparateBuildPerCPUArchitecture
           universalApk false  // If true, also generate a universal APK
           include "x86" , "armeabi-v7a", "arm64-v8a", "x86_64"
       }
   }

// Map for the version code that gives each ABI a value.
// [ABI_NUMBER] is 10 - 10 for 'x86', 11 for 'armeabi-v7a', 12 for 'arm64-v8a', 13 for 'x86_64' - a two
// digit number to represent the CPU architecture or Application Binary Interface

ext.abiCodes = ['x86':'10', 'armeabi-v7a':'11', 'arm64-v8a':'12', 'x86_64':'13']
android.applicationVariants.all { variant ->
  variant.outputs.each { output ->
    // https://developer.android.com/studio/build/configure-apk-splits.html
    // Determines the ABI for this variant and returns the mapped value.
    def endingAbiCode = project.ext.abiCodes.get(output.getFilter(OutputFile.ABI))

    // Because abiCodes.get() returns null for ABIs that are not mapped by ext.abiCodes,
    // the following code does not override the version code for universal APKs.
    // However, because we want universal APKs to have the lowest version code,
    // this outcome is desirable.
    if (endingAbiCode != null) {

      // Assigns the new version code to versionCodeOverride, which changes the version code
      // for only the output APK, not for the variant itself. Skipping this step simply
      // causes Gradle to use the value of variant.versionCode for the APK.
      def nextVersionCode = variant.versionCode.toString() + endingAbiCode
      output.versionCodeOverride = nextVersionCode.toInteger()
    }
  }
}
```

- **Q: How to manage multiple apks after using abi split functionality as described above**

  There are different ways to manage multiple apks. We recommend to use a CI/CD system for app deployments to internal CI/CD platform and also to Play Store. In this way, it would become very easy to distribute all apks as per device architecture. We use [Fastlane](https://fastlane.tools/) for all our mobile CI/CD tasks and most mobile CI/CD platforms support fastlane scripts by default. Below is the fastlane script to upload all apks to AppCenter and to the Play Store:

  ```ruby
    # upload all 4 apks to appcenter

    appcenter_upload(api_token: ENV["APPCENTER_API_TOKEN"], owner_type: "organization", owner_name: "<owner-name>", app_name: "<app-name>", file: "app/build/outputs/apk/release/app-armeabi-v7a-release.apk")
    appcenter_upload(api_token: ENV["APPCENTER_API_TOKEN"], owner_type: "organization", owner_name: "<owner-name>", app_name: "<app-name>", file: "app/build/outputs/apk/release/app-arm64-v8a-release.apk")
    appcenter_upload(api_token: ENV["APPCENTER_API_TOKEN"], owner_type: "organization", owner_name: "<owner-name>", app_name: "<app-name>", file: "app/build/outputs/apk/release/app-x86-release.apk")
    appcenter_upload(api_token: ENV["APPCENTER_API_TOKEN"], owner_type: "organization", owner_name: "<owner-name>", app_name: "<app-name>", file: "app/build/outputs/apk/release/app-x86_64-release.apk")

    # upload all 4 apks to Play Store
    supply(
      track: "beta",
      apk_paths: [
        "app/build/outputs/apk/release/app-armeabi-v7a-release.apk",
        "app/build/outputs/apk/release/app-arm64-v8a-release.apk",
        "app/build/outputs/apk/release/app-x86-release.apk",
        "app/build/outputs/apk/release/app-x86_64-release.apk"
      ],
    )
  ```

  Play Store automatically detects device architecture and will show the build to the user which is meant for their device. For internal CI/CD platform it can happen that they don't support automatic device architecure and hence show all four apks per build. For that purpose, we have to remember code for apk type. Most of the devices are 64 bit devices, and we just might need to remember single code for internal testing. 

  Another approach is to use `abi split` approach only for production builds that needs to go to Play Store. Use universal builds for development and internal testing or for anything other than play store such as AppCenter, Bitrise, App Circle, Code Magic, etc. In this way, internal testing will only have single build and won't need to remember any code. When we go to Play Store, then it will automatically select the build as per device architecture. In this approach, we need to change our deployment script and needs to bit of extra work. Here is how we can do it.

  - Use `bash` or any other script to change this line in build.gradle `universalApk false` and set `false` to `true` before the pipeline that needs to deploy to internal CI/CD platform. We can use this `sed` comand to change false to true `$ sed -i 's/universalApk\sfalse/universalApk\strue/gI' build.gradle` 
  - Add `if...else` in fastlane script for uploading single apk for internal CI/CD build, and multiple apks for Play Store deployment
