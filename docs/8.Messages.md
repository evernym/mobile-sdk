# Messages

libVcx provides a way to receive messages for specific connection. It can be used to receive incoming proof requests, credential offers and structured messages.

To get messages, following steps should be performed:

1. Deserialize connection
2. Get connection pwDid
3. Get messages
4. Parse result

> **NOTE:** library should be initialized before using messages API. See [initialization documentation](2.Initialization.md)

## 1. Deserialize connection

Retrieve connection handle using stored serialzied connection.

### iOS
```objC
[appDelegate.sdkApi connectionDeserialize:serializedConnection
        completion:^(NSError *error, NSInteger connectionHandle) {
            // ...
        }];
```

### Android
```java
int connectionHandle = ConnectionApi.connectionDeserialize(serializedConnection).get();
```

## 2. Get connection pwDid

Using retrieved connection handle get `pwDid` of this connection:

### iOS
<!--TODO add obj-c sample-->

### Android
```java
String pwDid = ConnectionApi.connectionGetPwDid(handle).get()
```

## 3. Get messages

Using `pwDid` you can retrieve messages like following:

### iOS
```objC
[appDelegate.sdkApi downloadMessages:messageStatus
        uid_s:nil
        pwdids:pwDid
        completion:^(NSError *error, NSString* messages) {
            // ...
        }];
```

### Android
```java
String messages = UtilsApi.vcxGetMessages(messageStatusType, null, pwDid).get();
```

Avaliable message status types:
* `MS-101` - created
* `MS-102` - sent
* `MS-103` - pending
* `MS-104` - accepted
* `MS-105` - rejected
* `MS-106` - answered

In most cases, you will need to receive pending messages.


## 4. Parse result

Received `messages` string is an JSON array string containing retrieved messages.
Each entry has `msgs` array, containig message.
For each entry of this array, following fields should be noted:

* `uid` - UID of message, required to perform operations with message
* `type` - type of message. This field could contain different values depending on protocol used (See `me.connect.sdk.java.message.MessageType` for reference)
* `decryptedPayload` - message payload. It has different contents depending on message type and should be processed according to it. For reference see other parts of documentation.

Avaialble messasge types:

* Credential offer:
    * `credOffer`
    * `CRED_OFFER`
    * `credential-offer`
* Proof request:
    * `proofReq`
    * `PROOF_REQUEST`
    * `presentation-request`
* Structured message:
    * `Question`
    * `QUESTION`
    * `question`
    * `committed-question`


## Message updates

In certain cases you will need to update message status.

### iOS
```objC
[appDelegate.sdkApi updateMessages:messageStatus
        pwdidsJson:pwdidsJson
        completion:^(NSError *error) {
            // ...
        }];
```

### Android
```java
UtilsApi.vcxUpdateMessages(messageType, answerMessage).get()
```

* `messageType` - desired message type, e.g. `"MS-106"` (answered)
* `answerMessage` - JSON string with following structure:
    ```json
    [
        {
            "pairwiseDID" : "pwDID", // pwDid of connection
             "uids": ["uid"] // UID of message to be updated
        }
    ]
    ```

## Sample of message

### Pending proof request message
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
