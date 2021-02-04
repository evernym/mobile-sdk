# Frequenly Asked Questions

- **Q: My connection is successfully established but I can't see any message?**

Sometimes changing connection state can be a bit longer, usually a couple of seconds. Please have that in consideration and count this delay in your user flow, without proceeding further in the flow before `conection state` is not successfully changed to **4**.

- **Q: MobileSDK app did not received Push Notification message?**

Make sure you have enabled Push notification capabilities in your project. 
For more details how to set them properly, please follow this guide: 

- [iOS](https://developer.apple.com/library/archive/documentation/Miscellaneous/Reference/EntitlementKeyReference/Chapters/EnablingLocalAndPushNotifications.html)

-  [Android](https://developers.google.com/web/ilt/pwa/introduction-to-push-notifications)

