package me.connect.sdk.java.samplekt.proofs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.connect.sdk.java.Connections
import me.connect.sdk.java.Messages
import me.connect.sdk.java.Proofs
import me.connect.sdk.java.connection.QRConnection
import me.connect.sdk.java.message.MessageState
import me.connect.sdk.java.message.MessageType
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.db.entity.ProofRequest
import me.connect.sdk.java.samplekt.messages.ProofDataHolder
import me.connect.sdk.java.samplekt.proofs.ProofCreateResult.*
import me.connect.sdk.java.samplekt.wrap
import java.util.*


class ProofRequestsViewModel(application: Application) : AndroidViewModel(application) {
    private val db: Database = Database.getInstance(application)
    private val proofRequestsLiveData by lazy {
        db.proofRequestDao().getAll()
    }

    fun getProofRequests(): LiveData<List<ProofRequest>> = proofRequestsLiveData

    fun acceptProofRequest(proofId: Int): SingleLiveData<ProofCreateResult> {
        val data = SingleLiveData<ProofCreateResult>()
        acceptProofReq(proofId, data)
        return data
    }

    private suspend fun acceptProofReqAndCreateConnection(
        proof: ProofRequest,
        liveData: SingleLiveData<ProofCreateResult>
    ) {
        val res = Connections.create(proof.attachConnection!!, QRConnection()).wrap().await()
        if (res != null) {
            val serializedCon = Connections.awaitStatusChange(res, MessageState.ACCEPTED)
            val pwDid = Connections.getPwDid(serializedCon)
            val c = Connection(
                name = proof.attachConnectionName!!,
                icon = proof.attachConnectionLogo,
                pwDid = pwDid,
                serialized = serializedCon
            )
            proof.pwDid = pwDid
            db.connectionDao().insertAll(c)
            liveData.postValue(SUCCESS_CONNECTION)
            db.proofRequestDao().update(proof)
            var creds: String? = null
            try {
                creds = Proofs.retrieveAvailableCredentials(proof.serialized).wrap().await()
            } catch (e: Exception) {
                liveData.postValue(MISSED)
            }
            val data = Proofs.mapCredentials(creds)
            val s = Proofs.send(serializedCon, proof.serialized, data, "{}").wrap().await()
            if (s != null) {
                val serializedProof =
                    Proofs.awaitStatusChange(s, MessageState.ACCEPTED)
                proof.accepted = true
                proof.serialized = serializedProof
                db.proofRequestDao().update(proof)
                liveData.postValue(SUCCESS)

            } else {
                liveData.postValue(FAILURE)
            }
        } else {
            liveData.postValue(FAILURE_CONNECTION)
        }
    }

    private fun acceptProofReq(proofId: Int, liveData: SingleLiveData<ProofCreateResult>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val proof: ProofRequest = db.proofRequestDao().getById(proofId)
            if (proof.attachConnection != null && proof.pwDid == null) {
                acceptProofReqAndCreateConnection(proof, liveData)
                return@launch
            }
            val con: Connection = db.connectionDao().getByPwDid(proof.pwDid!!)
            val creds: String? = null
            try {
                val creds = Proofs.retrieveAvailableCredentials(proof.serialized).wrap().await()
            } catch (e: Exception) {
                liveData.postValue(MISSED)
            }
            val data = Proofs.mapCredentials(creds)
            val s =  Proofs.send(con.serialized, proof.serialized, data, "{}").wrap().await()
            if (s != null) {
                val serializedProof =
                    Proofs.awaitStatusChange(s, MessageState.ACCEPTED)
                proof.accepted = true
                proof.serialized = serializedProof
                db.proofRequestDao().update(proof)
            }
            liveData.postValue(SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }

    }

    fun rejectProofRequest(proofId: Int): SingleLiveData<ProofCreateResult> {
        val data = SingleLiveData<ProofCreateResult>()
        rejectProofReq(proofId, data)
        return data
    }

    private fun rejectProofReq(proofId: Int, liveData: SingleLiveData<ProofCreateResult>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val proof = db.proofRequestDao().getById(proofId)
            val con = db.connectionDao().getByPwDid(proof.pwDid!!)
            val s = Proofs.reject(con.serialized, proof.serialized).wrap().await()
            val serializedProof = Proofs.awaitStatusChange(s, MessageState.REJECTED)
            proof.serialized = serializedProof
            proof.accepted = false
            db.proofRequestDao().update(proof)
            liveData.postValue(SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }

    fun getNewProofRequests(): SingleLiveData<ProofCreateResult> {
        val data = SingleLiveData<ProofCreateResult>()
        checkProofRequests(data)
        return data
    }

    private fun checkProofRequests(data: SingleLiveData<ProofCreateResult>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = Messages.getPendingMessages(MessageType.PROOF_REQUEST, null, null).wrap().await()

            res.forEach { message ->
                val holder = ProofDataHolder.extractRequestedFieldsFromProofMessage(message)!!
                val pwDid: String = message.pwDid
                if (!db.proofRequestDao().checkExists(holder.threadId)) {
                    val pr = Proofs.createWithRequest(UUID.randomUUID().toString(), holder.proofReq).wrap().await()
                    val proof = ProofRequest(
                            serialized = pr,
                            name = holder.name,
                            pwDid = pwDid,
                            attributes = holder.attributes,
                            threadId = holder.threadId,
                            messageId = message.uid
                    )
                    db.proofRequestDao().insertAll(proof)

                    Messages.updateMessageStatus(pwDid, message.uid)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            data.postValue(SUCCESS)
        }
    }
}
