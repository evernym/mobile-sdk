package me.connect.sdk.java.samplekt.homepage

import kotlinx.coroutines.future.await
import me.connect.sdk.java.*
import me.connect.sdk.java.connection.QRConnection
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Action
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.db.entity.ProofRequest
import me.connect.sdk.java.samplekt.homepage.Results.*

import me.connect.sdk.java.samplekt.wrap
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object StateProofRequests {
    suspend fun createProofStateObjectForExistingConnection(
        db: Database,
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        try {
            val connection = Utils.convertToJSONObject(outOfBandInvite.existingConnection)!!
            val connectionData = connection.getJSONObject("data")
            val pr = Proofs.createWithRequest(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).wrap().await()
            val decodedProofAttach = ProofRequests.decodeProofRequestAttach(outOfBandInvite.attach)
            val thread = outOfBandInvite.attach.getJSONObject("~thread")
            val threadId = thread.getString("thid");
            if(!db.proofRequestDao().checkProofExists(threadId)) {
                val name = ProofRequests.extractRequestedNameFromProofRequest(decodedProofAttach)
                val attr =
                    ProofRequests.extractRequestedAttributesFromProofRequest(decodedProofAttach)
                if (name != null && attr != null) {
                    val proof = ProofRequest(
                        serialized = pr,
                        name = name,
                        pwDid = connectionData.getString("pw_did"),
                        attributes = attr,
                        threadId = threadId,
                        attachConnectionLogo = JSONObject(outOfBandInvite.parsedInvite)
                            .getString("profileUrl")
                    )
                    db.proofRequestDao().insertAll(proof)

                    acceptProofReq(proof, db, liveData, action);
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    suspend fun createProofStateObject(
        db: Database,
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        val pr = Proofs.createWithRequest(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).wrap().await()
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

            acceptProofReq(proof, db, liveData, action);
        }
    }

    suspend fun acceptProofReq(
        proof: ProofRequest,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        try {
            if (proof.attachConnection != null && proof.pwDid == null) {
                acceptProofReqAndCreateConnection(proof, db, liveData, action)
                return
            }
            val con: Connection = db.connectionDao().getByPwDid(proof.pwDid!!)
            var creds: String? = null
            try {
                creds = Proofs.retrieveAvailableCredentials(proof.serialized).wrap().await()
            } catch (e: Exception) {
                liveData.postValue(PROOF_MISSED)
            }
            val data = Proofs.mapCredentials(creds)
            val s =  Proofs.send(con.serialized, proof.serialized, data, "{}").wrap().await()
            if (s != null) {
                proof.accepted = true
                proof.serialized = s
                db.proofRequestDao().update(proof)

                HomePageViewModel.HistoryActions.addToHistory(
                    action.id,
                    "Proofs send",
                    db,
                    liveData
                );

                liveData.postValue(PROOF_SUCCESS);
            } else {
                liveData.postValue(PROOF_FAILURE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(PROOF_FAILURE)
        }
    }

    private suspend fun acceptProofReqAndCreateConnection(
        proof: ProofRequest,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        val res = Connections.create(proof.attachConnection!!, QRConnection()).wrap().await()
        if (res != null) {
            val pwDid = Connections.getPwDid(res)
            val c = Connection(
                name = proof.attachConnectionName!!,
                icon = proof.attachConnectionLogo,
                pwDid = pwDid,
                serialized = res
            )
            proof.pwDid = pwDid
            db.connectionDao().insertAll(c)
            liveData.postValue(CONNECTION_SUCCESS)
            db.proofRequestDao().update(proof)

            HomePageViewModel.HistoryActions.addHistoryAction(
                db,
                proof.attachConnectionName!!,
                "Connection created",
                proof.attachConnectionLogo!!,
                liveData
            );

            var creds: String? = null
            try {
                creds = Proofs.retrieveAvailableCredentials(proof.serialized).wrap().await()
            } catch (e: Exception) {
                liveData.postValue(PROOF_MISSED)
            }
            val data = Proofs.mapCredentials(creds)
            val s = Proofs.send(res, proof.serialized, data, "{}").wrap().await()
            if (s != null) {
                proof.accepted = true
                proof.serialized = s
                db.proofRequestDao().update(proof)

                HomePageViewModel.HistoryActions.addToHistory(
                    action.id,
                    "Proofs send",
                    db,
                    liveData
                );

                liveData.postValue(PROOF_SUCCESS);
            } else {
                liveData.postValue(PROOF_FAILURE)
            }
        } else {
            liveData.postValue(CONNECTION_FAILURE)
        }
    }

    suspend fun rejectProofReq(proof: ProofRequest, db: Database, liveData: SingleLiveData<Results>) {
        try {
            if (proof.pwDid == null) {
                proof.accepted = false;
                db.proofRequestDao().update(proof);
                liveData.postValue(PROOF_SUCCESS);
                return;
            }
            val con = db.connectionDao().getByPwDid(proof.pwDid!!)
            val s = Proofs.reject(con.serialized, proof.serialized).wrap().await()
            proof.serialized = s
            proof.accepted = false
            db.proofRequestDao().update(proof)

            liveData.postValue(PROOF_SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(PROOF_FAILURE)
        }
    }
}