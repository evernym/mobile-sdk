# Configuration

This document is split into two parts each of them contains the list of options that can be used for the corresponding action.

* [Agent Provisioning](#agent-provisioning-options) - preparation step to obtain a user associated Agent and create a Wallet.
* [Library Initialization](#library-initialization-options) - get the library ready for work.

## Agent Provisioning Options

#### Agency related options
The options below are used to create a user associated Agent on the Agency.

* `agency_endpoint` - a public endpoint of agency to connect.
* `agency_did` - DID assigned to the agency.
* `agency_verkey` - Verkey associated with agency DID.
* `agent_seed` - (optional) Seed to use to create DID/Verkey will be for the provisioned agent.
    If no value specified, a random pair will be generated. 
* `enterprise_seed` - (optional) Seed to use to create DID/Verkey associated with user.
    If no value specified, a random pair will be generated. 
* `did_method` - (optional) Method name to use to create fully qualified DIDs (`did:<did_method>:<generated did value>`).
    If no value specified, `unqualified` DID form will be used. 

#### Wallet related options
The options below are used as parameters for wallet will be created during provisioning.

* `wallet_name` - (optional) the name of the wallet will be created. If no value specified the default name `LIBVCX_SDK_WALLET` will be used.
* `wallet_key` - key or passphrase will be used to encrypt the created walled.
* `wallet_type` - (optional) type of the wallet to create. 
    If no value specified, the default Libindy `sqlite` wallet will be created. 
    If you want to use another custom wallet type you have to register it in Libindy using `indy_register_wallet_storage` function first.
* `wallet_key_derivation` - (optional) Algorithm to use derive wallet master key.
    If no value specified, `ARGON2I_MOD` will be used. 
    Algorithms:
    * `ARGON2I_MOD` - derive secured wallet master key based on value of `wallet_key` parameter (used by default)
    * `ARGON2I_INT` - derive secured wallet master key based on value of `wallet_key` parameter (less secured then `ARGON2I_MOD` but faster)
    * `RAW` - raw wallet master key provided by `wallet_key` parameter (skip derivation).
* `storage_config` - (optional) an addition configuration related to the wallet storage. 
    Storage type defines the set of supported keys. 
    For default Libindy storage type should be empty.
* `storage_credentials` - (optional) an addition credentials for wallet storage.
    Storage type defines the set of supported keys. 
    For default Libindy storage type should be empty.

#### Communication Protocol
* `protocol_type` - (optional) message protocol to use for communication with Agent and other Users. 
Here is the list of available protocol types:
    * "1.0"
        * use bundled messages for communication with Agent.
        * use auth/anon - cryptography functions for messages encoding.
        * use proprietary protocols for communication with other Users.
    * "2.0" 
        * use aries cross domain message format for communication with Agent.
        * use pack/unpack cryptography functions for messages encoding. 
        * use proprietary protocols for communication with other Users.
    * "3.0"
        * use aries cross domain message format for communication with Agent, 
        * use pack/unpack cryptography functions for messages encoding
        * use aries protocols for communication with other Users.
        * all functions return resulting values in the format defined in `1.0/2.0` protocols.
    * "4.0" - is not supported (the work is in progress)
        * use aries cross domain message format for communication with Agent, 
        * use pack/unpack cryptography functions for messages encoding
        * use aries protocols for communication with other Users.
        * all functions return resulting values in the `aries` format.

    If no value specified, `1.0` will be used. 

#### Outdated
Here are listed settings that became outdated and should not be used anymore.
These settings can be removed in next releases. 

* `use_latest_protocols` - (optional) flag to use `"protocol_type":"2.0"` protocol for connection establishment.

* `communication_method` - (optional) the version of protocols to use (can be `aries` or `proprietary`) for connection establishment and messages exchange.
    Please not that this option makes effect only if `2.0`
    The combination of settings `"protocol_type":"2.0" and "communication_method":"aries"` is equals to usage `"protocol_type":"3.0"`.
      
    * `aries` - the public protocols described in the [repository](https://github.com/hyperledger/aries-rfcs).
    * `proprietary` - the proprietary protocols.
          
#### Meta Options

All these options are optional and do not make any effect on provisioning step.
They can be omitted in provisioning config and set/changed later in the config used for library initialization. 

Please note that if values for those options are passed it will be just transmitted into resulting JSON that can be used for next library initialization. 

#### User info options
* `name` - (optional) name associated with the user. 
      It will be used as a label for connection/credential offer/proof request. 
      Please note that result JSON will contain a field with a different name `institution_name`. 
      This field will be filled even if the value for `name` was not passed.
      If no value specified, the `<CHANGE_ME>` value will be set.  
    
* `logo` - (optional) url containing a logo associated with the user.
      Please note that result JSON will contain a field with a different name `institution_logo_url`. 
      This field will be filled even if the value for `logo` was not passed.
      If no value specified, the `<CHANGE_ME>` value will be set.  

* `webhook_url` - (optional) an address to be used by provisioned Agent in order to send events notifications.

#### Pool related options
* `path` - (optional) path to the Ledger genesis transaction file to use.
      Please note that result JSON will contain a field with a different name `genesis_path`. 
      This field will be filled even if the value for `path` was not passed.
      If no value specified, the `<CHANGE_ME>` value will be set.  

* `pool_config` - (optional) runtime pool configuration json: 
```
    {
        "timeout": int (optional), timeout for network request (in sec).
        "extended_timeout": int (optional), extended timeout for network request (in sec).
        "preordered_nodes": array<string> -  (optional), names of nodes which will have a priority during request sending:
            ["name_of_1st_prior_node",  "name_of_2nd_prior_node", .... ]
            This can be useful if a user prefers querying specific nodes.
            Assume that `Node1` and `Node2` nodes reply faster.
            If you pass them Libindy always sends a read request to these nodes first and only then (if not enough) to others.
            Note: Nodes not specified will be placed randomly.
        "number_read_nodes": int (optional) - the number of nodes to send read requests (2 by default)
            By default Libindy sends a read requests to 2 nodes in the pool.
            If response isn't received or `state proof` is invalid Libindy sends the request again but to 2 (`number_read_nodes`) * 2 = 4 nodes and so far until completion.
    }
```

## Library Initialization Options
The most of options listed below coincide with options listed for Agent Provision.
There are also some options that can extend the JSON received during Agent Provisioning.

##### Common library related options
* `payment_method` - the name of payment method which was registered by a plugin.
    In the current state Mobile-Sdk depends on payment library even if payment related functionality is not used.
    That is why payment plugin must be registered independently before library initialization and 
    corresponding `payment_method` must be passed into config.

* `protocol_type` - (optional) message protocol to use for communication with Agent and other Users.

    Note: This value must be taken from the JSON received during Agent Provisioning.
    
    Here is the list of available protocol types:
    * "1.0"
        * use bundled messages for communication with Agent.
        * use auth/anon - cryptography functions for messages encoding.
        * use proprietary protocols for communication with other Users.
    * "2.0" 
        * use aries cross domain message format for communication with Agent.
        * use pack/unpack cryptography functions for messages encoding. 
        * use proprietary protocols for communication with other Users.
    * "3.0"
        * use aries cross domain message format for communication with Agent, 
        * use pack/unpack cryptography functions for messages encoding
        * use aries protocols for communication with other Users.
        * all functions return resulting values in the format defined in `1.0/2.0` protocols.
    * "4.0" - is not supported (the work is in progress)
        * use aries cross domain message format for communication with Agent, 
        * use pack/unpack cryptography functions for messages encoding
        * use aries protocols for communication with other Users.
        * all functions return resulting values in the `aries` format.
    
        If no value specified, `1.0` will be used. 
    
* `author_agreement` - (optional) accept and use transaction author agreement set on the Ledger. 
    Contains the following fields:
    * `acceptanceMechanismType` - (string) mechanism how user has accepted the TAA.
        (must be one of the keys taken from GET_TRANSACTION_AUTHOR_AGREEMENT_AML response['result']['data']['aml'] map).
    * `timeOfAcceptance` - (u64) UTC timestamp when user has accepted the TAA.
    * `text` and `version` - (string) text and version of TAA.
    * `taaDigest` - (string) sha256 hash calculated on concatenated strings: `version || text`.

    NOTE that either pair `text` `version` or `taaDigest` must be used 
    This TAA data will be appended for every write transaction sending to the ledger.

    Example: 
    ```
    ... other config fields
    ...
    ...
    "author_agreement": {"taaDigest": "string", "acceptanceMechanismType":"string", "timeOfAcceptance": u64},
    or
    "author_agreement": {"text": "string", version": "string", "acceptanceMechanismType":"string", "timeOfAcceptance": u64},
    or
    "author_agreement": "{\"taaDigest\": \"string\", \"acceptanceMechanismType\":\"string\", \"timeOfAcceptance\": u64}”,
    or
    "author_agreement": "{\"text\": \"string\", "version\": \"string\", \"acceptanceMechanismType\":\"string\", \"timeOfAcceptance\": u64}”,
    ```

* `did_method` - (optional) Method name to use to create fully qualified identifications.

    Note: This value must be taken from the JSON received during Agent Provisioning.

    If no value specified, `unqualified` DID form will be used. 

* `threadpool_size` - (optional) size of thread pool used for command execution (8 by default). 

* `actors` - the set of actors which application supports. This setting is used within the `Feature Discovery` protocol to discover which features are supported by another connection side.

    The following actors are supported by default: `[inviter, invitee, issuer, holder, prover, verifier, sender, receiver]`. 
    You need to edit this list and add to an initialization config in case the application supports the fewer number of actors.

    Note that option is applicable for `aries` communication method only.

##### Agency related options
The options below relate to the Agent configuration.
Their values must be received during Agent Provisioning step. 

* `agency_endpoint` - a public endpoint of agency to connect.
* `agency_did` - DID assigned to the agency.
* `agency_verkey` - Verkey associated with agency DID.
* `remote_to_sdk_did` - DID of Agent created on the Agency for the User.
* `remote_to_sdk_verkey` - Verkey associated with Agent DID.
* `sdk_to_remote_did` - User pairwise DID create for Agent.
* `sdk_to_remote_verkey` - Verkey associated with USer DID.


##### Wallet related options
The options below relate to the Wallet interaction.
The correspondent Wallet must be already created.
The values must be received during Agent Provisioning step or some other way to create a Wallet.

* `wallet_name` - (optional) the name of the wallet will be used. 
    If no value specified the default name `LIBVCX_SDK_WALLET` will be used.
* `wallet_key` - key or passphrase used for wallet creation.
* `wallet_type` - (optional) type of the wallet to create. 
    If no value specified, the default Libindy `sqlite` wallet will be created. 
    If you want to use another custom wallet type you have to register it in Libindy using `indy_register_wallet_storage` function first.
* `wallet_key_derivation` - (optional) Algorithm to use derive wallet master key.
    If no value specified, `ARGON2I_MOD` will be used. 
    Algorithms:
    * `ARGON2I_MOD` - derive secured wallet master key based on value of `wallet_key` parameter (used by default)
    * `ARGON2I_INT` - derive secured wallet master key based on value of `wallet_key` parameter (less secured then `ARGON2I_MOD` but faster)
    * `RAW` - raw wallet master key provided by `wallet_key` parameter (skip derivation).
* `storage_config` - (optional) an addition configuration related to the wallet storage. 
    Storage type defines the set of supported keys. 
    For default Libindy storage type should be empty.
* `storage_credentials` - (optional) an addition credentials for wallet storage.
    Storage type defines the set of supported keys. 
    For default Libindy storage type should be empty.
* `wallet_handle` - handle to the already opened wallet to use.

##### User info options
* `institution_name` - (optional) name associated with the user. 
      It will be used as a label for connection/credential offer/proof request. 
* `institution_logo_url` - (optional) url containing a logo associated with the user.
* `webhook_url` - (optional) an address to be used by provisioned Agent in order to send events notifications.

##### Pool related options
These options describe Pool Ledger to connect.

* `pool_name` - (optional) name of the pool ledger configuration will be created.
      If no value specified, the default pool name `pool_name` will be used.  

* `genesis_path` - path to the Ledger genesis transaction file to use.

* `pool_config` - (optional) runtime pool configuration json: 
```
    {
        "timeout": int (optional), timeout for network request (in sec).
        "extended_timeout": int (optional), extended timeout for network request (in sec).
        "preordered_nodes": array<string> -  (optional), names of nodes which will have a priority during request sending:
            ["name_of_1st_prior_node",  "name_of_2nd_prior_node", .... ]
            This can be useful if a user prefers querying specific nodes.
            Assume that `Node1` and `Node2` nodes reply faster.
            If you pass them Libindy always sends a read request to these nodes first and only then (if not enough) to others.
            Note: Nodes not specified will be placed randomly.
        "number_read_nodes": int (optional) - the number of nodes to send read requests (2 by default)
            By default Libindy sends a read requests to 2 nodes in the pool.
            If response isn't received or `state proof` is invalid Libindy sends the request again but to 2 (`number_read_nodes`) * 2 = 4 nodes and so far until completion.
    }
```

##### Outdated
Here are listed settings that became outdated and should not be used anymore.
These settings can be removed in next releases. 

* `use_latest_protocols` - (optional) flag to use `"protocol_type":"2.0"` protocol for connection establishment.

* `communication_method` - (optional) the version of protocols to use (can be `aries` or `proprietary`) for connection establishment and messages exchange.
    Please not that this option makes effect only if `2.0`
    The combination of settings `"protocol_type":"2.0" and "communication_method":"aries"` is equals to usage `"protocol_type":"3.0"`.
      
    * `aries` - the public protocols described in the [repository](https://github.com/hyperledger/aries-rfcs).
    * `proprietary` - the proprietary protocols.
