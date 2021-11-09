# Configuration

This document contains a list of all options that can be used in the library configuration JSON.

#### Agency related options
The options below are used to create a user-associated Agent on the Agency. This agent is need to exchange messages with other users.

* `agency_endpoint` - a public endpoint of agency to connect.
* `agency_did` - DID assigned to the agency.
* `agency_verkey` - Verkey associated with agency DID.
* `enterprise_seed` - (optional) Seed to use to create DID/Verkey associated with user.
    You may need to pass this seed when you want to use a specific DID/Verkey pair that is registered in the Ledger Network.
    If no value specified, a random pair will be generated.

The input config will be also populated with the fields below after an Agent Provisioning step:  

* `remote_to_sdk_did` - DID of Agent created on the Agency for the User.
* `remote_to_sdk_verkey` - Verkey associated with Agent DID.
* `sdk_to_remote_did` - User pairwise DID create for Agent.
* `sdk_to_remote_verkey` - Verkey associated with USer DID.

#### Wallet related options
The options below are used to create a secure wallet which will be used to store private data.

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
    * "4.0" - is not supported (recommended)
        * use aries cross domain message format for communication with Agent, 
        * use pack/unpack cryptography functions for messages encoding
        * use aries protocols for communication with other Users.
        * all functions return resulting values in the `aries` format.

    > **NOTE**: If no value specified, `3.0` will be used as the default. 
  
#### User specific information options

* `institution_name` - (optional) name associated with the user. 
      It will be used as a label for connection/credential offer/proof request. 
      Please note that result JSON will contain a field with a different name `institution_name`. 
      This field will be filled even if the value for `name` was not passed.
      If no value specified, the `<CHANGE_ME>` value will be set.  
    
* `institution_logo_url` - (optional) url containing a logo associated with the user.
      Please note that result JSON will contain a field with a different name `institution_logo_url`. 
      This field will be filled even if the value for `logo` was not passed.
      If no value specified, the `<CHANGE_ME>` value will be set.  

#### Pool Ledger related options

* `genesis_path` - (optional) path to the Ledger genesis transaction file to use.
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

* `pool_networks` - (optional) a list of pool ledger networks to connect. It provides an ability to connect to multiple ledger networks at the same time. In this case, library will query each of them while getting public information (schema/cred_def). **NOTE** that this options must be used instead of top-level `genesis_path` field.
  ```
        [
            { pool_ledger_config }
        ]
    ```
  where `pool_ledger_config` can be one of the following formats:
    * explicit - pass path to Ledger genesis transaction file:
    ```
        {
            'genesis_path': string, // path to the Ledger genesis transaction file to use (see above)
            'pool_config': string, // (optional) runtime pool configuration json (see above)
            'pool_name': 'alice_indy' // (optional) name of the pool ledger configuration will be created (see above)
        }
    ```
    * predefined - use one of predefined Sovrin Pool Ledgers:
        * `production` - Sovrin Live Net
        * `staging` - Sovrin Staging Net
        ```
            {
                `pool_network_alias`: string, // one of predefinded aliaes
                'pool_config': string, // (optional) runtime pool configuration json (see above)
                'pool_name': 'alice_indy' // (optional) name of the pool ledger configuration will be created (see above)
            }
        ```

  Example:
    ```
        [
            {
                'genesis_path': 'docker.txn',
                'pool_name': 'indy_test'
            },
            {
                'pool_network_alias': 'production',
                'pool_name': 'indy_production'
            }
        ]
    ```

#### Outdated
Here are listed settings that became outdated and should not be used anymore.
These settings can be removed in next releases.

* `use_latest_protocols` - (optional) flag to use `"protocol_type":"2.0"` protocol for connection establishment.

* `communication_method` - (optional) the version of protocols to use (can be `aries` or `proprietary`) for connection establishment and messages exchange.
  Please not that this option makes effect only if `2.0`
  The combination of settings `"protocol_type":"2.0" and "communication_method":"aries"` is equals to usage `"protocol_type":"3.0"`.

    * `aries` - the public protocols described in the [repository](https://github.com/hyperledger/aries-rfcs).
    * `proprietary` - the proprietary protocols.

### Config Sample

```json
{
  // These fields are used for agency configuration
  "agency_endpoint": "http://agency.pps.evernym.com", // URL of agency to use
  "agency_did": "3mbwr7i85JNSL3LoNQecaW", // DID of agency
  "agency_verkey": "2WXxo6y1FJvXWgZnoYUP5BJej2mceFrqBDNPE3p6HDPf", // Verification key of the agency

  //These fields are used for wallet configuration
  "wallet_name": "wallet-name-wwwww-wallet",  // Name of the wallet
  "wallet_key": "viM/BUU7I+Ypn+AdXAIQUAGX59pteVzau7Z7Jv3Ll6nzmYsSHrFqRdT71tjoMhTPRM2uSnqt8tDTSOLMP1KVf0fl1uP/dPsWu7cjucMsqfK8ohb92amhAWnNn+8s8UWC5owLN3EXZuilqYtjtRZtRUm/hhK5ycQ/OuxMgNPpfUQ=", // Name of the wallet

  // Communication Protocol
  "protocol_type": "3.0", // Type of the protocol

  // Pool Ledger
  "pool_networks": [
    {
      'genesis_path': '/data/user/0/me.connect.sdk.java.sample/files/connectMeVcx/pool_transactions_genesis',
      'pool_name': 'indy_test'
    }
    // you can use several objects here in order to connect to multile pool ledgers
  ],

  // User Meta
  "institution_logo_url": "https://robothash.com/logo.png", // url leading to image
  "institution_name": "real institution name" // name to use
}
```
