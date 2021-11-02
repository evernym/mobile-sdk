package msdk.kotlin.sample.messages

import org.json.JSONArray
import org.json.JSONException

class CredentialOfferMessage(
    var id: String,
    var name: String,
    var attributes: String,
    var offer: String?,
    var threadId: String
) {

    companion object {
        fun parse(msg: Message): CredentialOfferMessage? {
            return try {
                val data = JSONArray(msg.payload).getJSONObject(0)
                val id = data.getString("claim_id")
                val thread_id = data.getString("thread_id")
                val name = data.getString("claim_name")
                val attributesJson = data.getJSONObject("credential_attrs")
                val attributes = StringBuilder()
                val keys = attributesJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = attributesJson.getString(key)
                    attributes.append(String.format("%s: %s\n", key, value))
                }
                CredentialOfferMessage(
                    id,
                    name,
                    attributes.toString(),
                    msg.payload,
                    thread_id
                )
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
    }

}