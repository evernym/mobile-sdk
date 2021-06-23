package me.connect.sdk.java.samplekt.homepage

import kotlinx.coroutines.future.await
import me.connect.sdk.java.Connections
import me.connect.sdk.java.ConnectionsUtils
import me.connect.sdk.java.OutOfBandHelper
import me.connect.sdk.java.Utils
import me.connect.sdk.java.connection.QRConnection
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Action
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.homepage.Results.*
import me.connect.sdk.java.samplekt.wrap
import org.json.JSONException
import org.json.JSONObject

object StateConnections {
    suspend fun createConnection(action: Action, db: Database, liveData: SingleLiveData<Results>) {
        try {
            val parsedInvite: String? = ConnectionsUtils.parseInvite(action.invite)
            val invitationType = Connections.getInvitationType(parsedInvite)
            val userMeta: ConnectionsUtils.ConnDataHolder? = ConnectionsUtils.extractUserMetaFromInvite(parsedInvite)
            val serializedConns = db.connectionDao().getAllSerializedConnections()
            val existingConnection = parsedInvite?.let { Connections.verifyConnectionExists(it, serializedConns) }
            if (ConnectionsUtils.isProprietaryType(invitationType)) {
                if (existingConnection != null) {
                    Connections.connectionRedirectProprietary(action.invite, existingConnection)
                    liveData.postValue(CONNECTION_REDIRECT)
                } else {
                    connectionCreate(parsedInvite!!, db, userMeta!!, liveData)
                }
                return
            }
            if (ConnectionsUtils.isAriesConnection(invitationType)) {
                if (existingConnection != null) {
                    liveData.postValue(CONNECTION_REDIRECT)
                    return
                } else {
                    connectionCreate(parsedInvite!!, db, userMeta!!, liveData)
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
                        )
                        liveData.postValue(CONNECTION_REDIRECT)
                    } else {
                        connectionCreate(parsedInvite!!, db, userMeta!!, liveData)
                    }
                    return
                }
                if (parsedInvite != null) {
                    processAttachment(
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

    private suspend fun processAttachment(
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
                processCredentialAttachment(outOfBandInvite, db, liveData, action)
                return
            }
            if (ConnectionsUtils.isProofInviteType(attachType)) {
                processProofAttachment(outOfBandInvite, db, liveData, action)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private suspend fun processCredentialAttachment(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            Connections.connectionRedirectAriesOutOfBand(outOfBandInvite.parsedInvite, outOfBandInvite.existingConnection)
            StateCredentialOffers.createCredentialStateObjectForExistingConnection(outOfBandInvite, db, liveData, action)
            liveData.postValue(CONNECTION_REDIRECT)
            return
        }
        StateCredentialOffers.createCredentialStateObject(db, outOfBandInvite, liveData, action)
    }

    private suspend fun processProofAttachment(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            StateProofRequests.createProofStateObjectForExistingConnection(db, outOfBandInvite, liveData, action)
            liveData.postValue(CONNECTION_REDIRECT)
            return
        }
        StateProofRequests.createProofStateObject(db, outOfBandInvite, liveData, action)
    }

    private suspend fun connectionCreate(
        parsedInvite: String,
        db: Database,
        data: ConnectionsUtils.ConnDataHolder,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val co = Connections.create(parsedInvite, QRConnection()).wrap().await()
            val pwDid = Connections.getPwDid(co)
            val serializedCon = Connections.awaitConnectionReceived(co, pwDid)
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