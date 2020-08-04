package me.connect.sdk.java.samplekt.proofs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.ProofRequest
import me.connect.sdk.java.Messages
import me.connect.sdk.java.Proofs
import me.connect.sdk.java.message.Message
import me.connect.sdk.java.message.MessageState
import me.connect.sdk.java.message.MessageType
import org.json.JSONException
import org.json.JSONObject
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

    private fun acceptProofReq(proofId: Int, liveData: SingleLiveData<Boolean>) {
        Executors.newSingleThreadExecutor().execute(fun() {
            val proof = db.proofRequestDao().getById(proofId)
            val con = db.connectionDao().getById(proof.connectionId)
            Proofs.retrieveAvailableCredentials(proof.serialized).handle { creds, err ->
                if (err != null) {
                    liveData.postValue(false)
                    return@handle
                }
                // We automatically map first of each provided credentials to final structure
                // This process should be interactive in real app
                val data = Proofs.mapCredentials(creds)
                Proofs.send(con.serialized, proof.serialized, data, "{}", proof.messageId).handle { s, e ->
                    if (s != null) {
                        val serializedProof = Proofs.awaitStatusChange(s, MessageState.ACCEPTED)
                        proof.accepted = true
                        proof.serialized = serializedProof
                        db.proofRequestDao().update(proof)
                    }
                    loadProofRequests()
                    liveData.postValue(e == null)
                }

            }
        })
    }

    fun rejectProofRequest(proofId: Int): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        rejectProofReq(proofId, data)
        return data
    }

    private fun rejectProofReq(proofId: Int, liveData: SingleLiveData<Boolean>) {
        Executors.newSingleThreadExecutor().execute {
            val proof = db.proofRequestDao().getById(proofId)
            val con = db.connectionDao().getById(proof.connectionId)
            Proofs.reject(con.serialized, proof.serialized, proof.messageId).handle { s, err ->
                if (s != null) {
                    val serializedProof = Proofs.awaitStatusChange(s, MessageState.REJECTED)
                    proof.serialized = serializedProof
                    proof.accepted = false
                    db.proofRequestDao().update(proof)
                }
                loadProofRequests()
                liveData.postValue(err == null)
            }
        }
    }

    private fun loadProofRequests() {
        Executors.newSingleThreadExecutor().execute {
            val data = db.proofRequestDao().getAll()
            proofRequests.postValue(data)
        }
    }

    fun getNewProofRequests(): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        checkProofRequests(data)
        return data
    }

    private fun checkProofRequests(data: SingleLiveData<Boolean>) {
        Executors.newSingleThreadExecutor().execute {
            val connections = db.connectionDao().getAll()
            connections.forEach { c ->
                Messages.getPendingMessages(c.serialized, MessageType.PROOF_REQUEST).handle { res, throwable ->
                    res?.forEach { message ->
                        val holder = extractRequestedFieldsFromProofMessage(message)!!
                        if (!db.proofRequestDao().checkExists(holder.threadId)) {
                            Proofs.createWithRequest(UUID.randomUUID().toString(), holder.proofReq).handle { pr, err ->
                                if (err != null) {
                                    err.printStackTrace()
                                } else {
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
                                loadProofRequests()
                            }
                        }
                    }
                    data.postValue(true)
                }
            }
        }
    }

    private fun extractRequestedFieldsFromProofMessage(msg: Message): ProofDataHolder? {
        return try {
            val json = JSONObject(msg.payload)
            val data: JSONObject = json.getJSONObject("proof_request_data")
            val threadId: String = json.getString("thread_id")
            val name: String = data.getString("name")
            val requestedAttrs: JSONObject = data.getJSONObject("requested_attributes")
            val keys: Iterator<String> = requestedAttrs.keys()
            val attributes = StringBuilder()
            while (keys.hasNext()) {
                val key = keys.next()
                val value: String = requestedAttrs.getJSONObject(key).getString("name")
                attributes.append(value)
                if (keys.hasNext()) {
                    attributes.append(", ")
                }
            }
            ProofDataHolder(threadId, name, attributes.toString(), msg.payload)
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    internal class ProofDataHolder(var threadId: String, var name: String, var attributes: String, var proofReq: String)
}
