package me.connect.sdk.java.samplekt.connections

import android.app.Application
import android.net.Uri
import android.util.Base64
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.connect.sdk.java.*
import me.connect.sdk.java.connection.QRConnection
import me.connect.sdk.java.message.MessageState
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.connections.ConnectionCreateResult.*
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer
import me.connect.sdk.java.samplekt.db.entity.ProofRequest
import me.connect.sdk.java.samplekt.wrap
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class ConnectionsViewModel(application: Application) : AndroidViewModel(application) {
    private val db: Database = Database.getInstance(application)
    private val connectionsLiveData: LiveData<List<Connection>> by lazy {
        db.connectionDao().getAll()
    }

    fun getConnections(): LiveData<List<Connection>> = connectionsLiveData


    fun newConnection(invite: String): SingleLiveData<ConnectionCreateResult> {
        val data = SingleLiveData<ConnectionCreateResult>()
        createConnection(invite, data)
        return data
    }


    private fun createConnection(invite: String, liveData: SingleLiveData<ConnectionCreateResult>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val parsedInvite: String? = ConnectionsUtils.parseInvite(invite)
            val invitationType = Connections.getInvitationType(parsedInvite)
            val userMeta: ConnectionsUtils.ConnDataHolder? = ConnectionsUtils.extractUserMetaFromInvite(parsedInvite)
            val serializedConns = db.connectionDao().getAllSerializedConnections()
            val existingConnection =
                parsedInvite?.let { Connections.verifyConnectionExists(it, serializedConns) }
            if (ConnectionsUtils.isProprietaryType(invitationType)) {
                if (existingConnection != null) {
                    Connections.connectionRedirectProprietary(invite, existingConnection)
                    liveData.postValue(REDIRECT)
                } else {
                    connectionCreate(parsedInvite, userMeta!!, liveData)
                }
                return@launch
            }
            if (ConnectionsUtils.isAriesConnection(invitationType)) {
                if (existingConnection != null) {
                    liveData.postValue(REDIRECT)
                    return@launch
                } else {
                    connectionCreate(parsedInvite, userMeta!!, liveData)
                }
                return@launch
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
                        liveData.postValue(REDIRECT)
                    } else {
                        connectionCreate(parsedInvite, userMeta!!, liveData)
                    }
                    return@launch
                }
                if (parsedInvite != null) {
                    processAttachment(
                        parsedInvite,
                        extractedAttachRequest,
                        attachRequestObject,
                        existingConnection,
                        userMeta!!,
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
        parsedInvite: String,
        extractedAttachRequest: String?,
        attachRequestObject: JSONObject,
        existingConnection: String?,
        userMeta: ConnectionsUtils.ConnDataHolder,
        liveData: SingleLiveData<ConnectionCreateResult>
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
                processCredentialAttachment(outOfBandInvite, liveData)
                return
            }
            if (ConnectionsUtils.isProofInviteType(attachType)) {
                processProofAttachment(outOfBandInvite, liveData)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private suspend fun processCredentialAttachment(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<ConnectionCreateResult>
        ) {
        if (outOfBandInvite.existingConnection != null) {
            Connections.connectionRedirectAriesOutOfBand(outOfBandInvite.parsedInvite, outOfBandInvite.existingConnection)
            createCredentialStateObjectForExistingConnection(outOfBandInvite, liveData)
            liveData.postValue(REDIRECT)
            return
        }
        createCredentialStateObject(outOfBandInvite, liveData)
    }

    private suspend fun processProofAttachment(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<ConnectionCreateResult>
        ) {
        if (outOfBandInvite.existingConnection != null) {
            createProofStateObjectForExistingConnection(outOfBandInvite, liveData)
            liveData.postValue(REDIRECT)
            return
        }
        createProofStateObject(outOfBandInvite, liveData)
    }

    private suspend fun createCredentialStateObjectForExistingConnection(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<ConnectionCreateResult>
    ) {
        try {
            val connection: JSONObject = Utils.convertToJSONObject(outOfBandInvite.existingConnection)!!
            val connectionData = connection.getJSONObject("data")
            val co = Credentials.createWithOffer(
                UUID.randomUUID().toString(),
                outOfBandInvite.extractedAttachRequest
            ).wrap().await()
            val preview: JSONObject =
                outOfBandInvite.attach.getJSONObject("credential_preview")
            val offer = CredentialOffer(
                claimId = outOfBandInvite.attach.getString("@id"),
                name = outOfBandInvite.attach.getString("comment"),
                pwDid = connectionData.getString("pw_did"),
                attributes = preview.getJSONArray("attributes").getString(0),
                serialized = co,
                attachConnectionLogo = JSONObject(outOfBandInvite.parsedInvite).getString("profileUrl")
            )
            db.credentialOffersDao().insertAll(offer)
            liveData.postValue(REQUEST_ATTACH)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private suspend fun createCredentialStateObject(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<ConnectionCreateResult>
    ) {
        val co = Credentials.createWithOffer(
            UUID.randomUUID().toString(),
            outOfBandInvite.extractedAttachRequest
        ).wrap().await()
        val preview: JSONObject =
            outOfBandInvite.attach.getJSONObject("credential_preview")
        val offer = CredentialOffer(
            claimId = outOfBandInvite.attach.getString("@id"),
            name = outOfBandInvite.attach.getString("comment"),
            attributes = preview.getJSONArray("attributes").getString(0),
            serialized = co,
            attachConnection = outOfBandInvite.parsedInvite,
            attachConnectionName = outOfBandInvite.userMeta?.name,
            attachConnectionLogo = outOfBandInvite.userMeta?.logo
        )
        db.credentialOffersDao().insertAll(offer)
        liveData.postValue(REQUEST_ATTACH)
    }

    private suspend fun createProofStateObjectForExistingConnection(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<ConnectionCreateResult>
    ) {
        try {
            val connection = Utils.convertToJSONObject(outOfBandInvite.existingConnection)!!
            val connectionData = connection.getJSONObject("data")
            val pr = Proofs.createWithRequest(
                UUID.randomUUID().toString(),
                outOfBandInvite.extractedAttachRequest
            ).wrap().await()
            val decodedProofAttach = ProofRequests.decodeProofRequestAttach(outOfBandInvite.attach)
            val thread = outOfBandInvite.attach.getJSONObject("~thread")
            val name = ProofRequests.extractRequestedNameFromProofRequest(decodedProofAttach)
            val attr = ProofRequests.extractRequestedAttributesFromProofRequest(decodedProofAttach)
            if (name != null && attr != null) {
                val proof = ProofRequest(
                    serialized = pr,
                    name = name,
                    pwDid = connectionData.getString("pw_did"),
                    attributes = attr,
                    threadId = thread.getString("thid"),
                    attachConnectionLogo = JSONObject(outOfBandInvite.parsedInvite)
                        .getString("profileUrl")
                )
                db.proofRequestDao().insertAll(proof)
                liveData.postValue(PROOF_ATTACH)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private suspend fun createProofStateObject(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<ConnectionCreateResult>
    ) {
        val pr = Proofs.createWithRequest(
            UUID.randomUUID().toString(),
            outOfBandInvite.extractedAttachRequest
        ).wrap().await()
        val decodedProofAttach = ProofRequests.decodeProofRequestAttach(outOfBandInvite.attach)
        val thread: JSONObject = outOfBandInvite.attach.getJSONObject("~thread")
        val name = ProofRequests.extractRequestedNameFromProofRequest(decodedProofAttach)
        val attr = ProofRequests.extractRequestedAttributesFromProofRequest(decodedProofAttach)
        if (name != null && attr != null) {
            val proof = ProofRequest(
                serialized = pr,
                name = name,
                attributes = attr,
                threadId = thread.getString("thid"),
                attachConnection = outOfBandInvite.parsedInvite,
                attachConnectionName = outOfBandInvite.userMeta?.name,
                attachConnectionLogo = outOfBandInvite.userMeta?.logo
            )
            db.proofRequestDao().insertAll(proof)
            liveData.postValue(PROOF_ATTACH)
        }
    }


    private suspend fun connectionCreate(
        parsedInvite: String?,
        data: ConnectionsUtils.ConnDataHolder,
        liveData: SingleLiveData<ConnectionCreateResult>
    ) {
        try {
            val co = Connections.create(parsedInvite!!, QRConnection()).wrap().await()
            val serializedCon =
                Connections.awaitStatusChange(co, MessageState.ACCEPTED)
            val pwDid = Connections.getPwDid(serializedCon)
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
