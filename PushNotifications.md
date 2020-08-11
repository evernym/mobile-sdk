### Push Notifications
- Key definitions can be found here: https://github.com/evernym/mobile-sdk/blob/master/2.%20Initialization.md#definitions
 - Before you can get started with this, the `Sponsor` needs to be configured with Evernym's Cloud Service. (https://github.com/evernym/mobile-sdk/blob/master/2.%20Initialization.md#sponsor-registration-with-evernyms-cloud-service), a `Sponsor` will need to register with Evernym's Cloud Service. An endpoint is provided during this registration. 
- To push notify a message to your app (`Sponsee`), you will need to setup your own Push Notification System and handle Message Forwarding on your back-end.
- A `Sponsor` will receive a message via Message Forwarding from Evernym's Cloud Service.
### Message Forwarding
- Evernym's Cloud Service is not able to push notify a `Sponsor's` application (e.g. `Sponsee's` app install) because the appropriate push notification (e.g. Firebase) key is not available for Evernym's Cloud Service. \
Evernym's Cloud Service will need to forward the `Sponsee's` incoming message to the `Sponsor` so that the `Sponsor` can properly push notify its `Sponsee`. 

1. Sponsor Registration with Evernym's Cloud Service
 - As mentioned (https://github.com/evernym/mobile-sdk/blob/master/2.%20Initialization.md#sponsor-registration-with-evernyms-cloud-service), a `Sponsor` will need to register with Evernym's Cloud Service. An endpoint is provided during this registration. \
 This `endpoint` will be whatever address the registering `Sponsor` wants Evernym's Cloud Service to use to forward, via HTTPs, incoming messages meant for the `Sponsee`.
 - The `Sponsor's` responsibility is to handle these forwarded messages by push notifying their `Sponsee's` app or using some other preferred communication mechanism. \
 This means a `Sponsor` will have to implement a communication mechanism (e.g. Push Notification Service) with their `Sponsee`.
    
2. Sponsee Provisioning 
 - As mentioned https://github.com/evernym/mobile-sdk/blob/master/2.%20Initialization.md#mobile-sdk---3rd-party-apps-sponsee, a `Sponsee` will need to provision with Evernym's Cloud Service using a token signed by the `Sponsor`.
 - After provisioning is complete, the `Sponsee` needs to register a communication preference (`update_com_method`). \
   The 'Sponsee' will provide 3 values `id`, `type`, and `value`. \
      `id`: is whatever the `Sponsor` uses to identify this specific `Sponsee`. \
      `type`: Int - Com Method Type (ALWAYS `3` for forwarding), \
      `value`: String 
      - When Evernym's Cloud Service receives messages for this entity (through provisioned cloud agent), the cloud agent will forward the message to the `Sponsor`. The `Sponsor` will then deliver the message to the 'Sponsee' with whatever mechanism it already uses to communicate with its customer.  
      - `value` will be the whatever information the `Sponsor's` back-end will need to deliver that message to the 'Sponsee'. (e.g. If a `Sponsor` is using a push notifying service to communicate with its 'Sponsee', this will be the identifier the service needs to push the message)
      
3. Evernym's Cloud Service forwarding messages to the `Sponsor`
- An explanation for this can be found under the heading "3. Receiving Future Messages" at https://github.com/evernym/mobile-sdk/blob/master/2.%20Initialization.md#mobile-sdk---3rd-party-apps-sponsee

