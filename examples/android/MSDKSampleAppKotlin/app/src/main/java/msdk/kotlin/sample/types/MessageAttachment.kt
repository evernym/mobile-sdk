package msdk.kotlin.sample.types

import android.util.Base64
import msdk.kotlin.sample.utils.CommonUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MessageAttachment(var type: String, var data: JSONObject) {
    val isCredentialAttachment: Boolean
        get() = type.contains("issue-credential")

    val isProofAttachment: Boolean
        get() = type.contains("present-proof")

    companion object {
        fun parse(invite: String?): MessageAttachment? {
            try {
                val json = CommonUtils.convertToJSONObject(invite)
                if (json == null || !json.has("request~attach")) {
                    return null
                }
                val requestAttachCode = json.getString("request~attach")
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
                val attachment = CommonUtils.convertToJSONObject(requestAttachDecode) ?: return null
                attachment.put("@id", requestsAttachItem.getString("@id"))
                val type = attachment.getString("@type")
                return MessageAttachment(type, attachment)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }
    }

}