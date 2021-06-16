package me.connect.sdk.java;

import android.net.Uri;
import android.util.Base64;
import android.webkit.URLUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class ConnectionsUtils {
    public static boolean isProprietaryType(Connections.InvitationType type) {
        return type == Connections.InvitationType.Proprietary;
    }

    public static boolean isAriesConnection(Connections.InvitationType type) {
        return type == Connections.InvitationType.Connection;
    }

    public static boolean isOutOfBandType(Connections.InvitationType type) {
        return type == Connections.InvitationType.OutOfBand;
    }

    public static boolean isCredentialInviteType(String type) {
        return type.contains("issue-credential");
    }

    public static boolean isProofInviteType(String type) {
        return type.contains("present-proof");
    }

    public static String parseInvite(String invite) {
        if (URLUtil.isValidUrl(invite)) {
            Uri uri = Uri.parse(invite);
            String ariesConnection = uri.getQueryParameter("c_i");
            String ariesOutOfBand = uri.getQueryParameter("oob");
            if (ariesConnection != null) {
                return new String(Base64.decode(ariesConnection, Base64.NO_WRAP));
            }
            if (ariesOutOfBand != null) {
                return new String(Base64.decode(ariesOutOfBand, Base64.NO_WRAP));
            }
            return OutOfBandHelper.readDataFromUrl(invite);
        } else {
            return invite;
        }
    }

    public static ConnDataHolder extractUserMetaFromInvite(String invite) {
        try {
            JSONObject json = Utils.convertToJSONObject(invite);
            if (json != null && json.has("label")) {
                String label = json.getString("label");
                String logo = null;
                if (json.has("profileUrl")) {
                    logo = json.getString("profileUrl");
                }
                return new ConnDataHolder(label, logo);
            }
            JSONObject data = json.optJSONObject("s");
            if (data != null) {
                return new ConnDataHolder(data.getString("n"), data.getString("l"));
            } else {
                // workaround in case details missing
                String sourceId = json.getString("id");
                return new ConnDataHolder(sourceId, null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class ConnDataHolder {
        public String name;
        public String logo;

        public ConnDataHolder(String name, String logo) {
            this.name = name;
            this.logo = logo;
        }
    }
}
