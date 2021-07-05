package me.connect.sdk.java.samplekt.homepage

import kotlinx.coroutines.future.await
import me.connect.sdk.java.Connections
import me.connect.sdk.java.Credentials
import me.connect.sdk.java.OutOfBandHelper
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Action
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer
import me.connect.sdk.java.samplekt.homepage.Results.*
import me.connect.sdk.java.samplekt.wrap
import org.json.JSONException
import java.util.*

object StateCredentialOffers {
    suspend fun createCredentialStateObject(
        db: Database,
        outOfBandInvite: OutOfBandHelper.OutOfBandInvite,
        liveData: SingleLiveData<Results>,
        action: Action
    ) {
        try {
            val claimId: String = outOfBandInvite.attach.getString("@id");
            if (!db.credentialOffersDao().checkOfferExists(claimId)) {
                val thread = outOfBandInvite.attach.getJSONObject("~thread")
                val threadId = thread.getString("thid")
                var pwDid: String? = null
                if (outOfBandInvite.existingConnection != null) {
                    pwDid = Connections.getPwDid(outOfBandInvite.existingConnection)
                }
                val serialized = Credentials.createWithOffer(
                        UUID.randomUUID().toString(),
                        outOfBandInvite.extractedAttachRequest
                ).wrap().await()
                val offer = CredentialOffer(
                        threadId = threadId,
                        claimId = claimId,
                        pwDid = pwDid,
                        serialized = serialized,
                        attachConnection = outOfBandInvite.parsedInvite,
                        attachConnectionLogo = outOfBandInvite.userMeta.logo,
                        attachConnectionName = outOfBandInvite.userMeta.name
                )
                db.credentialOffersDao().insertAll(offer)
                processCredentialOffer(offer, db, liveData, action);
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    suspend fun processCredentialOffer(
            offer: CredentialOffer,
            db: Database,
            data: SingleLiveData<Results>,
            action: Action
    ) {
        try {
            if (offer.pwDid == null) {
                acceptCredentialOfferAndCreateConnection(offer, db, data, action)
            } else {
                val connection: Connection = db.connectionDao().getByPwDid(offer.pwDid!!)
                acceptCredentialOffer(offer, connection, db, data, action)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            data.postValue(OFFER_FAILURE)
        }
    }

    suspend fun acceptCredentialOffer(
        offer: CredentialOffer,
        connection: Connection,
        db: Database,
        data: SingleLiveData<Results>,
        action: Action
    ) {
        try {
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
        val res = Connections.create(offer.attachConnection!!, Connections.InvitationType.OutOfBand).wrap().await()
        if (res != null) {
            val pwDid = Connections.getPwDid(res)
            val serializedCon = Connections.awaitConnectionCompleted(res, pwDid)
            val connection = Connection(
                name = offer.attachConnectionName!!,
                icon = offer.attachConnectionLogo,
                pwDid = pwDid,
                serialized = serializedCon
            )
            db.connectionDao().insertAll(connection)
            data.postValue(CONNECTION_SUCCESS)

            HomePageViewModel.HistoryActions.addHistoryAction(
                db,
                offer.attachConnectionName!!,
                "Connection created",
                offer.attachConnectionLogo!!,
                data
            );

            acceptCredentialOffer(offer, connection, db, data, action)
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