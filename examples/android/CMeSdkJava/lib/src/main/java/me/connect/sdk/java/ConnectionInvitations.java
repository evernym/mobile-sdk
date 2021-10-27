package me.connect.sdk.java;

import android.net.Uri;
import android.util.Base64;
import android.webkit.URLUtil;

import org.json.JSONObject;

import me.connect.sdk.java.message.AriesMessageType;

public class ConnectionInvitations {
    public enum InvitationType {
        Proprietary,
        Connection,
        OutOfBand
    }

    public static boolean isAriesConnectionInvitation(ConnectionInvitations.InvitationType type) {
        return type == ConnectionInvitations.InvitationType.Connection;
    }

    public static boolean isAriesOutOfBandConnectionInvitation(ConnectionInvitations.InvitationType type) {
        return type == ConnectionInvitations.InvitationType.OutOfBand;
    }

    public static boolean isCredentialAttachment(String type) {
        return type.contains("issue-credential");
    }

    public static boolean isProofAttachment(String type) {
        return type.contains("present-proof");
    }

    public static String getConnectionInvitationFromData(String data) {
        if (URLUtil.isValidUrl(data)) {
            Uri uri = Uri.parse(data);
            String ariesConnection = uri.getQueryParameter("c_i");
            String ariesOutOfBand = uri.getQueryParameter("oob");
            if (ariesConnection != null) {
                return new String(Base64.decode(ariesConnection, Base64.NO_WRAP));
            }
            if (ariesOutOfBand != null) {
                return new String(Base64.decode(ariesOutOfBand, Base64.NO_WRAP));
            }
            return OutOfBandHelper.readDataFromUrl(data);
        } else {
            return data;
        }
    }

    public static ConnectionsUtils.ConnDataHolder extractUserMetaFromInvitation(String invite) {
        JSONObject json = Utils.convertToJSONObject(invite);
        if (json == null) {
            return new ConnectionsUtils.ConnDataHolder("Unknown", "");
        }
        String label = json.optString("label");
        String logo = json.optString("profileUrl");
        return new ConnectionsUtils.ConnDataHolder(label, logo);
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
