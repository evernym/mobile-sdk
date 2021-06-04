# Advanced Techniques

This document contains explanations for additional flows that can be implemented and optimizations can be done using Mobile SDK API functions.

* [Holder Present Proof](#holder-present-proof) 
* [Preparation of Connection Agent in advance](#preparation-of-connection-agent-in-advance)
* [Cache data for Proof generation](#cache-data-for-proof-generation)
* [Postponed Ledger connectivity](#postponed-ledger-connectivity)
* [Proof Request](#proof-request)

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


### Cache data for Proof generation

The first time the MSDK generates Proof for a specific credential it has to fetch it's public Credential Definition and Schema from the Ledger. 
It increases the overall time taken to present a proof to the remote side.
In order to decrease that time your application can fetch and cache these public entities right after receiving a credential.
Furthermore, it makes the Proof presentation independent from the Ledger connectivity.

#### Steps

Assume you received Credential message.  

1. Update Credential state object with received Credential message.

    Java pseudocode:
    ```
        Integer credentialHandle = CredentialApi.credentialDeserialize(serializedCredentialStateObject)
        CredentialApi.credentialUpdateStateWithMessage(credentialHandle, credential)
    ```
    
    Objective-C pseudocode
    ```
        NSInteger credentialHandle = [[[ConnectMeVcx alloc] init] credentialDeserialize:serializedCredential...]
        [[[ConnectMeVcx alloc] init] credentialUpdateStateWithMessage:credentialHandle
                                                                message:message...]
    ```

2. Fetch public Credential Definition and Schema from the Ledger associated with stored in the wallet credentials. It can be done as a background task.

    Java pseudocode:
    ```
        UtilsApi.vcxFetchPublicEntities()
    ```
    
    Objective-C pseudocode
    ```
        [[[ConnectMeVcx alloc] init] fetchPublicEntities...]

    ```


### Postponed Ledger connectivity

As a the first step of working with MSDK (assume you app is already did provisioning) you have to call a function to initialize it with prepared config (Java `VcxApi.vcxInitWithConfig(config)` and Objective-C `[[[ConnectMeVcx alloc] init] initWithConfig:config`).
Internally this function does three thing:
1. Sets settings passed within config
2. Open secured Wallet
3. Connects to a Pool Ledger

In fact Pool Ledger connectivity is needed only for receiving credentials (and proving them if you don't use [Cache data for Proof generation](#cache-data-for-proof-generation) technique).
You can make connections, answer questions and prove credentials without it.

You can optionally skip the Ledger Connectivity (which takes several seconds) on the library initialization and do it later by demand or right after initialization as a background task.

#### Steps

1. Remove `genesis_path` field from the config JSON passing into `initWithConfig` function and call initialization function.

2. Later, call function (for instance as a background task) to perform a connection to the Pool Ledger. You can run it in a separate thread.
    ```
    config = {
        "genesis_path": string,
        "pool_name": string,
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

### Proof Request

This section show different formats for Proof Requests that can be sent to credential holder.

In order to initiate Proof Verifier state object you need to call `Java ProofApi.proofCreate` or `Objective-C createProofVerifier:...` functions.

1. Request single attribute:
    ```
    requestedAttributes = [
        {'name': 'Name'}
    ]
    ```
    
1. Request single attribute restricted by Issuer:
    ```
    requestedAttributes = [
        {'name': 'Name', 'restrictions': {'issuer_did': 'ANM2cmvMVoWpk6r3pG5FAZ'}}
    ]
    
1. Request single attribute restricted by list of Issuer's:
    ```
    requestedAttributes = [
        {'name': 'Name', 'restrictions': {"issuer_did": {"$in": ["ANM2cmvMVoWpk6r3pG5FAZ", "BNM2cmvMVoWpk6r3pG5FAZ"]}}}
    ]
    
1. Request single attribute restricted by Credential Definition:
    ```
    requestedAttributes = [
        {'name': 'Name', 'restrictions': {'cred_def_id': 'ANM2cmvMVoWpk6r3pG5FAZ:3:CL:1:1'}}
    ]
    ```
    
1. Request single attribute that can be self attested:
    ```
    requestedAttributes = [
        {'name': 'Name'}
    ]
    
    // or 
    requestedAttributes = [
        {'name': 'Name', 'self_attest_allowed': true}
    ]
    ```
    
1. Request single attribute that cannot be self attested:
    ```
    requestedAttributes = [
        {'name': 'Name', 'self_attest_allowed': false}
    ]
    
    // or set any restriction 
    requestedAttributes = [
        {'name': 'Name', 'restrictions': {'issuer_did': 'ANM2cmvMVoWpk6r3pG5FAZ'}}
    ]
    ```
    
1. Request couple attributes which can be filled from different credentials:
    ```
    requestedAttributes = [
        {'name': 'Name'}
        {'name': 'Surname'}
    ]
    
1. Request couple attributes which must be filled from the same credential:
    ```
    requestedAttributes = [
        {'names': ['Name', 'Surname']}
    ]
    ```
    
1. Request couple attributes which must be filled from the same credential and restricted by issuer:
    ```
    requestedAttributes = [
        {'names': ['Name', 'Surname'], 'restrictions': {'issuer_did': 'ANM2cmvMVoWpk6r3pG5FAZ'}}
    ]
    ```
    
1. Request predicates:
    ```
    // Less or equal
    requestedPredicates = [
        { 'name': 'Age', 'p_type': '<=', 'p_value': 30 }
    ]
    // Less
    requestedPredicates = [
        { 'name': 'Age', 'p_type': '<', 'p_value': 30 }
    ]
    // Greater
    requestedPredicates = [
        { 'name': 'Age', 'p_type': '>', 'p_value': 30 }
    ]
    // Greater or equal
    requestedPredicates = [
        { 'name': 'Age', 'p_type': '>=', 'p_value': 30 }
    ]
    ```
    
1. Request predicates restricted by issuer:
    ```
    requestedPredicates = [
        { 'name': 'Age', 'p_type': '<=', 'p_value': 30, 'restrictions': {'issuer_did': 'ANM2cmvMVoWpk6r3pG5FAZ'} }
    ]
    ```
