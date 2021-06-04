package me.connect.sdk.java.samplekt.credentials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.connect.sdk.java.Credentials
import me.connect.sdk.java.Messages
import me.connect.sdk.java.message.MessageState
import me.connect.sdk.java.message.MessageType
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
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

    fun getNewCredentialOffers(): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        checkCredentialOffers(data)
        return data
    }

    fun acceptOffer(offerId: Int): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        acceptCredentialOffer(offerId, data)
        return data
    }

    private fun acceptCredentialOffer(offerId: Int, data: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val offer = db.credentialOffersDao().getById(offerId)
            val connection = db.connectionDao().getByPwDid(offer.pwDid)
            val s = Credentials.acceptOffer(connection.serialized, offer.serialized).wrap().await()
            val s2: String = Credentials.awaitStatusChange(s, MessageState.ACCEPTED)
            offer.serialized = s2
            offer.accepted = true
            db.credentialOffersDao().update(offer)
            data.postValue(true)
        } catch (e: Exception) {
            e.printStackTrace()
            data.postValue(false)
        }
    }

    private fun checkCredentialOffers(liveData: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = Messages.getPendingMessages(MessageType.CREDENTIAL_OFFER).wrap().await()
            res.forEach { message ->
                val holder = CredDataHolder.extractDataFromCredentialsOfferMessage(message)
                val pwDid: String = message.pwDid
                if (!db.credentialOffersDao().checkOfferExists(pwDid)) {
                    val co = Credentials.createWithOffer(UUID.randomUUID().toString(), holder!!.offer).wrap().await()
                    val offer = CredentialOffer(
                            claimId = holder.id,
                            name = holder.name,
                            pwDid = pwDid,
                            attributes = holder.attributes,
                            serialized = co,
                            messageId = message.uid
                    )
                    db.credentialOffersDao().insertAll(offer)

                    Messages.updateMessageStatus(pwDid, message.uid)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            liveData.postValue(true)
        }
    }

}