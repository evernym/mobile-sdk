package me.connect.sdk.java.samplekt.messages

import me.connect.sdk.java.message.Message
import org.json.JSONArray
import org.json.JSONException


class CredDataHolder(var id: String, var name: String, var attributes: String, var offer: String) {
    companion object {
        fun extractDataFromCredentialsOfferMessage(msg: Message): CredDataHolder? {
            return try {
                val data = JSONArray(msg.payload).getJSONObject(0)
                val id = data.getString("claim_id")
                val name = data.getString("claim_name")
                val attributesJson = data.getJSONObject("credential_attrs")
                val attributes = StringBuilder()
                val keys = attributesJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = attributesJson.getString(key)
                    attributes.append(String.format("%s: %s\n", key, value))
                }
                CredDataHolder(id, name, attributes.toString(), msg.payload)
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
    }
}