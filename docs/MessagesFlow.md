# Messages flow

In the [Initialization document](./2.Initialization.md) we described how to obtain your Cloud Agent.
Cloud Agent can be considered as a mailbox which able to receive and collect messages for your application.
In this document, we will describe how to deal with messages on the mobile application side.

## Message downloading

There are two strategies regarding receiving messages by the application from its Cloud Agent:

* Polling - Application once in a while calls Cloud Agent to download all received messages for existing connections. 
    ![](/wiki-images/Polling.png)
    
* Push Notifications - Cloud Agent forwards messages to `Sponsor` which then notifies application. The application can download only message related to received push notification. 
    ![](/wiki-images/PushNotifications.png)

After that, received from the Cloud Agent messages should be handled depending on the type and updated on the Cloud Agent as reviewed.

## 1.1 Getting all pending messages

messageStatus - Using `MS-103` message status you can receive the only pending messages.

### iOS
```objC
[appDelegate.sdkApi downloadMessages:@"MS-103"
        uid_s:nil
        pwdids:nil
        completion:^(NSError *error, NSString* messages) {
            // ...
        }];
```

## 1.1 Getting a specific message

messageStatus - Using `MS-103` message status you can receive the only pending messages.
uid_s - Identifier of the message received from a push notification (`uid` field from a push notification)
pwdids - Identifier of connection to which the message relates to (`forDID` field from a push notification)

### iOS
```objC
[appDelegate.sdkApi downloadMessages:@"MS-103"
        uid_s:@"message uuid"
        pwdids:@"connectionDid"
        completion:^(NSError *error, NSString* messages) {
            // ...
        }];
```

## 2. Parsing result

Received `messages` string is an JSON array string containing the list of received messages for each particular connection:
* `pairwiseDID` - DID of a connection to which messages relate.
* `msgs` - the list of messages related to connection.

For each entry of `msgs` array, following fields should be noted:

* `uid` - UID of message, required to perform operations with message
* `type` - type of message. This field could contain different values depending on protocol used (See `me.connect.sdk.java.message.MessageType` for reference)
* `decryptedPayload` - message payload. It has different contents depending on message type and should be processed according to it. For reference see other parts of documentation.

Base message types (`type` field):

* Credential offer:
    * `credential-offer`
* Proof request:
    * `presentation-request`
* Structured message:
    * `question`
    * `committed-question`

### Sample of message

#### Pending proof request message
```json
[
    {
        "pairwiseDID": "JhpTz7etj4vXuY9YaFfKvt",
        "msgs": [
            {
                "statusCode": "MS-103",
                "payload": null,
                "senderDID": "4ShhXixK19DNK4h6ngyhvq",
                "uid": "461e7630-b1c3-47e4-a31c-9922a4f29e99",
                "type": "proofReq",
                "refMsgId": null,
                "deliveryDetails": [],
                "decryptedPayload": "{\"@type\":{\"name\":\"PROOF_REQUEST\",\"ver\":\"1.0\",\"fmt\":\"json\"},\"@msg\":\"{\\\"@topic\\\":{\\\"mid\\\":0,\\\"tid\\\":0},\\\"@type\\\":{\\\"name\\\":\\\"PROOF_REQUEST\\\",\\\"version\\\":\\\"1.0\\\"},\\\"from_timestamp\\\":null,\\\"msg_ref_id\\\":\\\"461e7630-b1c3-47e4-a31c-9922a4f29e99\\\",\\\"proof_request_data\\\":{\\\"name\\\":\\\"DEMO-Employment Proof\\\",\\\"non_revoked\\\":null,\\\"nonce\\\":\\\"818414048827045368111037\\\",\\\"requested_attributes\\\":{\\\"DEMO-Address Number\\\":{\\\"name\\\":\\\"DEMO-Address Number\\\"},\\\"DEMO-Apartment\\\":{\\\"name\\\":\\\"DEMO-Apartment\\\"},\\\"DEMO-Citizenship\\\":{\\\"name\\\":\\\"DEMO-Citizenship\\\"},\\\"DEMO-City\\\":{\\\"name\\\":\\\"DEMO-City\\\"},\\\"DEMO-Country of Residence\\\":{\\\"name\\\":\\\"DEMO-Country of Residence\\\"},\\\"DEMO-Date of Birth\\\":{\\\"name\\\":\\\"DEMO-Date of Birth\\\"},\\\"DEMO-Dual Citizenship\\\":{\\\"name\\\":\\\"DEMO-Dual Citizenship\\\"},\\\"DEMO-Email Address\\\":{\\\"name\\\":\\\"DEMO-Email Address\\\"},\\\"DEMO-First Name\\\":{\\\"name\\\":\\\"DEMO-First Name\\\"},\\\"DEMO-Home Phone\\\":{\\\"name\\\":\\\"DEMO-Home Phone\\\"},\\\"DEMO-Last Name\\\":{\\\"name\\\":\\\"DEMO-Last Name\\\"},\\\"DEMO-Middle Name\\\":{\\\"name\\\":\\\"DEMO-Middle Name\\\"},\\\"DEMO-State\\\":{\\\"name\\\":\\\"DEMO-State\\\"},\\\"DEMO-Street Name\\\":{\\\"name\\\":\\\"DEMO-Street Name\\\"},\\\"DEMO-Time at Current Address\\\":{\\\"name\\\":\\\"DEMO-Time at Current Address\\\"},\\\"DEMO-Zip\\\":{\\\"name\\\":\\\"DEMO-Zip\\\"}},\\\"requested_predicates\\\":{},\\\"ver\\\":null,\\\"version\\\":\\\"0.1\\\"},\\\"thread_id\\\":null,\\\"to_timestamp\\\":null}\"}"
            }
        ]
    }
]
```

## 3. Process parsed message 

Iterate over received messages and apply them either to existing state objects or create a new one.  
1. The first loop goes over objects containing `pairwiseDID/msgs` pairs.
2. The nested loop goes over entries from `msgs` field.

## 4. Update message status as reviewed

After processing received messages you need to update their status on the Cloud Agent to avoid their repeatedly receiving and handling during the next downloads.

Parameters:
* `messageStatus` - desired message type, e.g. `"MS-106"` (reviewed)
* `handledMessage` - JSON string with following structure:
    ```json
    [
        {
            "pairwiseDID" : "pwDID", // DID of connection
             "uids": ["uid"] // list of UID's of processed messages related to the connection
        },
        ...
    ]
    ```

  ### iOS
  ```objC
  [appDelegate.sdkApi updateMessages:messageStatus
          pwdidsJson:handledMessage
          completion:^(NSError *error) {
              // ...
          }];
  ```
  
  ### Android
  ```java
  UtilsApi.vcxUpdateMessages(messageStatus, handledMessage).get()
  ```
