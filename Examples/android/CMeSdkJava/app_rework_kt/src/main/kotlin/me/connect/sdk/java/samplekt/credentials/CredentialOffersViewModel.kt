package me.connect.sdk.java.samplekt.credentials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer
import me.connect.sdk.java.Credentials
import me.connect.sdk.java.Messages
import me.connect.sdk.java.message.Message
import me.connect.sdk.java.message.MessageState
import me.connect.sdk.java.message.MessageType
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import java.util.concurrent.Executors


class CredentialOffersViewModel(application: Application) : AndroidViewModel(application) {
    private val db: Database = Database.getInstance(application)
    private val credentialOffers: MutableLiveData<List<CredentialOffer>> by lazy {
        MutableLiveData<List<CredentialOffer>>()
    }

    fun getCredentialOffers(): LiveData<List<CredentialOffer>> {
        loadCredentialOffers()
        return credentialOffers
    }

    private fun loadCredentialOffers() {
        Executors.newSingleThreadExecutor().execute {
            val data = db.credentialOffersDao().getAll()
            credentialOffers.postValue(data)
        }
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

    private fun acceptCredentialOffer(offerId: Int, data: SingleLiveData<Boolean>) {
        Executors.newSingleThreadExecutor().execute {
            val offer = db.credentialOffersDao().getById(offerId)
            val connection = db.connectionDao().getById(offer.connectionId)
            Credentials.acceptOffer(connection.serialized, offer.serialized, offer.messageId).handle { s, throwable ->
                if (s != null) {
                    val s2: String = Credentials.awaitStatusChange(s, MessageState.ACCEPTED)
                    offer.serialized = s2
                    offer.accepted = true
                    db.credentialOffersDao().update(offer)
                }
                loadCredentialOffers()
                data.postValue(throwable == null)
            }
        }
    }

    private fun checkCredentialOffers(liveData: SingleLiveData<Boolean>) {
        Executors.newSingleThreadExecutor().execute {
            val connections = db.connectionDao().getAll()
            connections.forEach { c ->
                Messages.getPendingMessages(c.serialized, MessageType.CREDENTIAL_OFFER).handle { res, throwable ->
                    throwable?.printStackTrace()
                    res?.forEach { message ->
                        val holder = extractDataFromCredentialsOfferMessage(message)
                        if (!db.credentialOffersDao().checkOfferExists(holder!!.id, c.id)) {
                            Credentials.createWithOffer(c.serialized, UUID.randomUUID().toString(), holder.offer).handle { co, err ->
                                if (err != null) {
                                    err.printStackTrace()
                                } else {
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
                                loadCredentialOffers()
                            }
                        }
                    }
                    liveData.postValue(true)
                }
            }
        }
    }

    private fun extractDataFromCredentialsOfferMessage(msg: Message): CredDataHolder? {
        return try {
            val data = JSONArray(msg.payload).getJSONObject(0)
            val id = data.getString("claim_id")
            val name= data.getString("claim_name")
            val attributesJson = data.getJSONObject("credential_attrs")
            val attributes = StringBuilder()
            val keys = attributesJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = attributesJson.getString(key)
                attributes.append("$key: $value\n")
            }
            CredDataHolder(id, name, attributes.toString(), msg.payload)
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    internal class CredDataHolder(var id: String, var name: String, var attributes: String, var offer: String)
}