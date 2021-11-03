### Push Notifications

- Key definitions can be found [here](3.Initialization.md#definitions)

- Before you can get started with this, you as the `Sponsor` need to be [onboarded](3.Initialization.md#sponsor-ie-you-onboarding-with-evernyms-cloud-service) with Evernym's Cloud Service. You as the sponsor will need to provide an endpoint during this process.

- To push notify a message to your customer's app (`Sponsee`), you will need to setup your own Push Notification System and handle Message Forwarding on your back-end.

- You as the `Sponsor` will receive messages via Message Forwarding from Evernym's Cloud Service. These messages will contain enough information to push-notify your cutomer (`Sponsee`)

### Message Forwarding

![](/wiki-images/Push%20Notifications%20Diagram.png)

- Each instance of the mobile SDK (each Sponsee) has an associated cloud agent which provides a store-and-forward function for messages traffic to and from your app.
- Evernym does not handle your push notification service because it would require us to hold push notification keys to your app (`Sponsee`).
- Instead, you will need to host your own push notification service, and receive forwarded messages from each `Sponsee`'s cloud agent. This is done by registering with Evernym as a `Sponsor`. See below.

1 Sponsor Registration with Evernym's Cloud Service
 - [As mentioned](3.Initialization.md#sponsor-registration-with-evernyms-cloud-service), you as a `Sponsor` will need to register with Evernym's Cloud Service. An endpoint is provided during this registration. \
 This `endpoint` will be whatever address you (`Sponsor`) want Evernym's Cloud Service to use to forward, via HTTPs, incoming messages meant for the end customer (`Sponsee`).
 - Your (`Sponsor's`) responsibility is to handle these forwarded messages by push notifying your customer's (`Sponsee's`) app or using some other preferred communication mechanism. \
 This means you (`Sponsor`) will have to implement a communication mechanism (e.g. Push Notification Service) with your customer (`Sponsee`).

2 Sponsee Provisioning 
 - As mentioned [here](3.Initialization.md#mobile-sdk-customer-provisioning-overview), a customer (`Sponsee`) will need to provision with Evernym's Cloud Service using a token signed by you (`Sponsor`).
 
 - After provisioning is complete, the customer's application (`Sponsee`) will need to call MSDK method to set communication method with the agent.
     * Android - `UtilsApi.vcxUpdateAgentInfo(config)`
     * iOS - `agentUpdateInfo:config`
         ```
         where config is JSON: 
         {
             `id`: String - whatever the `Sponsor` uses to identify this specific `Sponsee`.
             `type`: Int - Com Method Type (ALWAYS `4` for forwarding),
             `value`: String 
         }
         ```
 
3 Evernym's Cloud Service forwarding messages to the you as the `Sponsor`

  - The customer's `Sponsee's` messages are forwarded to you (the `Sponsor`). `value` will be the whatever information your (`Sponsor's`) back-end will need to deliver that message to the customer (`Sponsee`). You will receive this `value` in the field `sponseeDetails` in the message from CAS. 
  
  - When Evernym's Cloud Service receives a message for this entity (through provisioned cloud agent), the cloud agent will forward the message to you (`Sponsor`) at whichever endpoint that was provided during [Onboarding](3.Initialization.md#your-sponsor-onboarding-with-evernyms-cloud-service).
    
  - The cloud agent will forward messages to you (the `Sponsor's`) back-end via http. No A2A encryption will be added. One thing to note here is that this webhook call does not contain cutomer's message. Your customer's cloud agent is only notifying your endpoint that there is a new message available for your customer. The http message will include:
      1. `msgId`:  This is the id that your customer (`Sponsee`) will use to actually download the message
      2. `sponseeDetails`: You (`Sponsor`) will be forwarded your customer's `Sponsee` messages. This will be the whatever information your (the `Sponsor's`) back-end will need to deliver that message to the customer (`Sponsee`). Contains the `value` that you sent into `update_com_method`. 
      3. `relationshipDid` - My DID - the specific relationship I'm interacting on.  
      4. `metaData` - message type and sender's name. This is used mostly to display the message for the customer (`Sponsee`). These values are optional from the sender's perspective.
    
          ```json
          {
              msgId: String,
              sponseeDetails: String, 
              relationshipDid: String,
              metaData: {
              msgType: String,
              msgSenderName: String,
              }
          }
          ```
  
  - You `Sponsor` will then deliver the message to the customer `Sponsee` with whatever mechanism you already uses to communicate with your customer.  

      
<!-- ## Sponsor provisioning overview

### First run - standard push notification flow
![First run - standard push notification flow](wiki-images/FIRST_RUN_ MobileApp-Regular-standard-push-notification-flow.png)

### App start - user accepted push notification flow
![App start - user accepted push notification flow](wiki-images/APP-START-User-accepted-push-notifications.png)

### APP running after uploading push token to sponsor web service

![APP running after uploading push token to sponsor web service](wiki-images/APP-Running-After-uploading-push-token.png)
 -->


### Push notifications setup

For in more depth information how push notifications works in one of the platforms, you can read official documentation sections for: 
   
   - Android: [https://developer.android.com/guide/topics/ui/notifiers/notifications](https://developer.android.com/guide/topics/ui/notifiers/notifications)
   - iOS: [https://developer.apple.com/documentation/usernotifications/](https://developer.apple.com/documentation/usernotifications/)


From Vcx and cloud agent perspective, you will need to link received **push notification token** you received on your device with your cloud agent in Vcx, so notifications will be sent to correct device. 



* iOS: After wallet initialization steps (eg. AppDelegate.m) call `appDelegate.sdkApi agentUpdateInfo:config` function

* Android: After wallet initialization steps (eg. ConnectMeVCX.java) call `UtilsApi.vcxUpdateAgentInfo(config)` function

   ```
   where config is JSON string:
   {
        `id`: String - `Sponsee's` Id, can be device ID
        `type`: Int - Com Method Type (ALWAYS `4` for forwarding),
        `value`: String - FCM:{pushToken}
   }
   ```

We also recommend using Firebase Cloud Messaging, since it's provide universal configuration for both platforms (iOS and Android): https://firebase.google.com/docs/cloud-messaging/android/client. 

How to link your push notification with VCX cloud agent? 

For the moment, sending push notifications should happen from user's cloud agent (which will receive notification from Evernym cloud agent) so all mobile device tokens are stored on your side, safely and securely.

We are working also on improving service related to push notifications, we should have more details soon.

### Processing push notifications on mobile device

Once you (Sponsor) receive a message on your webhook. You can send push notification to your customer's app. 
Inside the app you can use method ```downloadMessages``` in ObjC or ```UtilsApi.vcxGetMessages``` in Java, for pulling either all messages waiting on cloud agent, or a specific one matching to received notification.

See [messages documentation](4.MessagesFlow.md) for message download information.
