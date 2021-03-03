package me.connect.sdk.java.samplekt.messages

import me.connect.sdk.java.message.Message
import org.json.JSONException
import org.json.JSONObject

class ProofDataHolder(var threadId: String, var name: String, var attributes: String, var proofReq: String) {
    companion object {
        fun extractRequestedFieldsFromProofMessage(msg: Message): ProofDataHolder? {
            return try {
                val json = JSONObject(msg.payload)
                val data = json.getJSONObject("proof_request_data")
                val threadId = json.getString("thread_id")
                val name = data.getString("name")
                val requestedAttrs = data.getJSONObject("requested_attributes")
                val keys = requestedAttrs.keys()
                val attributes = StringBuilder()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = requestedAttrs.getJSONObject(key).getString("name")
                    attributes.append(value)
                    if (keys.hasNext()) {
                        attributes.append(", ")
                    }
                }
                ProofDataHolder(threadId, name, attributes.toString(), msg.payload)
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
    }
}
