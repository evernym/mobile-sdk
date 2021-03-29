## Aries Out-of-Band

Aries RFCs provides [Out-of-Band](https://github.com/hyperledger/aries-rfcs/tree/master/features/0434-outofband) protocol describing the way of creating new connections and reusing of existing.
Futheremore Out-of-Band allows to attach an additional action which should be started right after connection completion.

This document contains guidelines on how to handle all possible cases for Out-of-Band invitation using Mobile-SDK.

On scanned Out-of-Band Invitation application should check whether connection already exists or not.
Connection exists if one of the following conditions resolve for one of the existing connections:
* `@id` field of original invitation is the same - means that we scan exactly the same invitation JSON.
* `public_did` field of original invitation is the same - means that we scan invitation from same Inviter
* `recipient_keys[0]` field of original invitation is the same - means that we scan invitation from same Inviter
 
  The next steps depend on connection existence and the Invitation format. Specifically on values of `handshake_protocols` and `request~attach` fields.
  
<table>
  <tr>  
    <th>`handshake_protocols` Present?</th>
    <th>`request~attach` Present?</th>
    <th>Connection exists?</th>
    <th>Actions</th>
  </tr>
  <tr>
    <td>NO</td>
    <td>NO</td>
    <td>NO</td>
    <td>
    
1. Invalid Invitation format. Throw error.
    </td>
  </tr>
  <tr>
    <td>NO</td>
    <td>NO</td>
    <td>YES</td>
    <td>

1. Invalid Invitation format. Throw error.
    </td>
  </tr>
  <tr>
    <td>YES</td>
    <td>NO</td>
    <td>NO</td>
    <td>
    
1. Call ConnectionApi.vcxCreateConnectionWithOutofbandInvite function to accept Invitation and create Connection state object. <br>
2. Complete Connection with regular steps.
    1. ConnectionApi.vcxConnectionConnect to start connection
    1. Wait until complete: int state = ConnectionApi.vcxConnectionUpdateState in a loop until state != 4

  </tr>
  <tr>
    <td>NO</td>
    <td>YES</td>
    <td>NO</td>
    <td>
    
1. Call ConnectionApi.vcxCreateConnectionWithOutofbandInvite function to accept Invitation and create Connection state object. 
2. Connection will be immediately created in the completed state - 4.<br>
3. Extract the original message from invitation `request~attach` field:
    * base64decode(invitation['request~attach'][0]['data']['base64'])
    * invitation['request~attach'][0]['data']['json']
4. Start protocol related to the extracted message:
    * proof request - DisclosedProofApi.proofCreateWithRequest
    * question - answer question
5. Complete protocol using connection created on step 1.
    </td>
  </tr>
  <tr>
    <td>YES</td>
    <td>YES</td>
    <td>NO</td>
    <td>

1. Call ConnectionApi.vcxCreateConnectionWithOutofbandInvite function to accept Invitation and create Connection state object. <br>
2. Complete Connection with regular steps.
    1. ConnectionApi.vcxConnectionConnect to start connection
    1. Wait until complete: int state = ConnectionApi.vcxConnectionUpdateState in a loop until state != 4
3. Extract the original message from invitation `request~attach` field:
    * base64decode(invitation['request~attach'][0]['data']['base64'])
    * invitation['request~attach'][0]['data']['json']
4. Start protocol related to the extracted message:
    * credential offer - CredentialApi.credentialCreateWithOffer ...
    * proof request - DisclosedProofApi.proofCreateWithRequest ...
    * question - answer question ...
5. Complete protocol using connection created on step 1.
    </td>
  </tr>
  <tr>
    <td>YES</td>
    <td>NO</td>
    <td>YES</td>
    <td>
    
1. Reuse existing connection. 
    1. call ConnectionApi.connectionSendReuse using existing connection.
    2. wait until `handshake-reuse-accepted` message is received.
        * UtilsApi.vcxGetMessages to download message
        * find message by thread['@thid']
    </td>
  </tr>
  <tr>
    <td>NO</td>
    <td>YES</td>
    <td>YES</td>
    <td>
    
1. Extract the original message from invitation `request~attach` field:
    * base64decode(invitation['request~attach'][0]['data']['base64'])
    * invitation['request~attach'][0]['data']['json']
2. Start protocol related to the extracted message:
    * credential offer - CredentialApi.credentialCreateWithOffer ...
    * proof request - DisclosedProofApi.proofCreateWithRequest
    * question - answer question
3. Complete protocol using existing connection.
    </td>
  </tr>
  <tr>
    <td>YES</td>
    <td>YES</td>
    <td>YES</td>
    <td>
    
1. Reuse existing connection. 
    1. call ConnectionApi.connectionSendReuse using existing connection.
    2. wait until `handshake-reuse-accepted` message is received.
        * UtilsApi.vcxGetMessages to download message
        * find message by thread['@thid']
2. Extract the original message from invitation `request~attach` field:
    * base64decode(invitation['request~attach'][0]['data']['base64'])
    * invitation['request~attach'][0]['data']['json']
3. Start protocol related to the extracted message:
    * credential offer - CredentialApi.credentialCreateWithOffer ...
    * proof request - DisclosedProofApi.proofCreateWithRequest
    * question - answer question
4. Complete protocol using existing connection.
    </td>
  </tr>
</table>