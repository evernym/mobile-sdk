package msdk.kotlin.sample.messages

import msdk.kotlin.sample.utils.CommonUtils
import org.json.JSONException
import org.json.JSONObject


class CredentialOfferMessage(
    var name: String,
    var attributes: JSONObject,
    var offer: String?,
    var threadId: String
) {

    companion object {
        fun parse(message: String?): CredentialOfferMessage? {
            return try {
                val json = JSONObject(message)
                val threadId: String = CommonUtils.getThreadId(json)!!
                val name = json.getString("comment")
                val attributes = extractAttributesFromCredentialOffer(json)
                CredentialOfferMessage(
                    name,
                    attributes!!,
                    message,
                    threadId
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