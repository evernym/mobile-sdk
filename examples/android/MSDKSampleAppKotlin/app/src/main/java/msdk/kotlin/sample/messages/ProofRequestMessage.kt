package msdk.kotlin.sample.messages

import com.evernym.sdk.vcx.utils.UtilsApi
import msdk.kotlin.sample.utils.CommonUtils
import org.json.JSONException
import org.json.JSONObject


class ProofRequestMessage(
    var threadId: String,
    var name: String,
    var attributes: JSONObject,
    var proofReq: String?
) {

    companion object {
        fun parse(message: String?): ProofRequestMessage? {
            return try {
                val json = JSONObject(message)
                val threadId: String = CommonUtils.getThreadId(json)!!
                val name = json.getString("comment")
                val attachment = decodeProofRequestAttach(json)
                val requestedAttrs =
                    extractRequestedAttributesFromProofRequest(attachment)
                ProofRequestMessage(threadId, name, requestedAttrs!!, message)
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }

        fun decodeProofRequestAttach(proofAttach: JSONObject): JSONObject? {
            try {
                val attachment = UtilsApi.vcxExtractAttachedMessage(proofAttach.toString()).get()
                return CommonUtils.convertToJSONObject(attachment)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }

        fun extractRequestedAttributesFromProofRequest(proofRequest: JSONObject?): JSONObject? {
            try {
                return proofRequest?.getJSONObject("requested_attributes")
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