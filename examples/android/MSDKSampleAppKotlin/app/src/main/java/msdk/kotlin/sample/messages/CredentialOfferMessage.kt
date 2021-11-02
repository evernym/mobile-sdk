package msdk.kotlin.sample.messages

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CredentialOfferMessage(
    var id: String,
    var name: String,
    var attributes: JSONObject,
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
                val attributes = data.getJSONObject("credential_attrs")
                CredentialOfferMessage(
                    id,
                    name,
                    attributes,
                    msg.payload,
                    thread_id
                )
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }

        fun extractAttributesFromCredentialOffer(offer: JSONObject): JSONObject? {
            val attributes = JSONObject()
            try {
                val previewAttributes =
                    offer.getJSONObject("credential_preview").getJSONArray("attributes")
                for (i in 0 until previewAttributes.length()) {
                    val attribute = previewAttributes.getJSONObject(i)
                    attributes.put(attribute.getString("name"), attribute.getString("value"))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return attributes
        }
    }

}