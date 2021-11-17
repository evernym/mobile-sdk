package msdk.kotlin.sample.messages

import android.util.Base64
import com.evernym.sdk.vcx.utils.UtilsApi
import msdk.kotlin.sample.utils.CommonUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ProofRequestMessage(
    var threadId: String,
    var name: String,
    var attributes: JSONObject,
    var proofReq: String?
) {

    companion object {
        fun parse(msg: Message): ProofRequestMessage? {
            return try {
                val json = JSONObject(msg.payload)
                val data = json.getJSONObject("proof_request_data")
                val threadId = json.getString("thread_id")
                val name = data.getString("name")
                val attributes = data.getJSONObject("requested_attributes")
                ProofRequestMessage(
                    threadId,
                    name,
                    attributes,
                    msg.payload
                )
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