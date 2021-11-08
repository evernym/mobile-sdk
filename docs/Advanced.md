# Advanced Techniques

This document contains explanations for additional flows that can be implemented and optimizations can be done using Mobile SDK API functions.

* [Postponed Ledger connectivity](#postponed-ledger-connectivity)
* [Holder Present Proof](#holder-present-proof) 
* [Preparation of Connection Agent in advance](#preparation-of-connection-agent-in-advance)

## Postponed Ledger connectivity

As the first step of working with MDK (assume you app is already completed provisioning) you have to call a function to initialize it with prepared config (Java `VcxApi.vcxInitWithConfig(config)` and Objective-C `[[[ConnectMeVcx alloc] init] initWithConfig:config`).
Internally this function does three things:
1. Sets settings passed within the config JSON
2. Open secured Wallet
3. Connects to a Pool Ledger Network

Connection to a Pool Ledger Network may take several seconds that significantly slow down library initialization.
In fact, Pool Ledger connectivity required only when you receive credentials and prove them for the first time ( if you don't use [Cache data for Proof generation](6.Credentials.md#cache-data-for-proof-generation---optional) technique).
Your application can make connections, answer questions and prove credentials without establishing a connection with Pool Ledger Network.

SDK allows you to skip Pool Ledger Network connectivity on the Library initialization step and do it later by demand or right after initialization as a background task.

#### Steps

1. Do not set `path` field in the config JSON passing into `vcxAgentProvisionWithToken` function. Remove `genesis_path` filed from the resulting config (it should contain `<CHANGE_ME>` value).

1. Init SDK usual way by calling `initWithConfig` function.

1. Later, call function (for instance as a background task) to perform a connection to the Pool Ledger.

   > **NOTE:** The recommended way is to call it in a separate thread as it may take several seconds.

    ```
    config = {
        "genesis_path": string,
        "pool_name": string, // optional
    }
    ```

   Java pseudocode:
    ```
        VcxApi.vcxInitPool(config)
    ```

   Objective-C pseudocode
    ```
        [[[ConnectMeVcx alloc] init] initPool:config...]
    
    ```

### Holder Present Proof

In this scenario, we assume that the credential Holder (mobile app) wants to share/prove his credential to another side which can be a mobile app as well.

#### Steps overview (without establishing a Connection between sides )

1. Holder prepares `Out-of-Band Connection Invitation` containing `Presentation Proposal` attachment:\
   1.1. Deserialize credential state object corresponding to the credential needs to be shared.\
   1.2. Prepare `Presentation Proposal` message.\
   1.3. Create Out-of-Band Connection state object and Pairwise Cloud Agent. In the steps below we are passing `handshake:false` to skip regular connection establishing process. It allows us to share proof faster as we skip exchange of `Connection Request/Connection Response` messages.\
   1.4. Receive `Connection Invitation`.\
   1.5. Serialize and store Connection state object.\
   1.6. Share the `Connection Invitation` with a Verifier somehow.
2. Verifier generates and sends `Presentation Request`:\
   1.1. Create Connection state object using received `Out-of-Band Connection Invitation`.\
   1.2. Accept `Out-of-Band Connection Invitation`.\
   1.3. Extract `Presentation Proposal` from the invitation attachment.\
   1.4. Create `Proof` state object using `Presentation Proposal` message.\
   1.5. Send `Presentation Request` message to Holder.\
   1.6. Serialize and store Connection state object.\
   1.7. Get `Presentation Request` message from Proof state machine.\
   1.8. Serialize and store Proof state object.\
3. Holder generates and sends `Presentation`:\
   1.1. Download `Presentation Request` message from the Cloud Agent.\
   1.2. Create `Proof` state object using received `Presentation Request` message.\
   1.3. Deserialize Connection state object.\
   1.4. Generate and send `Proof` message.\
   1.5. Update `Presentation Request` message status on the Cloud Agent as read.
2. Verifier verifies `Presentation`:\
   1.1. Download `Proof` message from the Cloud Agent.\
   1.2. Deserialize Proof state object.\
   1.3. Update Proof state object with received `Proof` message.\
   1.4. Get result of proof verification.

#### Steps

1. Holder App generates Presentation Proposal and puts it into Aries Out-Of-Band invitation (without handshake):

    Java pseudocode:
    ```
        // 1.1. Deserialize credential state object corresponding to the credential needs to be shared
        Integer credentialHandle = CredentialApi.credentialDeserialize(serializedCredentialStateObject)
        
        // 1.2. Prepare `Presentation Proposal` message.
        String presentationProposal = CredentialApi.credentialGetPresentationProposal(credentialHandle)
   
        // 1.3. Create Out-of-Band Connection state object and Pairwise Cloud Agent.
        // `false` means skipping regular connection establishing process.
        Integer connectionHandle = ConnectionApi.vcxConnectionCreateOutofband('sourceId', 'present-credential', 'Present Credential', false, presentationProposal)
        ConnectionApi.vcxConnectionConnect(connectionHandle, '{}')
   
        // 1.4. Receive `Connection Invitation`.
        String invitation = ConnectionApi.getConnectionInvite(connectionHandle, false)
   
        // 1.5. Serialize and store Connection state object.
        String connectionSerialized = Connection.Api.connectionSerialize(connectionHandle)
   
        // 1.6. Share the `Connection Invitation` with a Verifier somehow.
    ```
    
    Objective-C pseudocode
    ```
        // 1.1. Deserialize credential state object corresponding to the credential needs to be shared
        NSInteger credentialHandle = [[[ConnectMeVcx alloc] init] credentialDeserialize:serializedCredential...]

        // 1.2. Prepare `Presentation Proposal` message.
        NSString *presentationProposal = [[[ConnectMeVcx alloc] init] credentialGetPresentationProposal:credential_handle...]
        
        // 1.3. Create Out-of-Band Connection state object and Pairwise Cloud Agent.
        // `false` means skipping regular connection establishing process.
        NSInteger connectionHandle = [[[ConnectMeVcx alloc] init] connectionCreateOutofband:sourceId
                                                                                   goalCode:goalCode
                                                                                       goal:goal
                                                                                  handshake:FALSE
                                                                              requestAttach:requestAttach...]
        [[[ConnectMeVcx alloc] init] connectionConnect:connectionHandle
                                                    connectionType:@"{}"...]

        // 1.4. Receive `Connection Invitation`.
        NSString *invitation = [[[ConnectMeVcx alloc] init] getConnectionInviteDetails:connectionHandle
                                                                                   abbreviated:FALSE...]
        
        // 1.5. Serialize and store Connection state object.
        NSString *connectionSerialized = [[[ConnectMeVcx alloc] init] connectionSerialize:connectionHandle...]
   
        // 1.6. Share the `Connection Invitation` with a Verifier somehow.
    ```

2. Verifier App accept Out-Of-Band invitation, create Verifier state object and send Proof Request. App need to extract Presentation Proposal message from invitation `request~attach` field.
    Java pseudocode:
    ```
        // 1.1. Create Connection state object using received `Out-of-Band Connection Invitation`.
        Integer connectionHandle = ConnectionApi.createConnectionWithOutOfBandInvite('sourceId', invitation)
   
        // 1.2. Accept `Out-of-Band Connection Invitation`.
        ConnectionApi.vcxConnectionConnect(connectionHandle, '{}')
   
        // 1.3. Extract `Presentation Proposal` from the invitation attachment.
        String presentationProposal = String presentationProposal = base64decode(invitation['request~attach'][0]['data']['base64'])
   
        // 1.4. Create `Proof` state object using `Presentation Proposal` message.
        Integer proofHandle = ProofApi.proofCreateWithProposal('sourceId', presentationProposal, 'name')
        
        // 1.5. Send `Presentation Request` message to Holder.
        ProofApi.proofSendRequest(proofHandle, connectionHandle)
   
        // 1.6. Serialize and store Connection state object.
        String connectionSerialized = Connection.Api.connectionSerialize(connectionHandle)
         
        // 1.7. Get `Presentation Request` message from Proof state machine.
        String proofRequest = ProofApi.proofGetRequestMsg(proofHandle)
   
        // 1.8. Serialize and store Proof state object.
        String serializedProof = ProofApi.proofSerialize(proofHandle)
    ```
    
    Objective-C pseudocode
    ```
        // 1.1. Create Connection state object using received `Out-of-Band Connection Invitation`.
        NSInteger connectionHandle = [[[ConnectMeVcx alloc] init] connectionCreateWithOutofbandInvite:@"sourceId"
                                                                                                 invite:invitation...]
        
        // 1.2. Accept `Out-of-Band Connection Invitation`.
        [[[ConnectMeVcx alloc] init] connectionConnect:connectionHandle
                                                    connectionType:@"{}"...]
   
        // 1.3. Extract `Presentation Proposal` from the invitation attachment.
        String presentationProposal = String presentationProposal = base64decode(invitation['request~attach'][0]['data']['base64'])
   
        // 1.4. Create `Proof` state object using `Presentation Proposal` message.
        NSInteger proofHandle = [[[ConnectMeVcx alloc] init] createProofVerifierWithProposal:@"sourceId"
                                                                        presentationProposal:presentationProposal
                                                                                        name:@"name"...]
        
        // 1.5. Send `Presentation Request` message to Holder.
        [[[ConnectMeVcx alloc] init] proofVerifierSendRequest:proofHandle
                                               connectionHandle:connectionHandle...]
   
        // 1.6. Serialize and store Connection state object.
        NSString *connectionSerialized = [[[ConnectMeVcx alloc] init] connectionSerialize:connectionHandle...]
   
        // 1.7. Get `Presentation Request` message from Proof state machine.
        NSString *proofRequest = [[[ConnectMeVcx alloc] init] proofVerifierGetProofRequestMessage:proofHandle...]
        
        // 1.8. Serialize and store Proof state object.
        NSString *serializedProof = [[[ConnectMeVcx alloc] init] proofVerifierSerialize:proofHandle...]
    ```

3. Holder App accept Presentation Request, generates and sends Proof.

   Java pseudocode:
    ```
        // 1.1. Download `Presentation Request` message from the Cloud Agent
        String proofRequest = getAllPendingMessages('Proof Request')
   
        // 1.2. Create `Proof` state object using received `Presentation Request` message.
        Integer proofHandle = DisclosedProofApi.proofCreateWithRequest('sourceId', proofRequest)
         
        // 1.3. Deserialize Connection state object.
        Integer connectionHandle = ConnectionApi.connectionDeserialize(serializedConnection)

        // 1.4. Generate and send `Proof` message.
        String credentials = DisclosedProofApi.proofRetrieveCredentials(proofHandle)
        // select credentials to use
        DisclosedProofApi.proofGenerate(proofHandle, selectedCredentials, selfAttestedAttributes)
        DisclosedProofApi.proofSend(proofHandle, connectionHandle)
   
        // 1.5. Update `Presentation Request` message status on the Cloud Agent as read.
        UtilsApi.vcxUpdateMessages(messageStatus, handledMessage).get()  
    ```
    
    Objective-C pseudocode
    ```
        // 1.1. Download `Presentation Request` message from the Cloud Agent
        String proofRequest = getAllPendingMessages('Proof Request')
   
        // 1.2. Create `Proof` state object using received `Presentation Request` message.
        NSInteger proofHandle = [[[ConnectMeVcx alloc] init] proofCreateWithRequest:sourceId
                                                                     withProofRequest:proofRequest...]
   
        // 1.3. Deserialize Connection state object.
        NSInteger connectionHandle = [[[ConnectMeVcx alloc] init] connectionDeserialize:serializedConnection...]
   
        // 1.4. Generate and send `Proof` message.
        NSString *credentials = [[[ConnectMeVcx alloc] init] proofRetrieveCredentials:proofHandle...]
        // select credentials to use
        [[[ConnectMeVcx alloc] init] proofGenerate:proofHandle
                             withSelectedCredentials:selectedCredentials
                               withSelfAttestedAttrs:selfAttestedAttributes...]
        [[[ConnectMeVcx alloc] init] proofSend:proofHandle
                            withConnectionHandle:connectionHandle...]
   
        // 1.5. Update `Presentation Request` message status on the Cloud Agent as read.
        [appDelegate.sdkApi updateMessages:messageStatus
                               pwdidsJson:handledMessage
                               completion:^(NSError *error) {
                      // ...
                  }];
    ```
    
4. Verifier App handle received Proof message
   Java pseudocode:
    ```
        // 1.1. Download `Presentation` message from the Cloud Agent
        String proofRequest = getAllPendingMessages('Presentation')
   
        // 1.2. Deserialize Proof state object.
        Integer proofHandle = ProofApi.proofDeserialize(serialized)
   
        // 1.3. Update Proof state object with received `Proof` message.
        ProofApi.proofUpdateStateWithMessage(proofHandle, proof)
   
        // 1.4. Get result of proof verification.
        vcx.proof.GetProofResult result = ProofApi.getProofMsg(proofHandle)
    ```
    
    Objective-C pseudocode
    ```
        // 1.1. Download `Presentation Request` message from the Cloud Agent
        String proof = getAllPendingMessages('Proof Request')
   
        // 1.2. Deserialize Proof state object.
        NSInteger proofHandle = [[[ConnectMeVcx alloc] init] proofDeserialize:serializedProof...]
   
        // 1.3. Update Proof state object with received `Proof` message.
        [[[ConnectMeVcx alloc] init] proofVerifierUpdateStateWithMessage:proofHandle
                                                                 message:proof...]
   
        // 1.4. Get result of proof verification.
        NSString *result = [[[ConnectMeVcx alloc] init] proofVerifierGetProofMessage:proofHandle...]
    ```
 
### Preparation of Connection Agent in advance

Every time MSDK accepts a Connection Invitation it creates a new specific Pairwise Agent (matching to the connection) on the Agency.
The operation of creating Pairwise Agent happens during `ConnectionApi.vcxConnectionConnect` and `[[[ConnectMeVcx alloc] init] connectionConnect` API  calls and it takes approximately 2 seconds.
It increases the overall time taken to establish a connection with the remote side.
In order to decrease that time your application can create Pairwise Agents in advance and assign them to a particular connection.

#### Steps

1. Create new Pairwise Agent

    Java pseudocode:
    ```
        String agentInfo = UtilsApi.vcxCreatePairwiseAgent() 
    ```
    
    Objective-C pseudocode
    ```
        NSString *agentInfo = [[[ConnectMeVcx alloc] init] createPairwiseAgent...]
    ```

2. Store `agentInfo` in the application state/store.
3. When you accept a new connection invitation fetch `agentInfo` and pass it as the parameter to `connect` function.

    Java pseudocode:
    ```
        String connectionOption = JSON.toString({ 'pairwise_agent_info': JSON.parse(agentInfo) })
        ConnectionApi.vcxConnectionConnect(connectionHandle, connectionOption)
    ```
    
    Objective-C pseudocode
    ```
        NSString *connectionOption = JSON.toString({ 'pairwise_agent_info': JSON.parse(agentInfo) })
        [[[ConnectMeVcx alloc] init] connectionConnect:connectionHandle
                                                    connectionType:connectionOption...]
    ```
4. Create a new Pairwise Agent and store it.
