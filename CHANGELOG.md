# Changelog

# Release notes - MSDK 1.4.1 - Mar 22 2021

## Bugs
* [MSDK-398] - Provisioning with token does not work after its enforcing (disabling of old provisioning protocol) on the Agency side.

# Release notes - MSDK 1.4.0 - Nov 24 2020

## Bugs
* [MSDK-308] - Fixed deadlock that may have happened during high load.
* Updated VCX Aries message threading handling to use @id of the first message as thread id (thid) only if that first message doesn't contain thid.

## Features
* [MSDK-186] Added support for Android x86 device (armeabi-v7a architecture).
* [MSDK-315] Changed **get_provision_token** function to return token as JSON string value instead of void.
* [MP-73] Supported Aries Invite Action Protocol:
    * Added **vcx_connection_send_invite_action** (Android - **ConnectionApi.connectionSendInviteAction** / iOS - **connectionSendInviteAction**) function which prepares and sends a message to invite another side to take a particular action.

# Release notes - MSDK 1.3.0 - Sep 3 2020

## Change
* [MSDK-181] **Aries only**: Changed the logic for updating the status of the messages on the Agency:
    * **vcx_*_update_state** - still update messages state on agency internally.
    * **vcx_*_update_state_with_message** - caller has full control, passes messages, and is also responsible to update states in agency.
* Updated signature of Java API functions to return null (Void type) value for functions that actually do not return any result.
    * Consequences: the combination of exceptionally/thenAccept function to handle results may treat null as an error.
    * Tip: Use **whenComplete** function to proper handling instead of combination **exceptionally/thenAccept**.

## Bugs
* [MSDK-191] Updated building of DIDDoc to set id field according to W3C RFC.
* [MSDK-193] Connection handles in Aries state machines can't be serialized properly.
    * Overview: Aries Issuance and Presentation state machines held connection_handle as property.
But actual Connection object matching to handle will be destroyed once the library is unloaded.
That will break Aries state machines.
    * Change:  Updated Aries Issuance and Presentation state machines to embed required connection-related data.
    * Consequences: Deserialization for Aries Issuance and Presentation state machines in the intermediate state is broken but will work for Started and Finished.
* Added check that Credential Offer attributes fully match to Credential Definition. Partially filled credentials cannot be issued.
* Fixed custom logger to generate only logs with a set level.
* Corrected **vcx_download_agent_messages** (Android - **UtilsApi.vcxGetMessages** / iOS - **downloadMessages**) function to set **msg_ref_id** field for downloaded messages (Proprietary protocol).

## Features
* [MSDK-257] Added **vcx_credential_reject** (Android - **CredentialApi.credentialReject** / iOS - **credentialReject**) function to explicitly reject Aries Credential Offer by sending a ProblemReport message.
* [CM-2691] Added **vcx_delete_credential** (Android - **CredentialApi.deleteCredential** / iOS - **deleteCredential**) function to delete Credential from the wallet.
* [CM-2656] Supported Aries Question Answer Protocol: Added **vcx_connection_send_answer** (Android - **ConnectionApi.connectionSendAnswer** / iOS - **connectionSendAnswer**) which prepares and sends the answer on the received question.
* [MSDK-196] Partial support of Out-of-Band Aries protocol:
    * Sender - Added `vcx_connection_create_outofband` function which prepares Connection object containing Out-of-Band invitation.
               The parameter `handshake` specifies whether the Sender wants to establish a regular connection using connections handshake protocol or create a public channel.
               Next when you called `vcx_connection_connect` Connection state machine either goes by regular steps or transit to Accepted state when no handshake requested.

    * Received - Added `vcx_connection_create_with_outofband_invitation` function which accepts Out-of-Band invitation.
                 If invitation contains `handshake_protocols` connection goes regular flow else transits to Completed state.
    * HandshakeReuse - Added `vcx_connection_send_reuse` function to send HandshakeReuse message.
    * request~attach:
        * Sender - It can be set into Out-of-Band invitation but VCX Issuance and Presentation state machines are not compatible with that protocol.
        * Receiver - User should start attached process once Connection is established.
* [MSDK-166] Add a helper function **vcx_agency_download_message** (Android - **UtilsApi.vcxGetMessage** / iOS - **downloadMessage**) to download a single message from the Agency by the given id.
* [MSDK-189] Updated handling of **~thread** decorator for Aries messages to track and set **sender_order** and **received_orders** fields.
* Adopted Aries **Problem Report** message for **issue-credential** and **present-proof** protocols.
    Previous versions send general **Problem Report** messages from **notification** message family in case an error occurred in Issuance and Presentation protocols.
    This version sets appropriate **issue-credential/present-proof** message family while sending **Problem Report** message.
* [CM-2672] Added separate function for Pool initialization.
You can deffer connecting to the Pool Ledger during library initialization(to decrease the time taken) by omitting **genesis_path** field in the config JSON.
Next, you need to use **vcx_init_pool** (Android - **VcxApi.vcxInitPool** / iOS - **initPool**) function (for instance as a background task) to perform a connection to the Pool Ledger.
* [CM-2685] Added helper function **vcx_fetch_public_entities** (Android - **UtilsApi.vcxFetchPublicEntities** / iOS - **fetchPublicEntities**) to fetch public entities from the ledger.
This function performs two steps:
    1) Retrieves the list of all credentials stored in the opened wallet.
    2) Fetch and cache Schemas / Credential Definitions / Revocation Registry Definitions correspondent to received credentials from the connected Ledger.
This helper function can be used, for instance as a background task, to refresh library cache.
* [MSDK-177] Updated library to make payment plugin optional dependency. You can remove payment method setting from configuration JSON if you are not going to use payments.

# Release notes - MSDK 1.2.0 - Aug 30 2020

## Bugs
* [MSDK-257] Significantly improved performance of the library by updating Object Cache to use concurrent map instead of the blocking variant.

## Features
* [MSDK-261] Updated the **connection_options** parameter passed into **vcx_connection_connect** (Android - **ConnectionApivcxConnectionConnect** / iOS - **connectionConnect**) function to allow an optional **update_agent_info:bool** field be specified, that can be used to skip updating of the agent information during the connection establishemnt.

# Release notes - MSDK 1.1.0 - Jul 28 2020

## Features

* [MSDK-212] Updated the config parameter passed into **vcx_agent_update_info** (Android - **UtilsApi.vcxUpdateAgentInfo** /  iOS - **agentUpdateInfo**) function to allow an optional type field be specified, that can be used to distinguish between different classes of push notifications (use `4` for forwarding)

# Release notes - MSDK 1.0.1 - May 22 2020

## Bugs
* Proof reject is not working

# Release notes - MSDK 1.0.0 - Apr 01 2020

**Initial release**

## Features
* Connections - encrypted communication channels between two parties used for secure messages exchange.
* Verifiable Credentials - digital credentials that can be used to prove yourself.
* Proofs - digital evidence revealing information about a credential holder.
* Structured Messages / Questions - request to provided one of the predefined answers from the specified question.
* Connection Redirection - way if reusing existing connections instead of creating a new.
* Initial support of [Aries](https://github.com/hyperledger/aries-rfcs) protocols.