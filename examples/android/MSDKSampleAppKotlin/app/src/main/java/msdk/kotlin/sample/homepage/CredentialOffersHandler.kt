package msdk.kotlin.sample.homepage

import kotlinx.coroutines.future.await
import msdk.kotlin.sample.SingleLiveData
import msdk.kotlin.sample.db.Database
import msdk.kotlin.sample.db.entity.Action
import msdk.kotlin.sample.db.entity.Connection
import msdk.kotlin.sample.db.entity.CredentialOffer
import msdk.kotlin.sample.handlers.Connections
import msdk.kotlin.sample.handlers.Credentials
import msdk.kotlin.sample.history.HistoryHandler
import msdk.kotlin.sample.homepage.Results.*
import msdk.kotlin.sample.messages.ConnectionInvitation
import msdk.kotlin.sample.messages.CredentialOfferMessage
import msdk.kotlin.sample.messages.OutOfBandInvitation
import msdk.kotlin.sample.utils.wrap
import java.util.*


object CredentialOffersHandler {
    suspend fun createCredentialStateObject(
            db: Database,
            outOfBandInvite: OutOfBandInvitation,
            liveData: SingleLiveData<Results>,
            action: Action
    ) {
        val credentialOffer = CredentialOfferMessage.parse(outOfBandInvite.attachment.toString())!!

        var pwDid: String? = null
        if (outOfBandInvite.existingConnection != null) {
            pwDid = Connections.getPwDid(outOfBandInvite.existingConnection!!)
        }
        val serialized = Credentials.createWithOffer(
            UUID.randomUUID().toString(),
            outOfBandInvite.attachment.toString()
        ).wrap().await()
        val offer = CredentialOffer(
            threadId = credentialOffer.threadId,
            pwDid = pwDid,
            serialized = serialized,
            attachConnection = outOfBandInvite.invitation,
            attachConnectionLogo = outOfBandInvite.userMeta!!.logo,
            attachConnectionName = outOfBandInvite.userMeta!!.name
        )
        db.credentialOffersDao().insertAll(offer)
        processCredentialOffer(offer, db, liveData, action)
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

                HistoryHandler.addToHistory(
                    action.id,
                    "Credential accept",
                    db,
                    data
                )

                data.postValue(OFFER_SUCCESS)
            } else {
                HistoryHandler.addToHistory(
                    action.id,
                    "Credential accept failure",
                    db,
                    data
                )
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
        val res = Connections.create(offer.attachConnection!!, ConnectionInvitation.InvitationType.OutOfBand).wrap().await()
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

            offer.pwDid = pwDid
            HistoryHandler.addHistoryAction(
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