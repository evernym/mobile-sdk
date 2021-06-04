package me.connect.sdk.java.samplekt.proofs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.connect.sdk.java.Messages
import me.connect.sdk.java.Proofs
import me.connect.sdk.java.message.MessageState
import me.connect.sdk.java.message.MessageType
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.ProofRequest
import me.connect.sdk.java.samplekt.messages.ProofDataHolder
import me.connect.sdk.java.samplekt.wrap
import java.util.*


class ProofRequestsViewModel(application: Application) : AndroidViewModel(application) {
    private val db: Database = Database.getInstance(application)
    private val proofRequestsLiveData by lazy {
        db.proofRequestDao().getAll()
    }

    fun getProofRequests(): LiveData<List<ProofRequest>> = proofRequestsLiveData

    fun acceptProofRequest(proofId: Int): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        acceptProofReq(proofId, data)
        return data
    }

    private fun acceptProofReq(proofId: Int, liveData: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val proof = db.proofRequestDao().getById(proofId)
            val con = db.connectionDao().getByPwDid(proof.pwDid)
            val creds = Proofs.retrieveAvailableCredentials(proof.serialized).wrap().await()
            // We automatically map first of each provided credentials to final structure
            // This process should be interactive in real app
            val data = Proofs.mapCredentials(creds)
            val s = Proofs.send(con.serialized, proof.serialized, data, "{}").wrap().await()
            val serializedProof = Proofs.awaitStatusChange(s, MessageState.ACCEPTED)
            proof.accepted = true
            proof.serialized = serializedProof
            db.proofRequestDao().update(proof)
            liveData.postValue(true)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(false)
        }

    }

    fun rejectProofRequest(proofId: Int): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        rejectProofReq(proofId, data)
        return data
    }

    private fun rejectProofReq(proofId: Int, liveData: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val proof = db.proofRequestDao().getById(proofId)
            val con = db.connectionDao().getByPwDid(proof.pwDid)
            val s = Proofs.reject(con.serialized, proof.serialized).wrap().await()
            val serializedProof = Proofs.awaitStatusChange(s, MessageState.REJECTED)
            proof.serialized = serializedProof
            proof.accepted = false
            db.proofRequestDao().update(proof)
            liveData.postValue(true)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(false)
        }
    }

    fun getNewProofRequests(): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        checkProofRequests(data)
        return data
    }

    private fun checkProofRequests(data: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = Messages.getPendingMessages(MessageType.PROOF_REQUEST).wrap().await()

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
            data.postValue(true)
        }
    }
}
