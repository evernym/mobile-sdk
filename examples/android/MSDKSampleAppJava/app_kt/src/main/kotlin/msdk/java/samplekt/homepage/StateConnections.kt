package msdk.java.samplekt.homepage

import kotlinx.coroutines.future.await
import msdk.java.messages.ConnectionInvitation
import msdk.java.handlers.Connections
import msdk.java.messages.OutOfBandInvitation
import msdk.java.samplekt.SingleLiveData
import msdk.java.samplekt.db.Database
import msdk.java.samplekt.db.entity.Action
import msdk.java.samplekt.db.entity.Connection
import msdk.java.samplekt.homepage.Results.*
import msdk.java.samplekt.wrap
import msdk.java.types.MessageAttachment

object StateConnections {
    suspend fun handleConnectionInvitation(action: Action, db: Database, liveData: SingleLiveData<Results>) {
        try {
            // 1. Get invitation data, type, and metadata to show on UI

            // 1. Get invitation data, type, and metadata to show on UI
            val invitation = ConnectionInvitation.getConnectionInvitationFromData(action.invite)
            val invitationType = ConnectionInvitation.getInvitationType(invitation)
            val userMeta = ConnectionInvitation.extractUserMetaFromInvitation(invitation)

            // 2. Get existing invitations and check if we already has correspondent connection
            val serializedConnections = db.connectionDao().getAllSerializedConnections()
            val existingConnection = msdk.java.handlers.Connections.verifyConnectionExists(invitation, serializedConnections)

            // 3. Handle Aries Connection Invitation

            // 3. Handle Aries Connection Invitation
            if (ConnectionInvitation.isAriesConnectionInvitation(invitationType)) {
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
            if (ConnectionInvitation.isAriesOutOfBandConnectionInvitation(invitationType)) {
                val attachment = MessageAttachment.parse(invitation)
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
            attachment: MessageAttachment,
            existingConnection: String?,
            userMeta: Connections.ConnectionMetadata,
            action: Action,
            liveData: SingleLiveData<Results>
    ) {
        val outOfBandInvite = OutOfBandInvitation.builder()
                .withInvitation(parsedInvite)
                .withAttachment(attachment.data)
                .withExistingConnection(existingConnection)
                .withUserMeta(userMeta)
                .build()

        if (attachment.isCredentialAttachment()) {
            // handle invitation with attached credential offer
            processInvitationWithCredentialAttachment(outOfBandInvite, db, liveData, action)
        }
        if (attachment.isProofAttachment()) {
            // handle invitation with attached proof request
            processInvitationWithProofAttachment(outOfBandInvite, db, liveData, action)
        }
    }

    private suspend fun processInvitationWithCredentialAttachment(
        outOfBandInvite: OutOfBandInvitation,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            Connections.connectionRedirectAriesOutOfBand(outOfBandInvite.invitation, outOfBandInvite.existingConnection).wrap().await()
            liveData.postValue(CONNECTION_REDIRECT)
            StateCredentialOffers.createCredentialStateObject(db, outOfBandInvite, liveData, action)
        } else {
            StateCredentialOffers.createCredentialStateObject(db, outOfBandInvite, liveData, action)
        }
    }

    private suspend fun processInvitationWithProofAttachment(
        outOfBandInvite: OutOfBandInvitation,
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
            invitationType: ConnectionInvitation.InvitationType,
            db: Database,
            data: Connections.ConnectionMetadata,
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