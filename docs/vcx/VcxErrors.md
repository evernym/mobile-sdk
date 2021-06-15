# VCX Errors

In communication with cloud agent and VCX library, it is possible you will stumble upon some errors. 
In order to better understand what those errors mean, this document contains information about all errors you can get. 

In [Error Details](#error-details) section you can find information about how to get additional details for an error that occurred in VCX.

### Errors

##### Common
- Success: 
    * code: 0
    * description: a function completed with success
    
- UnknownError:
    * code: **1001**  
    * description: Unknown error happened 
        
- InvalidState: 
    * code: **1081**
    * description: Object is in a state in which can't perform the requested action
    * causes:
        * Cannot get a requested attribute of state object (the state of object suppose to have the requested field but it is missing).
        Possibly, the object has been restored from a malformed string.
        
- InvalidDelegate:
    * code: **1012**
    * description: Invalid DELEGATE        
        
- InvalidConfiguration: 
    * code: **1004**
    * description: Invalid configuration JSON passed into provisioning or library initialization functions
    * causes:
        * Configuration JSON passed into `vcx_init_*` has invalid format or validation for some field failed
        * Configuration JSON passed into `vcx_provision_agent_*` has invalid format or validation for some field failed
        * Configuration JSON file not found by the path passed into `vcx_init` function.
        * Internal (these errors should be caught and handled internally):
            * Cannot read library settings
            * Cannot read the value for some key from the library settings
    * tip: Check the format of config JSON. The list of available options you can find in the [document](./Configuration.md).

- InvalidJson:
    * code: **1016**
    * description: Cannot parse an object from JSON string (JSON has invalid format, field missing, field has an invalid type)
    * tip: Ensure JSON parameters you pass to function match to defined format

- InvalidOption:
    * code: **1007**
    * description: The value passed for a mandatory parameter is null
    * tip: Ensure that you do not pass `null` values for required parameters

- IncompatibleParameter:
    * code: **1111**
    * description: Passed a combination of incompatible parameters. Some functions declare self exclusive parameters
    * tip: Check function documentation to ensure that you do not pass self exclusive parameters

- InvalidMessagePack:
    * code: **1019**
    * description: Cannot encode/decode message using MessagePack
    * note: MessagePack encoding is used from communication with Agency when library initialized with `protocol_type:1.0`
    
- ObjectCacheError:
    * code: **1070**
    * description: Could not get access to Object Cache containing state objects
    * note: unexpected internal error
    
- NotReady:
    * code: **1005**
    * description: A state object is in a state in which can not perform requested action
    * causes:
        * You try to perform an action with a state object which has not come into the appropriate state yet  
        * You try to get state object attribute which is not received yet
        * You try to use uncompleted Connection to send messages

- IOError:
    * code: **1074**
    * description: Cannot perform input output operation 
    * causes:
        * Could not create / read / write file
        * Could not perform IO operation with a Wallet (Wallet not found or unavailable)
        * Could not create Wallet backup
    * tips:
        * Ensure the specifying file exists / available
        * Ensure that there are enough permissions to perform input/output operations on a file system
    
- SerializationError:
    * code: **1050**
    * description: Could not represent object as a JSON string
    
- NotBase58:
    * code: **1014**
    * description: Could not encode/decode base58 string
    * tip: Check validity of passed parameters which are base58 strings
    
- InvalidProvisioningToken:
    * code: **1107**
    * description: Token object passed into `vcx_provision_agent_with_token` is invalid
    * causes:
        * Could not parse Token object form JSON string
        * Provisioning with provided Token failed on Agency
    * format: Token JSON
        ```
        {
            sponseeId: String,
            sponsorId: String,
            nonce: String,
            timestamp: String,
            sig: String,
            sponsorVerKey: String,
        }
        ```
    
- AlreadyInitialized:
    * code: **1044**
    * description: The library already has been initialized whit one of `vcx_init*` functions
    * tips: 
        * You need to call `vcx_shutdown` function first to be able initialize library again
        * The combination of `vcx_shutdown` + `vcx_init*` can be used to switch used environment
    
- ActionNotSupported:
    * code: **1103**
    * description: Requested action is not supported for state object 
    * tip: Some functions are allowed for only one of available `protocol_type`: 
        * `proprietary - 1.0/2.0` 
        * `aries - 3.0`.
    * tip: Most probably you try to perform an action with a state object which is not implemented for the object.
    You either need to set a different `protocol_type` during library initialization or created an object with an invitation/offer/request relates to one of protocols.

- EncodeError: 
    * code: **1022**
    * description: Could not encode string to a big integer
    * causes:
        * Could not generate nonce using for Proof Request preparation
        * Count not encode provided Credential attribute values
        
- IndyWalletNotFound:
    * code: **212**
    * description: Error from Indy: Wallet Item not found
    
- InvalidHttpResponse:
    * code: **1033**
    * description: Invalid HTTP response.
    
- InvalidLibIndyError:
    * code: **1035**
    * description: Unknown libindy error
    
- InvalidPoolName:
    * code: **1059**
    * description: Pool Name in config was invalid     
        
- InvalidWalletKeys:
    * code: **1062**
    * description: Invalid wallet keys...have you provisioned correctly?
    
- CommonError:
    * code: **1063**
    * description: Common Error     
     
- UnknownLedgerTransactionType:
    * code: **1065**
    * description: Unknown ledger transaction type
    
- InvalidPaymentDetails:
    * code: **1068**
    * description: Invalid Payment Details     
          
- MissingWalletName:
    * code: **1076**
    * description: Missing wallet name in config          
    
- MissingExportedWalletPath:
    * code: **1077**
    * description: Missing exported wallet path in config      
          
- MissingExportedBackupKey:
    * code: **1078**
    * description: Missing exported backup key in config 
          
- UnableToCreateThread:
    * code: **1085**
    * description: Unable to create thread
    
- LibindyRejection:
    * code: **1089**
    * description: Unknown libindy rejection
    
- InvalidA2AMessage:
    * code: **1098**
    * description: Invalid A2A Message version                     
          
##### Handles
   
VCX keeps all state objects within in-memory Object Cache (which actually is thread safe HashMap). 
These objects are accessible by an integer handle.
You use handles every time you want to perform an action with some object or pass referent to another object as parameter.
The error is raised if an object for the requested handle is not found in the Object Cache.
  
**Note**: Please note that VCX does not store state into some persistent storage. 
When you unloaded the library all objects disappear. 
You need either create them again or deserialize from a string.
        
- InvalidHandle:
    * code: **1048**
    * description: Cannot find state object in the Object Cache
    * causes:
        * Cannot find Wallet Backup object associated with the handle passed into `vcx_wallet_backup_*` functions
    * suggestion: 
        * Ensure you pass correct handle correspondent to state object
    * note: If you unloaded the library all objects disappear. You need either create them again or deserialize from string.
     
- InvalidConnectionHandle:
    * code: **1003**
    * description: Cannot find Connection object in the Object Cache
    * causes:
        * Cannot find Connection object associated with the handle passed into `vcx_connection_*` functions
        * Cannot find Connection object associated with the handle passed into other functions depending on connection
    * suggestion: 
        * Ensure you pass correct handle correspondent to Connection object
    * note: If you unloaded the library all objects disappear. You need either create them again or deserialize from string.
     
- InvalidCredentialHandle:
    * code: **1053**
    * description: Cannot find Credential object in the Object Cache
    * causes:
        * Cannot find Credential object associated with the handle passed into `vcx_credential_*` functions
    * suggestion: 
        * Ensure you pass correct handle correspondent to Credential object
    * note: If you unloaded the library all objects disappear. You need either create them again or deserialize from string.
         
- InvalidIssuerCredentialHandle:
    * code: **1015**
    * description: Cannot find Issuer Credential object in the Object Cache
    * causes:
        * Cannot find Issuer Credential object associated with the handle passed into `vcx_issuer_credential_*` functions
    * suggestion: 
        * Ensure you pass correct handle correspondent to Issuer Credential object
    * note: If you unloaded the library all objects disappear. You need either create them again or deserialize from string.
    
- InvalidDisclosedProofHandle:
    * code: **1049**
    * description: Cannot find Disclosed Proof object in the Object Cache
    * causes:
        * Cannot find Disclosed Proof object associated with the handle passed into `vcx_disclosed_proof_*` functions
    * suggestion: 
        * Ensure you pass correct handle correspondent to Disclosed Proof object
    * note: If you unloaded the library all objects disappear. You need either create them again or deserialize from string.
        
- InvalidProofHandle:
    * code: **1017**
    * description: Cannot find Proof object in the Object Cache
    * causes:
        * Cannot find Proof object associated with the handle passed into `vcx_proof_*` functions
    * suggestion: 
        * Ensure you pass correct handle correspondent to Proof object
    * note: If you unloaded the library all objects disappear. You need either create them again or deserialize from string.
            
- InvalidCredDefHandle:
    * code: **1037**
    * description: Cannot find Credential Definition object in the Object Cache
    * causes:
        * Cannot find Credential Definition object associated with the handle passed into `vcx_credentialdef_*` functions
    * suggestion: 
        * Ensure you pass correct handle correspondent to Credential Definition object
    * note: If you unloaded the library all objects disappear. You need either create them again or deserialize from string.
                
- InvalidSchemaHandle:
    * code: **1042**
    * description: Cannot find Schema object in the Object Cache
    * causes:
        * Cannot find Schema object associated with the handle passed into `vcx_schema_*` functions
    * suggestion: 
        * Ensure you pass correct handle correspondent to Schema object
    * note: If you unloaded the library all objects disappear. You need either create them again or deserialize from string.
    
##### Validation
    
- MissingPaymentMethod: 
    * code: **1087**
    * description: The `payment_method` is not found in library setting.
    * causes: The `payment_method` field has not been passed during library initialization and now you try to perform a payment related action.
    * warn: In some cases null-pointer can be received in runtime when you has not initialized a payment library and try to perform a payment related action. 
    
- InvalidDid: 
    * code: **1008**
    * description: DID validation failed. DID must be a base58 string
    
- InvalidVerkey: 
    * code: **1009**
    * description: Verkey validation failed. Verkey must be a base58 string
    
- InvalidNonce: 
    * code: **1011**
    * description: Proof Request Nonce validation failed. Nonce must be at most 80-bit integer
    
- InvalidUrl: 
    * code: **1013**
    * description: URL-like validation failed
    
- InvalidDIDDoc: 
    * code: **1108**
    * description: Received DIDDoc has invalid format
    
- InvalidAttachmentEncoding: 
    * code: **1100**
    * description: Message attachment cannot be decoded or has unsupported format
    
- InvalidSelfAttestedValue:
    * code: **1046**
    * description: Self Attested Value invalid 
    
- InvalidProofPredicate:
    * code: **1047**
    * description: Predicate in proof is invalid     
    
##### Connection

- CreateConnection:
    * code: **1061**
    * description: Could not store Connection state object into the Object Cache

- ConnectionError:
    * code: **1002**  
    * description: Error with Connection
    * causes: Invitation connection object is probably invalid  
    * tip: Check if invitation is valid json object. For protocol v2, url parameter should be base64 decoded first
    
- ConnectionMissingEndpoint:
    * code: **1006**
    * description: No Endpoint set for Connection Object          
    
- InvalidInviteDetail:
    * code: **1045**
    * description: Could not parse Connection Invitation from JSON string
    * format: the format depends on the protocol inviter use
        * proprietary `protocol_type:1.0/2.0`: 
            ```
            {
                "targetName": string, 
                "statusMsg": string, 
                "connReqId": string,
                "statusCode": string,
                "threadId": Optional<string>, 
                "senderAgencyDetail": {
                    "endpoint": string, 
                    "verKey": string, 
                    "DID": string
                }, 
                "senderDetail": {
                    "agentKeyDlgProof": {
                        "agentDID": string, 
                        "agentDelegatedKey": string,
                        "signature": string
                    }, 
                    "publicDID": string, 
                    "name": string, 
                    "logoUrl": string, 
                    "verKey": string, 
                    "DID": string
                }
            }
            ```
        * aries `protocol_type:3.0`
            ```
              {
                 "@type": string,
                 "label": string,
                 "serviceEndpoint": string,
                 "recipientKeys": [string],
                 "routingKeys": [string]
              }
            ```

- InvalidRedirectDetail:
    * code: **1104**
    * description: Could not parse Connection Redirection Details / Out-of-Band Invitation from JSON string
    * format:
        * proprietary `protocol_type:1.0/2.0` Connection Redirection Details: 
            ```
            {
                "targetName": string, 
                "statusMsg": string, 
                "connReqId": string,
                "statusCode": string,
                "threadId": Optional<string>, 
                "senderAgencyDetail": {
                    "endpoint": string, 
                    "verKey": string, 
                    "DID": string
                }, 
                "senderDetail": {
                    "agentKeyDlgProof": {
                        "agentDID": string, 
                        "agentDelegatedKey": string,
                        "signature": string
                    }, 
                    "publicDID": string, 
                    "name": string, 
                    "logoUrl": string, 
                    "verKey": string, 
                    "DID": string
                }
            }
            ```
        * aries `protocol_type:3.0` Out-of-Band Invitation
            ````
            {
              "@type": "https://didcomm.org/out-of-band/%VER/invitation",
              "@id": "<id used for context as pthid>",
              "label": string
              "handshake_protocols": [string]
              "service": [
                  {
                    "id": "#inline"
                    "type": "did-communication",
                    "recipientKeys": [string],
                    "routingKeys": [string],
                    "serviceEndpoint": string
                  }
              ]
            }
            ```
    
- DeleteConnection:
    * code: **1060**
    * description: Could Delete Connection object
    * causes: 
        * Connection is in unappropriated state
        * Could not delete relate Agent on Agency
    * tip: Check status of connection is appropriate to be deleted from the Agency

- NoAgentInformation:
    * code: **1106**
    * description: Connection state object does not contain information about provisioned Agent 

##### Credential Definition
    
- CredentialDefinitionRequestFailed:
    * code: **1029**
    * description: Call to indy credential def request failed
        
- CreateCredDef:
    * code: **1034**
    * description: Call to create Credential Definition failed
    * causes:
        * Could not store CredentialDefinition state object into Object Cache
        * Cannot generate RevocationRegistry for Credential Definition supporting revocation.

- CredDefAlreadyCreated:
    * code: **1039**
    * description: Cannot create Credential Definitions as a Credential Definition referring to the same `id` is already exists in wallet
    * tip: Use `tag` field to create a new Credential Definition referring to the same schema and DID
    
- CredentialDefinitionNotFound:
    * code: **1036**
    * description: Cannot get Credential Definition from the Ledger
    * causes:
        * Credential Definition for requested id does not exist on the Ledger
        * Library is connected to different Ledger    
        
- CreateRevRegDef:
    * code: **1095**
    * description: Cannot create Revocation Registry for Credential Definition supporting revocation
    
##### Revocation
    
- InvalidRevocationDetails:
    * code: **1091**
    * description: Cannot parse Revocation Details from provided JSON string
    * format:
        ```
            {
                support_revocation: Option<bool>,
                tails_file: Option<string>,
                max_creds: Option<u32>,
            }
        ```
    
- InvalidRevocationEntry:
    * code: **1092**
    * description: Unable to Update Revocation Delta on the Ledger
    
- InvalidRevocationTimestamp:
    * code: **1093**
    * description: Revocation timestamp not found on Revocable Credential or cannot request Revocation Registry from the Ledger for requested timestamp
    
##### Credential
    
- InvalidCredentialRequest:
    * code: **1018**
    * description: Invalid Credential Request message
    * cause:
        * Cannot deserialize Credential Request from provided JSON string
        * Credential Request does not contain `msg_ref_id` field pointing to the message on the Agency

- InvalidCredentialOffer:
    * code: **1043**
    * description: Invalid Credential Offer message
    * causes:
        * Cannot parse Credential Offer message from JSON string
        * Credential Offer does not contain `msg_ref_id` field pointing to the message on the Agency

- InvalidCredential:
    * code: **1054**
    * description: Invalid Credential message
    * causes:
        * Cannot parse Credential message from JSON string

- CredentialRequestFailed:
    * code: **1055**
    * description: Could not create credential request 
    
- InvalidAttributesStructure:
    * code: **1021**
    * description: Invalid format of passed attributes
    * cause: 
        * Attributes provided to create Credential Offer has invalid format
            * format:
                ```
                {"state":string}
                ```
        * Attributes provided to create Proof Request has invalid format 
            * format:
                ```
                [
                    {
                        "name":Optional<string>,
                        "names":Optional<string>,
                        "restrictions": WQL - https://github.com/hyperledger/indy-sdk/tree/master/docs/design/011-wallet-query-language
                    },
                    ...
                ]
                ```
                NOTE: should either be "name" or "names", not both and not none of them.
                Use "names" to specify several attributes that have to match a single credential.

##### Proof       
- InvalidProof:
    * code: **1023**
    * description: Invalid Proof message
    * causes:
        * Cannot parse Proof message from JSON string
        * Proof verification failed
            * Proof math verification failed
            * Encoded values from Proof do not match to expected            
    
- InvalidProofCredentialData:
    * code: **1027**
    * description: Selected credentials passed for Proof generation has invalid format.
    * causes:
        * Cannot parse selected credentials
        * There are no credentials passed for a requested attribute.
    * format:
        ```
            {
                'attrs': {
                    'attribute_0': {
                        'credential_1': {
                            'cred_info': {
                                'cred_def_id': string, 
                                'schema_id': string, 
                                'referent': string, 
                                'attrs': {'attr_name': 'attr_value', ...}
                            }
                        },
                        ...
                    },
                    ...
                },
            }
        ```
- ProofIsNotCompliant:
    * code: **1032**
    * description: Proof is not compliant to proof request
    
- CreateProof:
    * code: **1056**
    * description: Cannot generate Proof
    * causes:
        * Cannot store Proof state object into the Object Cache
    
- InvalidProofRequest:
    * code: **1086**
    * description: Proof Request has invalid format
    * causes:
        * Cannot parse Proof Request message from JSON string
        * Proof Request does not contain `msg_ref_id` field pointing to the message on the Agency
        * Proof Request neither contains `requested_attributes` nor `requested_predicates`
    * format:
        ```
         {
             "name": string,
             "version": string,
             "nonce": string, 
             "requested_attributes": {
                  "attr_referent_1": {
                       "name": Optional<string>, 
                       "names": Optional<[string, string]>,
                       "restrictions": Optional<filter_json>, 
                  },
                  "attr_referent_2": {
                       "name": Optional<string>, 
                       "names": Optional<[string, string]>,
                       "restrictions": Optional<json>, 
                  }
                  ...,
             },
             "requested_predicates": {
                  "predicate_referent_1": {
                       "name": string
                       "p_type": string
                       "p_value": int
                       "restrictions": Optional<json>, 
                  }
                  ...,
              },
         }
        ```
    
- InvalidSchema:
    * code: **1031**
    * description: Cannot get Schema from the Ledger
    * causes:
        * Schema for requested id does not exist on the Ledger
        * Library is connected to different Ledger    
        * Cannot parse Schema from JSON string 
    
- InvalidPredicatesStructure:
    * code: **1028**
    * description: Predicates provided to create a Proof Request are not correct
    
##### Schema
    
- CreateSchema:
    * code: **1041**
    * description: Cannot store Schema state object into the Object Cache
    
- InvalidSchemaSeqNo:
    * code: **1040**
    * description: Cannot find Schema on the Ledger for requested id
    
- DuplicationSchema:
    * code: **1088**
    * description: Cannot not create Schema as Ledger already contains schema for given DID, Version, and Name combination
    * tip: Use different version to create a Schema with the same name
    
- UnknownSchemaRejection:
    * code: **1094**
    * description: Schema transaction has been rejected on the Ledger
    * causes:
        * DID you use does not has enough permissions on the ledger
        * Validation for schema attributes failed on the Ledger
    
##### Pool
    
- InvalidGenesisTxnPath:
    * code: **1024**
    * description: Pool genesis transactions file is invalid or does not exist
    * tip: 
        * Ensure the path to Pool genesis transactions file is correct
        * Ensure Pool genesis transactions has correct format
    
- CreatePoolConfig:
    * code: **1026**
    * description: Could not create Indy Pool Ledger config file
    * causes: 
        * Not enough permissions to create the config on the file system
    
- PoolLedgerConnect:
    * code: **1025**
    * description: Could not connect to Poole Ledger
    * causes:
        * Pool Ledger config file does not exist
        * Pool Ledger Genesis transactions are invalid
        * Pool Ledger Genesis transactions do not match to Network
    
- InvalidLedgerResponse:
    * code: **1082**
    * description: Could parse transaction response received from the Ledger
    
- NoPoolOpen:
    * code: **1030**
    * description: There is no opened Pool Ledger. 
    * causes: The Library initialization went wrong
    
##### Wallet    

- WalletCreate:
    * code: **1058**
    * description: Could not create Indy Wallet
    * causes:
        * Not enough permissions to create the Wallet on the file system
  
- WalletAccessFailed:
    * code: **1075**
    * description: Attempt to open Wallet failed with invalid credentials
    * causes:
        * Try to init library with `wallet_key` different of used for provisioning
        * Try to init library with `wallet_key_derivation` different of used for provisioning
    * tip: Ensure you pass the same combination of `wallet_key` and `wallet_key_derivation` as was used for provisioning/wallet creation.
    
- InvalidWalletHandle:
    * code: **1057**
    * description: Try to access Wallet with invalid handle
    * cause: VCX try to perform an operation with Wallet but Libindy does not have an opened wallet for used handle
    
- DuplicationWallet:
    * code: **1051**, **213**
    * description: Duplicate wallet, try to create Wallet with a duplicate name
    * tip: Try to use different `wallet_name` in provisioning config
    
- WalletRecordNotFound:
    * code: **1073**
    * description: Could not find a record with the requested id in a Wallet
    
- DuplicationWalletRecord:
    * code: **1072**
    * description: Wallet record for given id already exists
    
- WalletNotFound:
    * code: **1079**
    * description: Could not open/delete Wallet for the given name as it is not found
    
- WalletAlreadyOpen:
    * code: **1052**
    * description: Could not open Wallet as it is already opened
    * tip: call `vcx_shutdown` to reset library (close wallet / pool)
    
- MissingWalletKey:
    * code: **1069**
    * description: The config passed into provision / initialization functions does not contains `wallet_key` field.
    
- DuplicationMasterSecret:
    * code: **1084**
    * description: Attempted to add a Master Secret with a name that already existed in wallet
    * causes: Try to provision second time
    
- DuplicationDid:
    * code: **1083**
    * description: Attempted to add a DID to wallet when that DID already exists in wallet
    * causes: You try to do provisioning the second time

##### Wallet Backup  

- CreateWalletBackup:
    * code: **1096**
    * description: Could not prepare Wallet Backup because expected field not found in the state Object

- RetrieveExportedWallet:
    * code: **1097**
    * description: Failed to retrieve exported wallet
    * causes:
        * Could not find exported wallet file
        * Expected field not found in the state Object

- RetrieveDeadDrop:
    * code: **1099**
    * description: Failed to retrieve Dead Drop payload
  
##### A2A    

- PostMessageFailed:
    * code: **1010**
    * description: Could not send HTTP message on Agency or Remote Endpoint
    * causes:
        * Could not send message as remote endpoint is unreachable
        * Remote server responded with error status

- InvalidAgencyResponse:
    * code: **1020**
    * description: Error Retrieving messages from Agency
    * causes:
        * Agency responded with unexpected message
        * Agency has not returned any messages
        * Message of expected type not found in response
        * Agency returned message with empty payload
    
- MessageIsOutOfThread: 
    * code: **1109**
    * description: Received message is out of thread
    
##### Payments
    
- NoPaymentInformation:
    * code: **1071**
    * description: There is no payment information/transaction associated with requested object (Credential Definition, Schema, Credential). Creating an object did not require payments
    
- InsufficientTokenAmount:
    * code: **1064**
    * description: Insufficient amount of tokens to perform payment operation
    
- InvalidPaymentAddress: 
    * code: **1066**
    * description: Payment transaction does not contain or contains invalid payment address
    
##### Libndy
    
- LibindyInvalidStructure:
    * code: **1080**
    * description: The format of object parameter passed into Libindy has invalid structure (json, config, key, credential and etc...).
    * note: This is an unexpected internal error which means that the format of some Libindy messages has been changed.
    
- TimeoutLibindy:
    * code: **1038**
    * description: Waiting for callback passed into Libindy timed out (function has not been completed).
    * note: This is an unexpected internal error.
    
- InvalidLibindyParam:
    * code: **1067**
    * description: The parameter passed to libindy was invalid. Likely, null pointer has been passed for some reason
    * note This is an unexpected internal error.

- LibndyError(code):
    * code: code
    * description: Error raised from `Libindy` library and haven't handled by `Vcx`
    * note: This is an unexpected internal error. The list of all Liibndy error can be found [here](https://github.com/hyperledger/indy-sdk/blob/master/libindy/indy-api-types/src/lib.rs#L51)
    
##### Logger
    
- LoggingError:
    * code: **1090**
    * description: Logger initialization failed. Possibly, Logger already has been initialized.


### Error Details

* LibVCX C API extends `vcx_get_current_error` function. This function can be used to get details for last occurred error.This function should be called in two places to handle both cases of error occurrence:
Please note that there are two kinds of error for each of them this function should be used:
    * synchronous  - in the same application thread
    * asynchronous - inside of function callback

    NOTE: Error is stored until the next one occurs in the same execution thread or until asynchronous callback finished. Returning pointer has the same lifetime.

* Objective-C: `NSError` extended to contains additional error details within `userInfo` field:
    ```
    {
        "sdk_message": string,
        "sdk_full_message": string,
        "sdk_cause": string,
        "sdk_backtrace": string,
    }
    ```

* Java: `VcxException` class contains additional error details: 
    ```
    {
        "sdkErrorCode": int,
        "sdkMessage": string,
        "sdkFullMessage": string,
        "sdkCause": string,
        "sdkBacktrace": string,
    }
    ```

* Python: `VcxError` class contains additional error details: 
    ```
    {
        "error_msg": string,
        "sdk_error_full_message": string,
        "sdk_error_cause": string,
        "sdk_error_backtrace": string,
    }
    ```
