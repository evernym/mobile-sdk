# VCX Errors

In communication with cloud agent and VCX library, it is possible you will stumble upon some errors. 
In order to better understand what those errors mean, this document contains information about all errors you can get. 

In [Error Details](#error-details) section you can find information about how to get additional details for an error that occurred in VCX.

### Errors

##### Common
- Success: 
    * code: 0
    * description: a function completed with success
- InvalidState: 
    * code: 1081
    * description: an object is in a state in which can't perform the requested action
    * causes:
        * Cannot get a requested attribute of state object
        * Cannot perform requested action with state object
        
- InvalidConfiguration: 
    * code: 1004
    * description: Invalid configuration JSON passed into VCX function
    * causes:
        * Configuration JSON passed into `vcx_init_*` has invalid format or validation for some field failed
        * Configuration JSON passed into `vcx_provision_agent_*` has invalid format or validation for some field failed
        * Configuration JSON file not found
        
- InvalidHandle: 
    * code: 1048
    * description: VCX keeps state objects within in-memory Object Cache. Objects are accessible by an integer handle 
                   The error is raised if an object for the requested handle is not found in the Object Cache
- InvalidJson:
    * code: 1016
    * description: Cannot parse an object from JSON string (JSON has invalid format, field missing, field has invalid type)
    
- InvalidOption:
    * code: 1007
    * description: Passed invalid parameter
    * causes:
        * Cannot restore a string from pointer
        * Passed a combination of incompatible parameters
    
- InvalidMessagePack:
    * code: 1019
    * description: Cannot pack/unpack message
    
- ObjectCacheError:
    * code: 1070
    * description: Could not get access to Object Cache
    
- NotReady:
    * code: 1005
    * description: A state object is in a state in which can not perform requested action
    
- IOError:
    * code: 1074
    * causes:
        * Could not perform IO operation with a Wallet
        * Could not create Wallet backup
        * Attachment contains invalid bytes
    
- SerializationError:
    * code: 1050
    * description: Could not serialize object into JSON string
    
- NotBase58:
    * code: 1014
    * description: Could not encode/decode base58 string
    
- InvalidProvisioningToken:
    * code: 1107
    * description: Token provided by sponsor is invalid
    * causes:
        * Could not parse Token object form JSON string
        * Provisioning with provided Token failed on Agency
    * format
    ```
    // Token JSON
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
    * code: 1044
    * description: Library already has been initialized before 
    You need to call `vcx_shutdown` function first to be able initialize library again
    
- ActionNotSupported:
    * code: 1103
    * description: Requested action is not supported for state Object. 
    Some functions can be used for `proprietary` objects only and some for `aries`.
        
- EncodeError: 
    * code: 1022
    * description: Could not encode string to a big integer
    * causes:
        * Could not generate nonce using for Proof Request preparation
        * Count not encode provided Credential attribute values
    
##### Validation
    
- MissingPaymentMethod: 
    * code: 1087
    * description: Library initialization config does not contain `payment_method` field.
    If the payment method is specified but the payment library has not been initialized null-pointer can be received in runtime.
    
- InvalidDid: 
    * code: 1008
    * description: DID validation failed
    
- InvalidVerkey: 
    * code: 1009
    * description: Verkey validation failed
    
- InvalidNonce: 
    * code: 1011
    * description: Proof Request Nonce validation failed
    
- InvalidUrl: 
    * code: 1013
    * description: URL validation failed
    
##### Connection

- CreateConnection:
    * code: 1061
    * description: Could not create and store Connection state object. Likely, the problem relates to Object Cache

- InvalidConnectionHandle:
    * code: 1003
    * description: Cannot find Connection object associated with the handle in the Object Cache 

- InvalidInviteDetail:
    * code: 1045
    * description: Could not parse Connection Invitation from JSON

- InvalidRedirectDetail:
    * code: 1104
    * description: Could not parse Connection Redirection Details from JSON

- DeleteConnection:
    * code: 1060
    * description: Could Delete Connection. Check status of connection is appropriate to be deleted from agency.

- GeneralConnectionError:
    * code: 1002
    * description: Error occurred during connection establishment
    * causes:
        * Connection state object is in state not ready to connect/redirect
        * Invitation not found in Connection state object

- NoAgentInformation:
    * code: 1106
    * description: Connection object does not contain information about provisioned Agent 

- NoEndpoint:
    * code: 1006
    * description: No Endpoint set for Connection

##### Credential Definition
    
- CreateCredDef:
    * code: 1034
    * description: Call to create Credential Definition failed
    * causes:
        * Cannot restore CredentialDefinition state object from string
        * Cannot create RevocationRegistry for Credential Definition supporting revocation.

- CredDefAlreadyCreated:
    * code: 1039
    * description: Cannot create Credential Definitions as a Credential Definition referring to the same `id` is already exists in wallet
    * tip: Use `tag` field to create a new Credential Definition referring to the same schema and DID
    
- InvalidCredDefHandle:
    * code: 1037
    * description: Cannot find Credential Definition object associated with the handle in the Object Cache. 
    
##### Revocation
    
- InvalidRevocationDetails:
    * code: 1091
    * description: Cannot parse Revocation Registry Details from JSON string
    * format:
    ```
        {
            support_revocation: Option<bool>,
            tails_file: Option<String>,
            max_creds: Option<u32>,
        }
    ```
    
- InvalidRevocationEntry:
    * code: 1092
    * description: Unable to Update Revocation Delta on the Ledger
    
- InvalidRevocationTimestamp:
    * code: 1093
    * description: Revocation timestamp not found or cannot request Revocation Registry for requested timestamp
    
##### Credential
    
- InvalidCredentialHandle: 
    * code: 1053
    * description: Cannot find Credential object associated with the handle in the Object Cache
    
- CreateCredentialRequest:
    * code: 1055
    * description: Credential Offer message does not contain `msg_ref_id` field pointing to the message on the Agency
    
##### Issuer Credential
    
- InvalidIssuerCredentialHandle
    * code: 1015
    * description: Cannot find Issuer Credential object associated with the handle in the Object Cache
    
- InvalidCredentialRequest:
    * code: 1018
    * description: Credential Request message not found or does not contain `msg_ref_id` field pointing to the message on the Agency
    
- InvalidCredential:
    * code: 1054
    * causes:
        * Credential Offer message that can be used to create Credential Request not found in the state object
        * Cannot parse Credential message from JSON string
    
- InvalidAttributesStructure:
    * code: 1021
    * description: Attributes provided to create Credential Offer has invalid format
    * format:
    ```
    {"state":"UT"}
    ```
    
##### Proof
- InvalidProofHandle:
    * code: 1017
    * description: Cannot find Proof object associated with the handle in the Object Cache
    
- InvalidDisclosedProofHandle:
    * code: 1049
    * description: Cannot find Disclosed Proof object associated with the handle in the Object Cache
    
- InvalidProof:
    * code: 1023
    * description: Received Proof is invalid
    * causes:
        * Cannot parse Proof from JSON string
        * Encoded values from Proof do not match to expected
        * Proof verification failed
    
- InvalidSchema:
    * code: 1031
    * description: Schema received from the Ledger was invalid or corrupted
    
- InvalidProofCredentialData:
    * code: 1027
    * description: The received Proof contains invalid credential identifiers
    
- CreateProof:
    * code: 1056
    * description: Cannot generate Proof
    * causes:
        * Cannot find Proof Request in Disclosed Proof state object
        * Proof Request message not found or does not contain `msg_ref_id` field pointing to the message on the Agency.
        * Cannot get Proof message as it's not ready yet
    
- InvalidProofRequest:
    * code: 1086
    * description: Proof Request has invalid format
    
##### Schema
    
- CreateSchema:
    * code: 1041
    * description: Cannot restore Schema state object from string
    
- InvalidSchemaHandle:
    * code: 1042
    * description: Cannot find Schema object associated with the handle in the Object Cache
    
- InvalidSchemaSeqNo:
    * code: 1040
    * description: Cannot find Schema on the Ledger for requested id
    
- DuplicationSchema:
    * code: 1088
    * description: Could not create Schema as Ledger already contains schema for given DID, Version, and Name combination
    
- UnknownSchemaRejection:
    * code: 1094
    * description: Schema rejected on the Ledger
    
##### Pool
    
- InvalidGenesisTxnPath:
    * code: 1024
    * description: Pool genesis file is invalid or does not exist
    
- CreatePoolConfig:
    * code: 1026
    * description: Could not create Indy Pool Ledger config file
    
- PoolLedgerConnect:
    * code: 1025
    * description: Could not connect to Poole Ledger
    * causes:
        * Pool Ledger config file does not exist
        * Pool Ledger Genesis transactions are invalid
    
- InvalidLedgerResponse:
    * code: 1082
    * description: Could parse transaction response from the Ledger
    
- NoPoolOpen:
    * code: 1030
    * description: There is no opened Pool Ledger. Possibly,library initialization went wrong
    
- PostMessageFailed:
    * code: 1010
    * description: Sending a message on Agency or Remote Endpoint failed with HTTP error
    
##### Wallet    

- WalletCreate:
    * code: 1058
    * description: Could not create Indy Wallet
    
- MissingWalletName:
    * code: 1076
    * description: Missing wallet name in export/import config JSON
    
- MissingExportedWalletPath:
    * code: 1077
    * description: Missing exported wallet path in config JSON
    
- MissingBackupKey:
    * code: 1078
    * description: Missing exported backup key in config JSON
    
- WalletAccessFailed:
    * code: 1075
    * description: Attempt to open Wallet failed with invalid credentials
    
- InvalidWalletHandle:
    * code: 1057
    * description: Try to access Wallet with invalid handle
    
- DuplicationWallet:
    * code: 1051
    * description: Try to create Wallet with a duplicate name
    
- WalletRecordNotFound:
    * code: 1073
    * description: Could not find a record with the requested id in a Wallet
    
- DuplicationWalletRecord:
    * code: 1072
    * description: Wallet record for given id already exists
    
- WalletNotFound:
    * code: 1079
    * description: Could not open/delete Wallet for the given name as it is not found
    
- WalletAlreadyOpen:
    * code: 1052
    * description: Could not open Wallet as it is already opened
    
- MissingWalletKey:
    * code: 1069
    * description: Library config JSON does not contain `wallet_key` field
    
- DuplicationMasterSecret:
    * code: 1084
    * description: Attempted to add a Master Secret with a name that already existed in wallet
    
- DuplicationDid:
    * code: 1083
    * description: Attempted to add a DID to wallet when that DID already exists in wallet
    
##### Wallet Backup  

- CreateWalletBackup:
    * code: 1096
    * description: Could not prepare Wallet Backup because expected field not found in the state Object

- RetrieveExportedWallet:
    * code: 1097
    * description: Failed to retrieve exported wallet
    * causes:
        * Could not find exported wallet file
        * Expected field not found in the state Object

- RetrieveDeadDrop:
    * code: 1099
    * description: Failed to retrieve Dead Drop payload
  
##### A2A    

- InvalidHttpResponse:
    * code: 1033
    * description: Could not decode and parse expected value from the response body

- InvalidMessages:
    * code: 1020
    * description: Error Retrieving messages from external API
    * causes:
        * Response does not return any messages
        * Message of expected type not found in response

- InvalidMsgVersion:
    * code: 1098
    * description: Received Message of unsupported version
    
##### Payments
    
- NoPaymentInformation:
    * code: 1071
    * description: There is no payment information/transaction associated with requested object (Credential Definition, Schema, Credential). 
    
- InsufficientTokenAmount:
    * code: 1064
    * description: Insufficient amount of tokens to perform payment request.
    
- InvalidPaymentAddress: 
    * code: 1066
    * description: Payment transaction contains invalid payment address.
    
##### Libndy
    
- LibindyInvalidStructure:
    * code: 1080
    * description: Object (json, config, key, credential and etc...) passed to libindy has invalid structure
    
- TimeoutLibindy:
    * code: 1038
    * description: Waiting for callback timed out
    
- InvalidLibindyParam:
    * code: 1067
    * description: Parameter passed to libindy was invalid
    
- LibndyError(code):
    * code: code
    * description: Error raised from `Libindy` library and haven't handled by `Vcx`
    
- UnknownLibndyError:
    * code: 1035
    
##### Logger
    
- LoggingError:
    * code: 1090
    * description: Logger initialization failed. Possibly, Logger already has been initialized.

##### Unexpected Errors

- UnknownError: 
    * code: 1001
    * causes:
        * Could not serialize object into JSON
        * Could not encode object into bytes
        * Could not encode string

- Common:
    * causes:
        * Unable to lock store object
        * Could not decode bytes


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