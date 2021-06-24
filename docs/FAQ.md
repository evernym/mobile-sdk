# Frequenly Asked Questions

- **Q: My connection is successfully established but I can't see any message?**

    Sometimes changing connection state can be a bit longer, usually a couple of seconds. Please have that in consideration and count this delay in your user flow, without proceeding further in the flow before `conection state` is not successfully changed to **4**.

- **Q: MobileSDK app did not received Push Notification message?**

    Make sure you have enabled Push notification capabilities in your project. 
    For more details how to set them properly, please follow this guide: 
    
    - [iOS](https://developer.apple.com/library/archive/documentation/Miscellaneous/Reference/EntitlementKeyReference/Chapters/EnablingLocalAndPushNotifications.html)
    
    -  [Android](https://developers.google.com/web/ilt/pwa/introduction-to-push-notifications)

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

    Check this [guide](./6.StructuredMessages.md)
