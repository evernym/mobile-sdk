package me.connect.sdk.java.samplekt.homepage

import kotlinx.coroutines.future.await
import me.connect.sdk.java.ConnectionInvitations
import me.connect.sdk.java.Connections
import me.connect.sdk.java.ConnectionsUtils
import me.connect.sdk.java.OutOfBandHelper
import me.connect.sdk.java.OutOfBandHelper.OutOfBandInvite
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Action
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.homepage.Results.*
import me.connect.sdk.java.samplekt.wrap
import org.json.JSONException
import org.json.JSONObject

object StateConnections {
    suspend fun handleConnectionInvitation(action: Action, db: Database, liveData: SingleLiveData<Results>) {
        try {
            // 1. Get invitation data, type, and metadata to show on UI

            // 1. Get invitation data, type, and metadata to show on UI
            val invitation = ConnectionInvitations.getConnectionInvitationFromData(action.invite)
            val invitationType = ConnectionInvitations.getInvitationType(invitation)
            val userMeta = ConnectionInvitations.extractUserMetaFromInvitation(invitation)

            // 2. Get existing invitations and check if we already has correspondent connection
            val serializedConnections = db.connectionDao().getAllSerializedConnections()
            val existingConnection = Connections.verifyConnectionExists(invitation, serializedConnections)

            // 3. Handle Aries Connection Invitation

            // 3. Handle Aries Connection Invitation
            if (ConnectionInvitations.isAriesConnectionInvitation(invitationType)) {
                if (existingConnection != null) {
                    // duplicates - nothing to do
                    liveData.postValue(CONNECTION_REDIRECT)
                } else {
                    // create a new connection

                    // create a new connection
                    connectionCreate(action.id, invitation, invitationType, db, userMeta, liveData)
                }
                return
            }

            // 4. Handle Aries Out-Of-Band Connection Invitation
            if (ConnectionInvitations.isAriesOutOfBandConnectionInvitation(invitationType)) {
                val attachment: JSONObject? = OutOfBandHelper.extractRequestAttach(invitation)

                if (attachment == null) {
                    if (existingConnection != null) {
                        // reuse existing connection
                        Connections.connectionRedirectAriesOutOfBand(invitation, existingConnection).wrap().await()
                        liveData.postValue(CONNECTION_REDIRECT)
                    } else {
                        // create a new connection
                        connectionCreate(action.id, invitation, invitationType, db, userMeta, liveData)
                    }
                    return
                } else {
                    // handle invitation with attachment
                    handleOutOfBandConnectionInvitationWithAttachment(
                            db,
                            invitation,
                            attachment.toString(),
                            attachment,
                            existingConnection,
                            userMeta,
                            action,
                            liveData
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }

    private suspend fun handleOutOfBandConnectionInvitationWithAttachment(
        db: Database,
        parsedInvite: String,
        extractedAttachRequest: String?,
        attachRequestObject: JSONObject,
        existingConnection: String?,
        userMeta: ConnectionsUtils.ConnDataHolder,
        action: Action,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val outOfBandInvite = OutOfBandInvite.builder()
                    .withParsedInvite(parsedInvite)
                    .withExtractedAttachRequest(extractedAttachRequest)
                    .withAttach(attachRequestObject)
                    .withExistingConnection(existingConnection)
                    .withUserMeta(userMeta)
                    .build()

            val attachmentType = attachRequestObject.getString("@type")
            if (ConnectionInvitations.isCredentialAttachment(attachmentType)) {
                // handle invitation with attached credential offer
                processInvitationWithCredentialAttachment(outOfBandInvite, db, liveData, action)
            }
            if (ConnectionInvitations.isProofAttachment(attachmentType)) {
                // handle invitation with attached proof request
                processInvitationWithProofAttachment(outOfBandInvite, db, liveData, action)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private suspend fun processInvitationWithCredentialAttachment(
        outOfBandInvite: OutOfBandInvite,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            Connections.connectionRedirectAriesOutOfBand(outOfBandInvite.parsedInvite, outOfBandInvite.existingConnection).wrap().await()
            liveData.postValue(CONNECTION_REDIRECT)
            StateCredentialOffers.createCredentialStateObject(db, outOfBandInvite, liveData, action)
        } else {
            StateCredentialOffers.createCredentialStateObject(db, outOfBandInvite, liveData, action)
        }
    }

    private suspend fun processInvitationWithProofAttachment(
        outOfBandInvite: OutOfBandInvite,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            StateProofRequests.createProofStateObject(db, outOfBandInvite, liveData, action)
            liveData.postValue(CONNECTION_REDIRECT)
        } else{
            StateProofRequests.createProofStateObject(db, outOfBandInvite, liveData, action)
        }
    }

    private suspend fun connectionCreate(
        actionId: Int,
        parsedInvite: String,
        invitationType: ConnectionInvitations.InvitationType,
        db: Database,
        data: ConnectionsUtils.ConnDataHolder,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val co = Connections.create(parsedInvite, invitationType).wrap().await()
            val pwDid = Connections.getPwDid(co)
            val serializedCon = Connections.awaitConnectionCompleted(co, pwDid)
            val c = Connection(
                name = data.name,
                icon = data.logo,
                serialized = serializedCon,
                pwDid = pwDid
            )
            db.connectionDao().insertAll(c)
            liveData.postValue(SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }
}