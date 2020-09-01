package me.connect.sdk.java.samplekt.proofs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.ProofRequest
import me.connect.sdk.java.Messages
import me.connect.sdk.java.Proofs
import me.connect.sdk.java.message.Message
import me.connect.sdk.java.message.MessageState
import me.connect.sdk.java.message.MessageType
import me.connect.sdk.java.samplekt.wrap
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import java.util.concurrent.Executors


class ProofRequestsViewModel(application: Application) : AndroidViewModel(application) {
    private val db: Database = Database.getInstance(application)
    private val proofRequests by lazy {
        MutableLiveData<List<ProofRequest>>()
    }

    fun getProofRequests(): LiveData<List<ProofRequest>> {
        loadProofRequests()
        return proofRequests
    }

    fun acceptProofRequest(proofId: Int): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        acceptProofReq(proofId, data)
        return data
    }

    private fun acceptProofReq(proofId: Int, liveData: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val proof = db.proofRequestDao().getById(proofId)
            val con = db.connectionDao().getById(proof.connectionId)
            val creds = Proofs.retrieveAvailableCredentials(proof.serialized).wrap().await()
            // We automatically map first of each provided credentials to final structure
            // This process should be interactive in real app
            val data = Proofs.mapCredentials(creds)
            val s = Proofs.send(con.serialized, proof.serialized, data, "{}", proof.messageId).wrap().await()
            val serializedProof = Proofs.awaitStatusChange(s, MessageState.ACCEPTED)
            proof.accepted = true
            proof.serialized = serializedProof
            db.proofRequestDao().update(proof)
            loadProofRequests()
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
            val con = db.connectionDao().getById(proof.connectionId)
            val s = Proofs.reject(con.serialized, proof.serialized, proof.messageId).wrap().await()
            val serializedProof = Proofs.awaitStatusChange(s, MessageState.REJECTED)
            proof.serialized = serializedProof
            proof.accepted = false
            db.proofRequestDao().update(proof)
            loadProofRequests()
            liveData.postValue(true)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(false)
        }
    }

    private fun loadProofRequests() = viewModelScope.launch(Dispatchers.IO) {
        val data = db.proofRequestDao().getAll()
        proofRequests.postValue(data)
    }

    fun getNewProofRequests(): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        checkProofRequests(data)
        return data
    }

    private fun checkProofRequests(data: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val connections = db.connectionDao().getAll()
            connections.forEach { c ->
                val res = Messages.getPendingMessages(c.serialized, MessageType.PROOF_REQUEST).wrap().await()
                res.forEach { message ->
                    val holder = extractRequestedFieldsFromProofMessage(message)!!
                    if (!db.proofRequestDao().checkExists(holder.threadId)) {
                        val pr = Proofs.createWithRequest(UUID.randomUUID().toString(), holder.proofReq).wrap().await()
                        val proof = ProofRequest(
                                serialized = pr,
                                name = holder.name,
                                connectionId = c.id,
                                attributes = holder.attributes,
                                threadId = holder.threadId,
                                messageId = message.uid
                        )
                        db.proofRequestDao().insertAll(proof)
                    }
                }
            }
            loadProofRequests()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            data.postValue(true)
        }
    }

    private fun extractRequestedFieldsFromProofMessage(msg: Message): ProofDataHolder? {
        return try {
            val json = JSONObject(msg.payload)
            val data: JSONObject = json.getJSONObject("proof_request_data")
            val threadId: String = json.getString("thread_id")
            val name: String = data.getString("name")
            val requestedAttrs: JSONObject = data.getJSONObject("requested_attributes")
            val attributes = requestedAttrs.keys()
                    .asSequence()
                    .map { requestedAttrs.getJSONObject(it).getString("name") }
                    .joinToString(", ")
            ProofDataHolder(threadId, name, attributes.toString(), msg.payload)
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    internal class ProofDataHolder(var threadId: String, var name: String, var attributes: String, var proofReq: String)
}
