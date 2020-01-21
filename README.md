<a href="https://www.evernym.com/"><img src="https://pbs.twimg.com/profile_images/1022255393395929088/0eYH-Os__400x400.jpg" title="Evernym"></a>

# ConnectMe Mobile SDK

> Evernym's mobile sdk repo with links to aar and cocoapod and starter files

## Table of Contents

- [Setup and Configuration](#Setup-and-Configuration)
- [Initializing the Wallet](#Initializing-the-Wallet)
- [Connections](#connections)
- [Credentials](#credentials)
- [Proofs](#proofs)
- [Structured Messages](#Structured-Messages)
- [FAQ](#faq)

---

## Setup and Configuration ([IOS](#ios) [Android](#Android) )

> current repo of mobile starter kit <a href="https://github.com/evernym/mobile-starter " target="_blank">Mobile Starter Kit</a>.  Download before continuing. 
> <a href="https://drive.google.com/drive/folders/1-ySuVqU7q79jG2epoVJH4bFU1CqWVGnR?usp=sharing" target="_blank">
    current location</a>  of aar and coocapods

### IOS
- Create a new Xcode project (TODO)
    - In a terminal cd into the directory that contains the [project_name].xcodeproj file or the [project_name].xcworkspace file of your Xcode project. We will call this directory the project directory.
    - If you do not already have a Podfile in the project directory then run the command ‘pod init’(If you don't have Cocoapods, install it:  ‘sudo gem install cocoapods’ )

    - You should now have a Podfile in the project directory. We need to edit the Podfile and make sure it contains the following…
    > in podfile mobile-starter-master/ios/CMeSdkObjc/Podfile
    ```Ruby
        # Uncomment the next line to define a global platform for your project
        # platform :ios, '9.0'
        source 'https://github.com/CocoaPods/Specs.git'
        source 'https://github.com/evernym/indy-sdk.git'

        def vcx_version_for_debug_or_release
            if ENV['CONNECTME_DEBUG'] == "true"
                '0.0.102'
            else
                '0.0.101'
            end
        end
        We also need to make sure the Podfile contains the following for your project: pod 'vcx', vcx_version_for_debug_or_release
        target '[ProjectName]' do
            # Uncomment the next line if you're using Swift or would like to use dynamic frameworks
            #  Uncomment this line ONLY if you are using Swift
            # use_frameworks!

            # Pods for [ProjectName]
            pod 'vcx', vcx_version_for_debug_or_release
            # FIREBASE NOT SETUP NOW
            # pod 'Firebase/Analytics'
            # pod 'Firebase/Messaging'

            target '[ProjectName]Tests' do
                inherit! :search_paths
                # Pods for testing
            end

            target '[ProjectName]UITests' do
                inherit! :search_paths
                # Pods for testing
            end

        end
    ```

- Run `$ pod repo update`
- Since cocoapod is pointing to Evernym’s private repository. Developers outside Evernym network won’t have access to it. For developers outside Evernym network, one way we can follow is:
    - Get cocoapod zip file from someone inside Evernym
    - Go to ~/.cocoapods/repos/
    - Go to evernym repo directory
    - Inside evernym repo directory, go to, “Specs/vcx/0.0.102” (Do this as well to Specs/vcx/0.0.101)
      Open vcx.podspec in a text editor
    - Change s.source to point to your own http server instead of Evernym’s private repo
    - You can run your simple http server by installing an npm module http-server
        - npm i -g http-server
        - cd \<directory-of-cocoapod-zip>
            - http-server -p 1990
    - Now command “pod install” command should run successfully
- Now you can run the following command in a terminal after you cd to the project directory which contains the Podfile you just modified: `$ pod install` or `$ CONNECTME_DEBUG=true pod install`
	> `Pod install` will hit 0.0.101, `CONNECTME_DEBUG=true pod install` will hit 0.0.102
- Now make sure that the file [project_name].xcworkspace exists in the project directory and ONLY use that file to open your project and edit your source code in Xcode.   Do NOT use the [project_name].xcodeproj file any longer BUT don’t delete it either.   
- If you are using Swift then you must create an Objective-C bridging header file named [project_name]-Bridging-Header.h in the same directory as your AppDelegate.swift file and add it to your Xcode project. The easiest way to do this is to use the File -> New -> File… menu item of the Xcode IDE. In the wizard that launches make sure to select iOS and then ‘Header File’ and then click the ‘Next’ button. In the ‘Save As:’ text field enter [project_name]-Bridging-Header.h and select the [project_name] ‘Group’ and select the [project_name] ‘Targets’ and click the Create button. Now the file [project_name]-Bridging-Header.h should be listed in the Xcode IDE at the same level as the AppDelegate.swift file. Edit the [project_name]-Bridging-Header.h file in the Xcode IDE and make sure it has at least the following… 
    > in mobile-starter-master/ios/CMeSdkSwift/CMeSdkSwift/CMeSdkSwift-Bridging-Header.h
    ```c
    #ifndef [ProjectName]_Bridging_Header_h
    #define [ProjectName]_Bridging_Header_h

    #import "vcx/vcx.h"

    #endif /* [ProjectName]_Bridging_Header_h */
    ```
- If you are using Objective-C then edit your AppDelegate.h file and add the following to the imports section of the file…
    > in mobile-starter-master/ios/CMeSdkObjc/CMeSdkObjc/AppDelegate.h
    ```c
    #import "vcx/vcx.h"
    ```

#### Initialize self.sdkApi and self.sdkInited  
-  ‘self.sdkApi’ and ‘self.sdkInited’. These Two AppDelegate data members need to be declared in the AppDelegate.h file and then initialized in the AppDelegate.m file.     Here is the declaration for the AppDelegate.h file.
    ```c
        @property (strong, nonatomic) ConnectMeVcx *sdkApi;
        @property (nonatomic) BOOL sdkInited;
    ```
- Here is a synthesize statement to add to the AppDelegate.m file and the constructor or initer to be placed in the AppDelegate.m file. 
    ```c 

        @synthesize sdkInited;

        //
        // further down in AppDelegate.m
        // ...
        - (id)init
        {
        self = [super init];
        if (self) {
            // Initialization code here.
            self.sdkApi = [[ConnectMeVcx alloc] init];
            self.sdkInited = false;
        }

        return self;
        }
    ```        

  
- Now you should try to build your project in Xcode to make sure the changes you have made so far are correct. I would also recommend that you launch your mobile app in   a simulator to make sure that the linking and deploying steps work as well.  
- You are now ready to start adding calls to your code to [initialize the wallet](#Initializing-the-Wallet). 

### Android 

- You should have an existing Android Studio project or create a new Android Studio project using the Android Studio new project wizard. Instructions are available        <a href=' https://developer.android.com/training/basics/firstapp/creating-project'>here</a>
- During the setup of the project you can choose either Java or Kotlin as the language for your new project.
- Open the Project level build.gradle file that contains the declaration of the repositories and add the following to the allprojects repositories section (not the         buildscript repositories section).
    
    ```java
        maven {
            url 'https://evernym.mycloudrepo.io/public/repositories/libvcx-android'
        }
    
- Now open the app level build.gradle file and add the following to the dependencies section.
    ```java
        implementation 'com.evernym:vcx:0.2.43551129-6fc40e0@aar'
    ```

- Also, in the app level build.gradle file change minSdkVersion to be at least 23 rather than any smaller version number.
- Now you should try to build your project in Android Studio to make sure the changes you have made so far are correct. I would also recommend that you launch your         mobile app in an emulator to make sure that the bundling of the apk is working.

- TODO: Initialize self.sdkApi and self.sdkInited for android?...  mobile-starter-master/android/CMeSdkJava/app/src/main/java/me/connect/sdk/java/MainActivity.java



---

## Initializing the Wallet

Almost all of the iOS APIs provided by the ConnectMe iOS SDK are asynchronous. This means that a completion callback function is required to get the results of the function invocations. Here are the steps to initialize the wallet. Most of these wallet initialization steps are done in the AppDelegate’s didFinishLaunchingWithOptions lifecycle method.

> ios: refer to  mobile-starter-master/ios/CMeSdkObjc/CMeSdkObjc/AppDelegate.m
> android: refer to  init() in mobile-starter-master/android/CMeSdkJava/app/src/main/java/me/connect/sdk/java/ConnectMeVcx.java

```
    // ConnectMe/android/app/src/main/java/me/connect/rnindy/RNIndyModule.java
    @ReactMethod
    public void createOneTimeInfo(String agencyConfig, Promise promise) 
        // calls UtilsApi.vcxAgentProvisionAsync(agencyConfig)
    
    // ConnectMe/ios/RNIndy.m
    RCT_EXPORT_METHOD(createOneTimeInfo: (NSString *)config
                           resolver: (RCTPromiseResolveBlock) resolve
                           rejecter: (RCTPromiseRejectBlock) reject)   
        //   calls [[[ConnectMeVcx alloc] init] agentProvisionAsync:config completion:^(NSError *error, NSString *oneTimeInfo) {  
```                            

1. initialize the VcxLogger to use the default logger or to use a custom logger.
2. Save the pool transactions genesis configuration JSON to a file. The contents of the *poolTxnGenesis NSString variable will be very similar to the contents here: https://raw.githubusercontent.com/sovrin-foundation/sovrin/stable/sovrin/pool_transactions_sandbox_genesis. The path to the pool transactions genesis file is used in the function call to initialize the SDK.
3. Invoke the payment initialization function (for now this is nullPay). Notice that this call is not asynchronous.
4. Invoke the agentProvisionAsync function to get the oneTimeInfo configuration
5. Get the vcxConfig JSON from the keychain keystore if the oneTimeInfo JSON is nil but if the oneTimeInfo JSON is not nil then create the vcxConfig JSON using the oneTimeInfo JSON.
6. Finally, we can initialize the VCX native library so that we can now use the SDK APIs that give us access to the wallet. Please notice that we are using the [NSUserDefaults standardUserDefaults]; as the temporary storage mechanism in this Objective C code but you should use a more robust storage and retrieval mechanism.
7. Now you should make sure that your mobile app still builds and links and deploys onto a real mobile device and run some tests to ensure everything works up to this point. Please let us know if the initializing of the wallet does not work after you have followed these steps. If you had to do extra steps to get it working then please also let us know what you had to do so that we can update this document.

## Selecting the Ledger and Agency

#### Ledger

If you want to use a different ledger then you MUST change to the corresponding Agency as described in The Agency section below and you need to change the contents of the poolTxnGenesis variable in the above code. Evernym does have several different active ledger instances that we use for different scenarios: development, sandbox, staging, demo, qatest1, qatest2, devrc, qarc, devteam1, devteam2, devteam2, and prod. An older configuration for some of these ledgers can be seen <a href="https://github.com/sovrin-foundation/connector-app/blob/master/app/store/config-store.js">here</a> and you can search for the word ‘server_environment.’ to see some of these and then use the ‘poolConfig:’ setting in the particular environment to set the poolTxnGenesis variable to the value of the ‘poolConfig:’ setting. If you really want to use a different ledger other than Demo, which is what the above code uses, then please let us know.

#### Agency
If you choose to use a different Ledger then you MUST change to the corresponding Agency and vice versa. To use a different Agency you need to change the contents of the agencyConfig variable in the above code. For every different ledger configuration there is a corresponding agency configuration. These configuration settings can be seen in the same config-store.js code that was linked to in the Ledger section above. You will need to change the agency_did, agency_url, and agency_verkey in the code above in order to use a different agency for the corresponding ledger.

> TODO:

```
 WARN|vcx::utils::libindy::wallet   |        src/utils/libindy/wallet.rs:33  | could not create wallet wallet_name3: "Error: Wallet with this name already exists\n  Caused by: Wallet database file already exists: \"/private/var/mobile/Containers/Data/Application/B9883A8C-47F3-44D8-BF63-347FD2A338F1/Documents/.indy_client/wallet/wallet_name3/sqlite.db\"\n"
```

 NOTE about had issues were here where NSString *walletName = @"wallet_name"; had to be changed and NSString *fileName = @"pool_transactions_genesis_DEMO";

---
## Connections

Currently, Connect.Me can only respond to a connection invitation which has been generated by the inviter. It can respond to connection invites in two ways: Scanning a properly formatted QR code or deeplinking. To accept a connection invitation and form a new connection, Connect.Me generates a new unique DID & associated key pair. The public key is then included in the response to the inviter. This results in both parties having recorded the others’ public keys, essentially forming a unique pairwise encryption channel. Connect.Me can form an unlimited number of connections this way.

### Scanning a QR Code

1.  **connectionCreateWithInvite(configValues, inviteDetails)**

    _PARAMS_
    ```
        configValues: invitationId //NOT SURE WHAT THIS IS But think it is the id ‘attr’ in  in connConfig
        inviteDetails: pass invite details from scanning
    ```
	_RETURNS_
    ```
        connectionHandle
    ```    

2. **connectionConnect(connectionHandle, connectionType)** 

	_PARAMS_
    ```
        connectionHandle: from connectionCreateWithInvite
        connectionType: "{\"connection_type\":\"QR\",\"phone\":\"\"}"
    ```
	_RETURNS_
    ```
	    inviteDetails // Don’t seem to do anything with this
    ```    

3. **connectionSerialize(connectionHandle)**

	_PARAMS_
    ```
        connectionHandle: from connectionCreateWithInvite
    ```
	_RETURNS_
    ```
	    state // state of connection
    ```   

### Examples
#### ios
> from addNewConn in mobile-starter-master/ios/CMeSdkObjc/CMeSdkObjc/ViewController.m
```javascript
    - (IBAction)addNewConn:(id)sender {
        AppDelegate *appDelegate = (AppDelegate*)[[UIApplication sharedApplication] delegate];


        NSUserDefaults *standardUserDefaults = [NSUserDefaults standardUserDefaults];

        NSString *connConfig = self.addConnConfig.text;

        NSLog(@"Connection Config: %@", connConfig);
        NSError* error;
        NSMutableDictionary *configValues = [NSJSONSerialization JSONObjectWithData:[connConfig dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error:&error];

        [appDelegate.sdkApi connectionCreateWithInvite:[configValues valueForKey:@"id"] inviteDetails:connConfig
            completion:^(NSError *error, NSInteger connectionHandle) {
                if (error != nil && error.code != 0)
                {
                    NSString *indyErrorCode = [NSString stringWithFormat:@"Error occurred while creating connection: %@ :: %ld", error.domain, (long)error.code];
                    NSLog(@"4) Value of indyErrorCode is: %@", indyErrorCode);
                } else {
                NSLog(@"[4] createConnectionWithInvite was successful: %ld", connectionHandle);

                    // connectionConnect with connectionHandle
                    [appDelegate.sdkApi connectionConnect:connectionHandle
                        connectionType:@"{\"connection_type\":\"QR\",\"phone\":\"\"}"
                    completion:^(NSError *error, NSString *inviteDetails) {

                        if (error != nil && error.code != 0)
                        {
                            NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
                            NSLog(@"5) Value of indyErrorCode is: %@", indyErrorCode);
                        } else {
                            NSLog(@"[5] connectionConnect was successful with inviteDetails: %@", inviteDetails);

                            [appDelegate.sdkApi connectionSerialize:connectionHandle
                                    completion:^(NSError *error, NSString *state) {
                                        if (error != nil && error.code != 0)
                                        {
                                            NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
                                            NSLog(@"6) Value of indyErrorCode is: %@", indyErrorCode);
                                        } else {
                                        NSLog(@"[6] connectionSerialize was successful with state: %@", state);
                                        // Store the serialized connection
                                        if (standardUserDefaults) {
                                            [standardUserDefaults setObject:state forKey:@"serializedConnection"];
                                            [standardUserDefaults synchronize];
                                        }
                                        }
                                    }];
                        }
                    }];

                }
            }];

    }
```

- However your instution 

#### java

> from addConnectionOnClick in mobile-starter-master/android/CMeSdkJava/app/src/main/java/me/connect/sdk/java/ConnectMeVcx.java
```java
    public void addConnectionOnClick(View v) {
        EditText editText   = (EditText)findViewById(R.id.editText2);
        String invitationDetails = editText.getText().toString();
        Log.d(TAG, "connection invitation is set to: " + invitationDetails);

        try {
            JSONObject json = new JSONObject(invitationDetails);
            sdkApi.createConnectionWithInvite(json.getString("id"), invitationDetails, new CompletableFuturePromise<>(connectionHandle -> {
                Log.e(TAG, "createConnectionWithInvite return code is: " + connectionHandle);
                if(connectionHandle != -1) {
                    sdkApi.vcxAcceptInvitation(connectionHandle, "{\"connection_type\":\"QR\",\"phone\":\"\"}", new CompletableFuturePromise<>(inviteDetails -> {
                        Log.e(TAG, "vcxAcceptInvitation return code is: " + inviteDetails);
                        if(invitationDetails != null) {
                            sdkApi.getSerializedConnection(connectionHandle, new CompletableFuturePromise<>(state -> {
                                Log.e(TAG, "getSerializedConnection returned state is: " + state);
                            }, (t) -> {
                                Log.e(TAG, "getSerializedConnection error is: ", t);
                                return null;
                            }));
                        }
                    }, (t) -> {
                        Log.e(TAG, "vcxAcceptInvitation error is: ", t);
                        return null;
                    }));
                }
            }, (t) -> {
                Log.e(TAG, "createConnectionWithInvite error is: ", t);
                return -1;
            }));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
```
- For all the possible languages that support syntax highlithing on GitHub (which is basically all of them), refer <a href="https://github.com/github/linguist/blob/master/lib/linguist/languages.yml" target="_blank">here</a>.

---

## Credentials
- Credentials

## Proofs

  1. call `downloadMessages('MS-103', null, pwDID)` and filter objects having type `proofReq`.  From there use `decryptedPayload` and the `uid` from the object 

  ```javascript
        const MESSAGE_PENDING = 'MS-103';
        // Get messages will have type 'profReq'
        const downloadMessages = await RNIndy.downloadMessages(
          MESSAGE_PENDING,
          null,
          this.props.connection.pwDid,
        );
     
        const msgObject = JSON.parse(downloadMessages)[0]

        //
        const proofReqMsg = msgObject.msgs.find(msg => msg.type === 'proofReq')
        const parsedPayload = proofReqMsg.decryptedPayload
        
        // Step 1 convert decrypted payload from downloadMessages
        const proofRequest = this.convertDecryptedPayloadToSerializedProofRequest(parsedPayload, proofReqMsg.uid )
        // console.log('proofRequest',proofRequest)
        
        // Step 2 get proofHandle
        const proofHandle = await RNIndy.proofDeserialize(proofRequest)
        
        // Step 3 get matchingCredentialsJson
        const matchingCredentialsJson = await RNIndy.proofRetrieveCredentials(proofHandle)
        //{"attrs":{"Account Type":[],"Driver Card":[],"First Name":[]}}

        // Step 4 SKIPPING FOR NOW... if allowing self attested attributes get them from user.  

        // Step 5
       await RNIndy.proofGenerate(proofHandle, JSON.stringify(matchingCredentialsJson), '{}' )

        // Step 6
        const connectionHandle = this.props.connection.connectionHandle
        await RNIndy.proofSend(proofHandle, parseInt(connectionHandle))
    ```
    
    ```javascript
        convertDecryptedPayloadToSerializedProofRequest = (decryptedPayload,uid ) => {
        let stringifiableProofRequest = {
          data: {
            agent_did: null,
            agent_vk: null,
            link_secret_alias: 'main',
            my_did: null,
            my_vk: null,
            proof: null,
            proof_request: null,
            source_id: uid,
            state: 3,
            their_did: null,
            their_vk: null,
          },
          version: '1.0',
        }
        const parsedPayload = JSON.parse(decryptedPayload)
        const parsedMsg = JSON.parse(parsedPayload['@msg'])
        const parsedType = parsedPayload['@type']
        stringifiableProofRequest.data.proof_request = {
          ...parsedMsg,
          msg_ref_id: uid,
        }
        stringifiableProofRequest.version = parsedType.ver
        return JSON.stringify(stringifiableProofRequest)
      }
    ``` 

## Structured Messages 

1. Call connectionSignData(connectionHandle, data, base64EncodingOption, encodeBeforeSigning )

	PARAMS:
	```
        connectionHandle: from connection sending question
        data: answer.nonce, would get this when you users selects answer from answers from your secureMessage
	    base64EncodingOption: defaults to 'NO_WRAP' in connect.me another option of "URL_SAFE”. 
	    encodeBeforeSigning: defaults to true in connect.me
    ```
    RETURNS
    ```    
        {data, signature}  to be used in connectionSendMessage
    ```


2. Call connectionSendMessage(connectionHandle, withMessage )
	PARAMS
	```
    connectionHandle: from connection sending question
    withMessage: JSON.stringify({
             '@type': ‘did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/committedanswer/1.0/answer',
 			 'response.@sig': {
   			 signature: signature, // signature from connectionSignData
  			 sig_data: data, //data from connectionSignData
   			 timestamp: moment().format(),
  			}
 		 })
	endMessageOptions: JSON.stringify({
            msg_type: ‘Answer’,
          	msg_title: ‘Peer Sent Answer’,
         	ref_msg_id: uid || null, // get uid from question.payload.uid (from secure message)
 		})
    ```

```javascript
    // javascript example
     async getMessages(pwDID) {
    
    }

```

---


## FAQ

- **How do I do *specifically* so and so?**
    - No problem! Just do this.

### Helpful links
- <a href="https://github.com/evernym/mobile-starter " target="_blank">Mobile Starter Kit</a> current repo of mobile starter kit
- <a href="https://drive.google.com/drive/folders/1-ySuVqU7q79jG2epoVJH4bFU1CqWVGnR?usp=sharing" target="_blank">
    Starter files</a> current location of aar and coocapods
- <a href=" https://docs.google.com/document/d/1HAa27qArYlU0NO1VbEjA8ANXmVHl-b7fxa40e21I5L8/edit" target="_blank">
    Old SDK docs</a>
- <a href=" https://docs.google.com/document/d/1HAa27qArYlU0NO1VbEjA8ANXmVHl-b7fxa40e21I5L8/edit" target="_blank">
    Connector App</a> helpful resource to see how an older version od connect.me works
---




