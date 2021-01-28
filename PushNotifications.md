### Push Notifications
- Key definitions can be found here: https://github.com/evernym/mobile-sdk/blob/master/2.%20Initialization.md#definitions
 - Before you can get started with this, you as the `Sponsor` need to be configured/onboarded with Evernym's Cloud Service. (https://github.com/evernym/mobile-sdk/blob/master/2.%20Initialization.md#your-sponsor-onboarding-with-evernyms-cloud-service). You as the sponsor will need to provide an endpoint during this process.
- To push notify a message to your customer's app (`Sponsee`), you will need to setup your own Push Notification System and handle Message Forwarding on your back-end.
- You as the `Sponsor` will receive messages via Message Forwarding from Evernym's Cloud Service. These messages will contain enough info to enable to you push-notify your cutomer (`Sponsee`)
### Message Forwarding

<p align="center">
 <img src="https://github.com/evernym/mobile-sdk/blob/master/wiki-images/Push%20Notifications%20Diagram.png">
</p>


- Evernym's Cloud Service is not able to push notify your (`Sponsor's`) application (e.g. `Sponsee's` app install) because the appropriate push notification (e.g. Firebase) key is not available for Evernym's Cloud Service. \
Evernym's Cloud Service will need to forward the customer's (`Sponsee's`) incoming message to you (`Sponsor`) so that you can properly push notify your customer. 

1. Sponsor Registration with Evernym's Cloud Service
 - As mentioned (https://github.com/evernym/mobile-sdk/blob/master/2.%20Initialization.md#sponsor-registration-with-evernyms-cloud-service), you as a `Sponsor` will need to register with Evernym's Cloud Service. An endpoint is provided during this registration. \
 This `endpoint` will be whatever address you (`Sponsor`) want Evernym's Cloud Service to use to forward, via HTTPs, incoming messages meant for the end customer (`Sponsee`).
 - Your (`Sponsor's`) responsibility is to handle these forwarded messages by push notifying your customer's (`Sponsee's`) app or using some other preferred communication mechanism. \
 This means you (`Sponsor`) will have to implement a communication mechanism (e.g. Push Notification Service) with your customer (`Sponsee`).
    
2. Sponsee Provisioning 
 - As mentioned https://github.com/evernym/mobile-sdk/blob/master/2.%20Initialization.md#mobile-sdk---3rd-party-apps-sponsee, a customer (`Sponsee`) will need to provision with Evernym's Cloud Service using a token signed by you (`Sponsor`).
 - After provisioning is complete, the customer (`Sponsee`) needs to register a communication preference (`update_com_method`). \
   The 'Sponsee' will provide 3 values `id`, `type`, and `value`. \
      `id`: is whatever the `Sponsor` uses to identify this specific `Sponsee`. \
      `type`: Int - Com Method Type (ALWAYS `3` for forwarding), \
      `value`: String 
      - When Evernym's Cloud Service receives messages for this entity (through provisioned cloud agent), the cloud agent will forward the message to your (`Sponsor's`) registered endpoint. You `Sponsor` will then deliver the message to the customer 'Sponsee' with whatever mechanism you already uses to communicate with your customer.  
      - `value` will be the whatever information your (`Sponsor's`) back-end will need to deliver that message to the customer (`Sponsee`). (e.g. If a `Sponsor` is using a push notifying service to communicate with its 'Sponsee', this will be the identifier the service needs to push the message)
      
3. Evernym's Cloud Service forwarding messages to the you as the `Sponsor`
- An explanation for this can be found under the heading "3. Receiving Future Messages" at https://github.com/evernym/mobile-sdk/blob/master/2.%20Initialization.md#your-sponsor-onboarding-with-evernyms-cloud-scervice

