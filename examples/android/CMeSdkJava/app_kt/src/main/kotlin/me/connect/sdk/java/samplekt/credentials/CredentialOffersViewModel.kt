package me.connect.sdk.java.samplekt.credentials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.connect.sdk.java.Connections
import me.connect.sdk.java.Credentials
import me.connect.sdk.java.Messages
import me.connect.sdk.java.connection.QRConnection
import me.connect.sdk.java.message.MessageState
import me.connect.sdk.java.message.MessageType
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.credentials.CredentialCreateResult.*
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer
import me.connect.sdk.java.samplekt.messages.CredDataHolder
import me.connect.sdk.java.samplekt.wrap
import java.util.*


class CredentialOffersViewModel(application: Application) : AndroidViewModel(application) {
    private val db: Database = Database.getInstance(application)
    private val credentialOffersLiveData: LiveData<List<CredentialOffer>> by lazy {
        db.credentialOffersDao().getAll()
    }

    fun getCredentialOffers(): LiveData<List<CredentialOffer>> = credentialOffersLiveData

    fun getNewCredentialOffers(): SingleLiveData<CredentialCreateResult> {
        val data = SingleLiveData<CredentialCreateResult>()
        checkCredentialOffers(data)
        return data
    }

    fun acceptOffer(offerId: Int): SingleLiveData<CredentialCreateResult> {
        val data = SingleLiveData<CredentialCreateResult>()
        acceptCredentialOffer(offerId, data)
        return data
    }

    private suspend fun acceptCredentialOfferAndCreateConnection(
        offer: CredentialOffer,
        data: SingleLiveData<CredentialCreateResult>
    ) {
        val res = Connections.create(offer.attachConnection!!, QRConnection()).wrap().await()
        if (res != null) {
            val serializedCon =
                Connections.awaitStatusChange(res, MessageState.ACCEPTED)
            val pwDid = Connections.getPwDid(serializedCon)
            val c: Connection = Connection(
                name = offer.attachConnectionName!!,
                icon = offer.attachConnectionLogo,
                pwDid = pwDid,
                serialized = serializedCon
            )
            db.connectionDao().insertAll(c)
            data.postValue(SUCCESS_CONNECTION)
            val s = Credentials.acceptOffer(serializedCon, offer.serialized).wrap().await()
            if (s != null) {
                offer.serialized =
                    Credentials.awaitStatusChange(
                        s,
                        MessageState.ACCEPTED
                    )
                offer.pwDid = pwDid
                offer.accepted = true
                db.credentialOffersDao().update(offer)
                data.postValue(SUCCESS)

            } else {
                data.postValue(FAILURE)
            }
        } else {
            data.postValue(FAILURE_CONNECTION)
        }
    }

    private fun acceptCredentialOffer(offerId: Int, data: SingleLiveData<CredentialCreateResult>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val offer: CredentialOffer = db.credentialOffersDao().getById(offerId)
            if (offer.attachConnection != null) {
                acceptCredentialOfferAndCreateConnection(offer, data)
                return@launch
            }
            val connection: Connection = db.connectionDao().getByPwDid(offer.pwDid!!)
            val s = Credentials.acceptOffer(connection.serialized, offer.serialized).wrap().await()
            if (s != null) {
                offer.serialized = Credentials.awaitStatusChange(
                    s,
                    MessageState.ACCEPTED
                )
                offer.accepted = true
                db.credentialOffersDao().update(offer)
            }
            data.postValue(SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
            data.postValue(FAILURE)
        }
    }

    private fun checkCredentialOffers(liveData: SingleLiveData<CredentialCreateResult>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = Messages.getPendingMessages(MessageType.CREDENTIAL_OFFER, null, null).wrap().await()
            res.forEach { message ->
                val holder = CredDataHolder.extractDataFromCredentialsOfferMessage(message)
                val pwDid: String = message.pwDid
                val connection: Connection = db.connectionDao().getByPwDid(pwDid)
                if (!db.credentialOffersDao().checkOfferExists(pwDid)) {
                    val co = Credentials.createWithOffer(UUID.randomUUID().toString(), holder!!.offer).wrap().await()
                    val offer = CredentialOffer(
                            claimId = holder.id,
                            name = holder.name,
                            pwDid = pwDid,
                            attributes = holder.attributes,
                            serialized = co,
                            messageId = message.uid,
                            attachConnectionLogo = connection.icon

                    )
                    db.credentialOffersDao().insertAll(offer)

                    Messages.updateMessageStatus(pwDid, message.uid)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            liveData.postValue(SUCCESS)
        }
    }

}