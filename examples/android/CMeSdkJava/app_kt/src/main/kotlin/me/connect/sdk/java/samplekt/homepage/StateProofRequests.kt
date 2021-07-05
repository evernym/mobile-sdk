package me.connect.sdk.java.samplekt.homepage

import kotlinx.coroutines.future.await
import me.connect.sdk.java.*
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Action
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.db.entity.ProofRequest
import me.connect.sdk.java.samplekt.homepage.Results.*
import me.connect.sdk.java.samplekt.wrap
import java.util.*

object StateProofRequests {
    suspend fun createProofStateObject(
        db: Database,
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        val threadId = outOfBandInvite.attach.getJSONObject("~thread").getString("thid")

        var pwDid: String? = outOfBandInvite.existingConnection
        if (outOfBandInvite.existingConnection != null) {
            pwDid = Connections.getPwDid(outOfBandInvite.existingConnection)
        }

        val serialized = Proofs.createWithRequest(
                UUID.randomUUID().toString(),
                outOfBandInvite.extractedAttachRequest
        ).wrap().await()
        val proof = ProofRequest(
                serialized = serialized,
                threadId = threadId,
                pwDid = pwDid,
                attachConnection = outOfBandInvite.parsedInvite,
                attachConnectionName = outOfBandInvite.userMeta?.name,
                attachConnectionLogo = outOfBandInvite.userMeta?.logo
        )
        db.proofRequestDao().insertAll(proof)
        processProofRequest(proof, db, liveData, action);
    }

    suspend fun processProofRequest(
            proof: ProofRequest,
            db: Database,
            liveData: SingleLiveData<Results>,
            action: Action
    ) {
        try {
            if (proof.pwDid == null) {
                acceptProofReqAndCreateConnection(proof, db, liveData, action)
            } else {
                val con: Connection = db.connectionDao().getByPwDid(proof.pwDid!!)
                acceptProofRequest(proof, con, db, liveData, action)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(PROOF_FAILURE)
        }
    }

    private suspend fun acceptProofRequest(
        proof: ProofRequest,
        connection: Connection,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        try {
            var creds: String? = null
            try {
                creds = Proofs.retrieveAvailableCredentials(proof.serialized).wrap().await()
            } catch (e: Exception) {
                liveData.postValue(PROOF_MISSED)
            }
            val data = Proofs.mapCredentials(creds)
            val s =  Proofs.send(connection.serialized, proof.serialized, data, "{}").wrap().await()
            if (s != null) {
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
        val res = Connections.create(proof.attachConnection!!, Connections.InvitationType.OutOfBand).wrap().await()
        if (res != null) {
            val pwDid = Connections.getPwDid(res)
            val serializedCon = Connections.awaitConnectionCompleted(res, pwDid)

            val connection = Connection(
                name = proof.attachConnectionName!!,
                icon = proof.attachConnectionLogo,
                pwDid = pwDid,
                serialized = serializedCon
            )
            db.connectionDao().insertAll(connection)
            liveData.postValue(CONNECTION_SUCCESS)

            proof.pwDid = pwDid
            db.proofRequestDao().update(proof)

            HomePageViewModel.HistoryActions.addHistoryAction(
                db,
                proof.attachConnectionName!!,
                "Connection created",
                proof.attachConnectionLogo!!,
                liveData
            );
            acceptProofRequest(proof, connection, db, liveData, action)
        } else {
            liveData.postValue(CONNECTION_FAILURE)
        }
    }

    suspend fun rejectProofReq(proof: ProofRequest, db: Database, liveData: SingleLiveData<Results>) {
        try {
            if (proof.pwDid == null) {
                db.proofRequestDao().update(proof);
                liveData.postValue(PROOF_SUCCESS);
                return;
            }
            val con = db.connectionDao().getByPwDid(proof.pwDid!!)
            val s = Proofs.reject(con.serialized, proof.serialized).wrap().await()
            proof.serialized = s
            db.proofRequestDao().update(proof)

            liveData.postValue(PROOF_SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(PROOF_FAILURE)
        }
    }
}