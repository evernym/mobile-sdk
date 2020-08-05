package me.connect.sdk.java.samplekt.credentials

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
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer
import me.connect.sdk.java.Credentials
import me.connect.sdk.java.Messages
import me.connect.sdk.java.message.Message
import me.connect.sdk.java.message.MessageState
import me.connect.sdk.java.message.MessageType
import me.connect.sdk.java.samplekt.wrap
import org.json.JSONArray
import org.json.JSONException
import java.util.*


class CredentialOffersViewModel(application: Application) : AndroidViewModel(application) {
    private val db: Database = Database.getInstance(application)
    private val credentialOffers: MutableLiveData<List<CredentialOffer>> by lazy {
        MutableLiveData<List<CredentialOffer>>()
    }

    fun getCredentialOffers(): LiveData<List<CredentialOffer>> {
        loadCredentialOffers()
        return credentialOffers
    }

    private fun loadCredentialOffers() = viewModelScope.launch(Dispatchers.IO) {
        val data = db.credentialOffersDao().getAll()
        credentialOffers.postValue(data)
    }

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
            val connection = db.connectionDao().getById(offer.connectionId)
            val s = Credentials.acceptOffer(connection.serialized, offer.serialized, offer.messageId).wrap().await()
            val s2: String = Credentials.awaitStatusChange(s, MessageState.ACCEPTED)
            offer.serialized = s2
            offer.accepted = true
            db.credentialOffersDao().update(offer)
            loadCredentialOffers()
            data.postValue(true)
        } catch (e: Exception) {
            e.printStackTrace()
            data.postValue(false)
        }
    }

    private fun checkCredentialOffers(liveData: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val connections = db.connectionDao().getAll()
            connections.forEach { c ->
                val res = Messages.getPendingMessages(c.serialized, MessageType.CREDENTIAL_OFFER).wrap().await()
                res.forEach { message ->
                    val holder = extractDataFromCredentialsOfferMessage(message)
                    if (!db.credentialOffersDao().checkOfferExists(holder!!.id, c.id)) {
                        val co = Credentials.createWithOffer(c.serialized, UUID.randomUUID().toString(), holder.offer).wrap().await()
                        val offer = CredentialOffer(
                                claimId = holder.id,
                                name = holder.name,
                                connectionId = c.id,
                                attributes = holder.attributes,
                                serialized = co,
                                messageId = message.uid
                        )
                        db.credentialOffersDao().insertAll(offer)
                    }
                }
            }
            loadCredentialOffers()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            liveData.postValue(true)
        }
    }

    private fun extractDataFromCredentialsOfferMessage(msg: Message): CredDataHolder? {
        return try {
            val data = JSONArray(msg.payload).getJSONObject(0)
            val id = data.getString("claim_id")
            val name = data.getString("claim_name")
            val attributesJson = data.getJSONObject("credential_attrs")
            val attributes = attributesJson.keys()
                    .asSequence()
                    .map { "$it: ${attributesJson.getString(it)}" }
                    .joinToString("\n")
            CredDataHolder(id, name, attributes, msg.payload)
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    internal class CredDataHolder(var id: String, var name: String, var attributes: String, var offer: String)
}