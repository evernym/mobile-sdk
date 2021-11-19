package msdk.java.messages;

import android.webkit.URLUtil;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import msdk.java.handlers.Connections.ConnectionMetadata;
import msdk.java.types.AriesMessageType;
import msdk.java.utils.CommonUtils;

public class ConnectionInvitation {
    public enum InvitationType {
        Connection,
        OutOfBand
    }

    public static boolean isAriesConnectionInvitation(ConnectionInvitation.InvitationType type) {
        return type == ConnectionInvitation.InvitationType.Connection;
    }

    public static boolean isAriesOutOfBandConnectionInvitation(ConnectionInvitation.InvitationType type) {
        return type == ConnectionInvitation.InvitationType.OutOfBand;
    }

    public static String getConnectionInvitationFromData(String data) {
        if (URLUtil.isValidUrl(data)) {
            try {
                return UtilsApi.vcxResolveMessageByUrl(data).get();
            } catch (InterruptedException | ExecutionException | VcxException exception) {
                exception.printStackTrace();
                return null;
            }
        } else {
            return data;
        }
    }

    public static ConnectionMetadata extractUserMetaFromInvitation(String invite) {
        JSONObject json = CommonUtils.convertToJSONObject(invite);
        if (json == null) {
            return new ConnectionMetadata("Unknown", "");
        }
        String label = json.optString("label");
        String logo = json.optString("profileUrl");
        return new ConnectionMetadata(label, logo);
    }

    public static InvitationType getInvitationType(String invite) {
        try {
            JSONObject json = new JSONObject(invite);
            String invitationType = json.optString("@type");
            if (invitationType.contains(AriesMessageType.OUTOFBAND_INVITATION)) {
                return InvitationType.OutOfBand;
            }
            if (invitationType.contains(AriesMessageType.CONNECTION_INVITATION)) {
                return InvitationType.Connection;
            }
            throw new Exception("Invalid invite format");
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean compareInvites(String newInvite, String storedInvite) throws Exception {
        JSONObject newJson = new JSONObject(newInvite);
        JSONObject storedJson = new JSONObject(storedInvite);
        String newPublicDid = newJson.optString("public_did");
        String storedPublicDid = storedJson.optString("public_did");
        if (!storedPublicDid.isEmpty()) {
            return storedPublicDid.equals(newPublicDid);
        } else {
            String newDid = newJson.getJSONArray("recipientKeys").getString(0);
            String storedDid = storedJson.getJSONArray("recipientKeys").optString(0);
            return storedDid.equals(newDid);
        }
    }
}
