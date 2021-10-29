package msdk.kotlin.sample.messages

import android.util.Base64
import msdk.kotlin.sample.utils.CommonUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ProofRequest(
    var threadId: String,
    var name: String,
    var attributes: String,
    var proofReq: String?
) {

    companion object {
        fun parseProofRequestMessage(msg: Message): ProofRequest? {
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
                ProofRequest(
                    threadId,
                    name,
                    attributes.toString(),
                    msg.payload
                )
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }

        fun decodeProofRequestAttach(proofAttach: JSONObject): JSONObject? {
            try {
                val requestAttachCode =
                    proofAttach.getString("request_presentations~attach")
                val requestsAttachItems = JSONArray(requestAttachCode)
                if (requestsAttachItems.length() == 0) {
                    return null
                }
                val requestsAttachItem = requestsAttachItems.getJSONObject(0)
                val requestsAttachItemData = requestsAttachItem.getJSONObject("data")
                val requestsAttachItemBase =
                    requestsAttachItemData.getString("base64")
                val requestAttachDecode = String(
                    Base64.decode(
                        requestsAttachItemBase,
                        Base64.NO_WRAP
                    )
                )
                return CommonUtils.convertToJSONObject(requestAttachDecode)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }

        fun extractRequestedAttributesFromProofRequest(decodedProofAttach: JSONObject?): String? {
            try {
                return decodedProofAttach?.getJSONObject("requested_attributes")?.toString()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }

        fun extractRequestedNameFromProofRequest(decodedProofAttach: JSONObject?): String? {
            try {
                return decodedProofAttach?.getString("name")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }
    }

}