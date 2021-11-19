package msdk.kotlin.sample.messages

import android.webkit.URLUtil
import com.evernym.sdk.vcx.utils.UtilsApi
import msdk.kotlin.sample.handlers.Connections.ConnectionMetadata
import msdk.kotlin.sample.types.AriesMessageType
import msdk.kotlin.sample.utils.CommonUtils
import org.json.JSONObject
import java.util.concurrent.ExecutionException

object ConnectionInvitation {
    fun isAriesConnectionInvitation(type: InvitationType): Boolean {
        return type == InvitationType.Connection
    }

    fun isAriesOutOfBandConnectionInvitation(type: InvitationType): Boolean {
        return type == InvitationType.OutOfBand
    }

    fun getConnectionInvitationFromData(data: String): String {
        return if (URLUtil.isValidUrl(data)) {
            try {
                UtilsApi.vcxResolveMessageByUrl(data).get();
            } catch (ex: ExecutionException) {
                ex.printStackTrace()
                data
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
                data
            }
        } else {
            data
        }
    }

    fun extractUserMetaFromInvitation(invite: String?): ConnectionMetadata {
        val json =
            CommonUtils.convertToJSONObject(invite) ?: return ConnectionMetadata("Unknown", "")
        val label = json.optString("label")
        val logo = json.optString("profileUrl")
        return ConnectionMetadata(label, logo)
    }

    fun getInvitationType(invite: String?): InvitationType? {
        try {
            val json = JSONObject(invite)
            val invitationType = json.optString("@type")
            if (invitationType.contains(AriesMessageType.OUTOFBAND_INVITATION)) {
                return InvitationType.OutOfBand
            }
            if (invitationType.contains(AriesMessageType.CONNECTION_INVITATION)) {
                return InvitationType.Connection
            }
            throw Exception("Invalid invite format")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(Exception::class)
    fun compareInvites(newInvite: String?, storedInvite: String?): Boolean {
        val newJson = JSONObject(newInvite)
        val storedJson = JSONObject(storedInvite)
        val newPublicDid = newJson.optString("public_did")
        val storedPublicDid = storedJson.optString("public_did")
        return if (!storedPublicDid.isEmpty()) {
            storedPublicDid == newPublicDid
        } else {
            val newDid = newJson.getJSONArray("recipientKeys").getString(0)
            val storedDid = storedJson.getJSONArray("recipientKeys").optString(0)
            storedDid == newDid
        }
    }

    enum class InvitationType {
        Connection, OutOfBand
    }
}