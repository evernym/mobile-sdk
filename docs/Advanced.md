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
In fact, Pool Ledger connectivity required only when you receive credentials and prove them for the first time ( if you don't use [Cache data for Proof generation](./4.Credentials.md#cache-data-for-proof-generation---optional) technique).
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

In this scenario credential Holder (mobile app) initiate the Present Proof protocol in order to share credential information with another side (can be a mobile app as well). 

#### Steps

1. Holder App generates Presentation Proposal and puts it into Aries Out-Of-Band invitation (without handshake):

    Java pseudocode:
    ```
        Integer credentialHandle = CredentialApi.credentialDeserialize(serializedCredentialStateObject)
        String presentationProposal = CredentialApi.credentialGetPresentationProposal(credentialHandle)
        Integer connectionHandle = ConnectionApi.vcxConnectionCreateOutofband('sourceId', 'present-credential', 'Present Credential', false, presentationProposal)
        ConnectionApi.vcxConnectionConnect(connectionHandle, '{}')
        String connectionSerialized = Connection.Api.connectionSerialize(connectionHandle)
    ```
    
    Objective-C pseudocode
    ```
        NSInteger credentialHandle = [[[ConnectMeVcx alloc] init] credentialDeserialize:serializedCredential...]
        NSString *presentationProposal = [[[ConnectMeVcx alloc] init] credentialGetPresentationProposal:credential_handle...]
        NSInteger connectionHandle = [[[ConnectMeVcx alloc] init] connectionCreateOutofband:sourceId
                                                                                   goalCode:goalCode
                                                                                       goal:goal
                                                                                  handshake:handshake
                                                                              requestAttach:requestAttach...]
        [[[ConnectMeVcx alloc] init] connectionConnect:connectionHandle
                                                    connectionType:@"{}"...]
        NSString *connectionSerialized = [[[ConnectMeVcx alloc] init] connectionSerialize:connectionHandle...]
    ```

2. Verifier App accept Out-Of-Band invitation, create Verifier state object and send Proof Request. App need to extract Presentation Proposal message from invitation `request~attach` field.
    ```
    String presentationProposal = base64decode(invitation['request~attach'][0]['data']['base64'])
    ```
 
    Java pseudocode:
    ```
        Integer connectionHandle = ConnectionApi.createConnectionWithOutOfBandInvite('sourceId', invitation)
        ConnectionApi.vcxConnectionConnect(connectionHandle, '{}')
        Integer proofHandle = ProofApi.proofCreateWithProposal('sourceId', presentationProposal, 'name')
        ProofApi.proofSendRequest(proofHandle, connectionHandle)
        String proofRequest = ProofApi.proofGetRequestMsg(proofHandle)
        String serializedProof = ProofApi.proofSerialize(proofHandle)
    ```
    
    Objective-C pseudocode
    ```
        NSInteger connectionHandle = [[[ConnectMeVcx alloc] init] connectionCreateWithOutofbandInvite:@"sourceId"
                                                                                                 invite:invitation...]
        [[[ConnectMeVcx alloc] init] connectionConnect:connectionHandle
                                                    connectionType:@"{}"...]
        NSInteger proofHandle = [[[ConnectMeVcx alloc] init] createProofVerifierWithProposal:@"sourceId"
                                                                        presentationProposal:presentationProposal
                                                                                        name:@"name"...]
        [[[ConnectMeVcx alloc] init] proofVerifierSendRequest:proofHandle
                                               connectionHandle:connectionHandle...]
        NSString *proofRequest = [[[ConnectMeVcx alloc] init] proofVerifierGetProofRequestMessage:proofHandle...]
        NSString *serializedProof = [[[ConnectMeVcx alloc] init] proofVerifierSerialize:proofHandle...]
    ```

3. Holder App accept Presentation Request, generates and sends Proof.
   
   Java pseudocode:
    ```
        Integer proofHandle = DisclosedProofApi.proofCreateWithRequest('sourceId', proofRequest)
        String credentials = DisclosedProofApi.proofRetrieveCredentials(proofHandle)
        // select credentials to use
        DisclosedProofApi.proofGenerate(proofHandle, selectedCredentials, selfAttestedAttributes)
        Integer connectionHandle = ConnectionApi.connectionDeserialize(serializedConnection)
        DisclosedProofApi.proofSend(proofHandle, connectionHandle)
    ```
    
    Objective-C pseudocode
    ```
        NSInteger proofHandle = [[[ConnectMeVcx alloc] init] proofCreateWithRequest:sourceId
                                                                     withProofRequest:proofRequest...]
        NSString *credentials = [[[ConnectMeVcx alloc] init] proofRetrieveCredentials:proofHandle...]
        // select credentials to use
        [[[ConnectMeVcx alloc] init] proofGenerate:proofHandle
                             withSelectedCredentials:selectedCredentials
                               withSelfAttestedAttrs:selfAttestedAttributes...]
        NSInteger connectionHandle = [[[ConnectMeVcx alloc] init] connectionDeserialize:serializedConnection...]
        [[[ConnectMeVcx alloc] init] proofSend:proofHandle
                            withConnectionHandle:connectionHandle...]
    ```
    
4. Verifier App handle received Proof message
   
   Java pseudocode:
    ```
        Integer proofHandle = ProofApi.proofDeserialize(serialized)
        ProofApi.proofUpdateStateWithMessage(proofHandle, proof)
        vcx.proof.GetProofResult result = ProofApi.getProofMsg(proofHandle)
    ```
    
    Objective-C pseudocode
    ```
        NSInteger proofHandle = [[[ConnectMeVcx alloc] init] proofDeserialize:serializedProof...]
        [[[ConnectMeVcx alloc] init] proofVerifierUpdateStateWithMessage:proofHandle
        NSObject *result = [[[ConnectMeVcx alloc] init] proofVerifierGetProofMessage:proofHandle...]
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
