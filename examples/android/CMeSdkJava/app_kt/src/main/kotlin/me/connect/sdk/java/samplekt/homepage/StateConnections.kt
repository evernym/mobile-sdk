package me.connect.sdk.java.samplekt.homepage

import kotlinx.coroutines.future.await
import me.connect.sdk.java.Connections
import me.connect.sdk.java.ConnectionsUtils
import me.connect.sdk.java.OutOfBandHelper
import me.connect.sdk.java.Utils
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
            val parsedInvite: String? = ConnectionsUtils.getInvitation(action.invite)
            val invitationType = Connections.getInvitationType(parsedInvite)
            val userMeta: ConnectionsUtils.ConnDataHolder? = ConnectionsUtils.extractUserMetaFromInvitation(parsedInvite)
            val serializedConns = db.connectionDao().getAllSerializedConnections()
            val existingConnection = parsedInvite?.let { Connections.verifyConnectionExists(it, serializedConns) }
            if (ConnectionsUtils.isProprietaryType(invitationType)) {
                if (existingConnection != null) {
                    Connections.connectionRedirectProprietary(action.invite, existingConnection)
                    liveData.postValue(CONNECTION_REDIRECT)
                } else {
                    connectionCreate(parsedInvite!!, invitationType, db, userMeta!!, liveData)
                }
                return
            }
            if (ConnectionsUtils.isAriesConnection(invitationType)) {
                if (existingConnection != null) {
                    liveData.postValue(CONNECTION_REDIRECT)
                    return
                } else {
                    connectionCreate(parsedInvite!!, invitationType, db, userMeta!!, liveData)
                }
                return
            }
            if (ConnectionsUtils.isOutOfBandType(invitationType)) {
                val extractedAttachRequest: String? = OutOfBandHelper.extractRequestAttach(parsedInvite)
                val attachRequestObject: JSONObject? = Utils.convertToJSONObject(extractedAttachRequest)
                if (attachRequestObject == null) {
                    if (existingConnection != null) {
                        Connections.connectionRedirectAriesOutOfBand(
                            parsedInvite,
                            existingConnection
                        ).wrap().await()
                        liveData.postValue(CONNECTION_REDIRECT)
                    } else {
                        connectionCreate(parsedInvite!!, invitationType, db, userMeta!!, liveData)
                    }
                    return
                }
                if (parsedInvite != null) {
                    processInvitationWithAttachment(
                        db,
                        parsedInvite,
                        extractedAttachRequest,
                        attachRequestObject,
                        existingConnection,
                        userMeta!!,
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

    private suspend fun processInvitationWithAttachment(
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
            val outOfBandInvite: OutOfBandHelper.OutOfBandInvite =
                OutOfBandHelper.OutOfBandInvite.builder()
                    .withParsedInvite(parsedInvite)
                    .withExtractedAttachRequest(extractedAttachRequest)
                    .withAttach(attachRequestObject)
                    .withExistingConnection(existingConnection)
                    .withUserMeta(userMeta)
                    .build()
            val attachType = attachRequestObject.getString("@type")
            if (ConnectionsUtils.isCredentialInviteType(attachType)) {
                processInvitationWithCredentialAttachment(outOfBandInvite, db, liveData, action)
                return
            }
            if (ConnectionsUtils.isProofInviteType(attachType)) {
                processInvitationWithProofAttachment(outOfBandInvite, db, liveData, action)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private suspend fun processInvitationWithCredentialAttachment(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            Connections.connectionRedirectAriesOutOfBand(
                    outOfBandInvite.parsedInvite,
                    outOfBandInvite.existingConnection
            ).wrap().await()
            liveData.postValue(CONNECTION_REDIRECT)
            StateCredentialOffers.createCredentialStateObject(db, outOfBandInvite, liveData, action)
        } else {
            StateCredentialOffers.createCredentialStateObject(db, outOfBandInvite, liveData, action)
        }
    }

    private suspend fun processInvitationWithProofAttachment(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            StateProofRequests.createProofStateObject(db, outOfBandInvite, liveData, action)
            liveData.postValue(CONNECTION_REDIRECT)
            return
        }
        StateProofRequests.createProofStateObject(db, outOfBandInvite, liveData, action)
    }

    private suspend fun connectionCreate(
        parsedInvite: String,
        invitationType: Connections.InvitationType,
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