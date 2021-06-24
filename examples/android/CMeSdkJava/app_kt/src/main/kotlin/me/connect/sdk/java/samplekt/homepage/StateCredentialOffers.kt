package me.connect.sdk.java.samplekt.homepage

import kotlinx.coroutines.future.await
import me.connect.sdk.java.Connections
import me.connect.sdk.java.Credentials
import me.connect.sdk.java.OutOfBandHelper
import me.connect.sdk.java.Utils
import me.connect.sdk.java.connection.QRConnection
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Action
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer
import me.connect.sdk.java.samplekt.homepage.Results.*
import me.connect.sdk.java.samplekt.wrap
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object StateCredentialOffers {
    suspend fun createCredentialStateObjectForExistingConnection(
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        db: Database,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        try {
            val connection: JSONObject = Utils.convertToJSONObject(outOfBandInvite.existingConnection)!!
            val connectionData = connection.getJSONObject("data")
            val claimId: String = outOfBandInvite.attach.getString("@id");
            val pwDid: String =  connectionData.getString("pw_did")
            if (!db.credentialOffersDao().checkOfferExists(claimId, pwDid)) {
                val co = Credentials.createWithOffer(
                    UUID.randomUUID().toString(),
                    outOfBandInvite.extractedAttachRequest
                ).wrap().await()
                val thread = outOfBandInvite.attach.getJSONObject("~thread")
                val offer = CredentialOffer(
                    threadId = thread.getString("thid"),
                    claimId = claimId,
                    pwDid = pwDid,
                    serialized = co,
                    attachConnectionLogo = JSONObject(outOfBandInvite.parsedInvite).getString("profileUrl")
                )
                db.credentialOffersDao().insertAll(offer)
                acceptCredentialOffer(offer, db, liveData, action);
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    suspend fun createCredentialStateObject(
        db: Database,
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        val co = Credentials.createWithOffer(
            UUID.randomUUID().toString(),
            outOfBandInvite.extractedAttachRequest
        ).wrap().await()
        val thread = outOfBandInvite.attach.getJSONObject("~thread")
        val offer = CredentialOffer(
            threadId = thread.getString("thid"),
            claimId = outOfBandInvite.attach.getString("@id"),
            serialized = co,
            attachConnection = outOfBandInvite.parsedInvite,
            attachConnectionName = outOfBandInvite.userMeta?.name,
            attachConnectionLogo = outOfBandInvite.userMeta?.logo
        )
        db.credentialOffersDao().insertAll(offer)
        acceptCredentialOffer(offer, db, liveData, action);
    }

    suspend fun acceptCredentialOffer(
        offer: CredentialOffer,
        db: Database,
        data: SingleLiveData<Results>,
        action: Action
    ) {
        try {
            if (offer.attachConnection != null) {
                acceptCredentialOfferAndCreateConnection(offer, db, data, action)
                return
            }
            val connection: Connection = db.connectionDao().getByPwDid(offer.pwDid!!)
            val s = Credentials.acceptOffer(connection.serialized, offer.serialized).wrap().await()
            if (s != null) {
                offer.serialized = Credentials.awaitCredentialReceived(s, offer.threadId, offer.pwDid)
                db.credentialOffersDao().update(offer)

                HomePageViewModel.HistoryActions.addToHistory(
                    action.id,
                    "Credential accept",
                    db,
                    data
                );

                data.postValue(OFFER_SUCCESS);
            } else {
                HomePageViewModel.HistoryActions.addToHistory(
                    action.id,
                    "Credential accept failure",
                    db,
                    data
                );
                data.postValue(OFFER_FAILURE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            data.postValue(FAILURE)
        }
    }

    private suspend fun acceptCredentialOfferAndCreateConnection(
        offer: CredentialOffer,
        db: Database,
        data: SingleLiveData<Results>,
        action: Action
    ) {
        val res = Connections.create(offer.attachConnection!!, QRConnection()).wrap().await()
        if (res != null) {
            val pwDid = Connections.getPwDid(res)
            val serializedCon = Connections.awaitConnectionReceived(res, pwDid)
            val c = Connection(
                name = offer.attachConnectionName!!,
                icon = offer.attachConnectionLogo,
                pwDid = pwDid,
                serialized = serializedCon
            )
            db.connectionDao().insertAll(c)
            data.postValue(CONNECTION_SUCCESS)

            HomePageViewModel.HistoryActions.addHistoryAction(
                db,
                offer.attachConnectionName!!,
                "Connection created",
                offer.attachConnectionLogo!!,
                data
            );

            val s = Credentials.acceptOffer(serializedCon, offer.serialized).wrap().await()
            if (s != null) {
                offer.serialized = Credentials.awaitCredentialReceived(s, offer.threadId, pwDid)
                offer.pwDid = pwDid
                db.credentialOffersDao().update(offer)

                HomePageViewModel.HistoryActions.addToHistory(
                    action.id,
                    "Credential accept",
                    db,
                    data
                );

                data.postValue(OFFER_SUCCESS);
            } else {
                HomePageViewModel.HistoryActions.addToHistory(
                    action.id,
                    "Credential accept failure",
                    db,
                    data
                );
                data.postValue(OFFER_FAILURE)
            }
        } else {
            data.postValue(CONNECTION_FAILURE)
        }
    }

    suspend fun rejectCredentialOffer(
        offer: CredentialOffer,
        db: Database,
        liveData: SingleLiveData<Results>
    ) {
        try {
            if (offer.pwDid == null) {
                liveData.postValue(PROOF_SUCCESS)
                return
            }
            val con = db.connectionDao().getByPwDid(offer.pwDid!!)
            val s = Credentials.rejectOffer(con.serialized, offer.serialized).wrap().await()
            if (s != null) {
                offer.serialized = s
                db.credentialOffersDao().update(offer)
            }
            liveData.postValue(PROOF_SUCCESS)
        } catch (e: Exception) {
            liveData.postValue(PROOF_FAILURE)
        }
    }
}