package msdk.kotlin.sample.types

import com.evernym.sdk.vcx.VcxException
import com.evernym.sdk.vcx.utils.UtilsApi
import msdk.kotlin.sample.utils.CommonUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.ExecutionException

class MessageAttachment(var type: String, var data: JSONObject) {
    val isCredentialAttachment: Boolean
        get() = type.contains("issue-credential")

    val isProofAttachment: Boolean
        get() = type.contains("present-proof")

    companion object {
        fun parse(invite: String?): MessageAttachment? {
            try {
                val attachment = UtilsApi.vcxExtractAttachedMessage(fixForRequestField(invite)).get()
                val attachmentJson = CommonUtils.convertToJSONObject(attachment)!!
                attachmentJson.put("@id", getIdFromInvite(invite))
                val type = attachmentJson.getString("@type")
                return MessageAttachment(type, attachmentJson)
            } catch (e: VcxException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
            return null
        }

        private fun fixForRequestField(invite: String?): String? {
            try {
                val fixInvite = JSONObject()
                val inviteJson = CommonUtils.convertToJSONObject(invite)
                fixInvite.put("goal", inviteJson?.getString("goal"))
                fixInvite.put("service", inviteJson?.getJSONArray("service"))
                fixInvite.put("@id", inviteJson?.getString("@id"))
                fixInvite.put("@type", inviteJson?.getString("@type"))
                fixInvite.put("profileUrl", inviteJson?.getString("profileUrl"))
                fixInvite.put("handshake_protocols", inviteJson?.getJSONArray("handshake_protocols"))
                fixInvite.put("label", inviteJson?.getString("label"))
                fixInvite.put("goal_code", inviteJson?.getString("goal_code"))
                fixInvite.put("public_did", inviteJson?.getString("public_did"))
                fixInvite.put("requests~attach", inviteJson?.getJSONArray("request~attach"))
                return fixInvite.toString()
            } catch (exp: JSONException) {
                exp.printStackTrace()
            }
            return null
        }

        private fun getIdFromInvite(invite: String?): String? {
            try {
                val inviteJson = CommonUtils.convertToJSONObject(invite)
                if (inviteJson != null && !inviteJson.has("request~attach")) {
                    return null
                }
                val requestAttachCode = inviteJson!!.getString("request~attach")
                val requestsAttachItems = JSONArray(requestAttachCode)
                if (requestsAttachItems.length() == 0) {
                    return null
                }
                val requestsAttachItem = requestsAttachItems.getJSONObject(0)
                return requestsAttachItem.getString("@id")
            } catch (exp: JSONException) {
                exp.printStackTrace()
            }
            return null
        }
    }
}