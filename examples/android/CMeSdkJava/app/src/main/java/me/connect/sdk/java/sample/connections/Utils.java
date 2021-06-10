package me.connect.sdk.java.sample.connections;

import android.net.Uri;
import android.util.Base64;
import android.webkit.URLUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import me.connect.sdk.java.Connections;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class Utils {
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
            return readDataFromUrl(invite);
        } else {
            return invite;
        }
    }

    public static ConnDataHolder extractUserMetaFromInvite(String invite) {
        try {
            JSONObject json = convertToJSONObject(invite);
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
        String name;
        String logo;

        public ConnDataHolder(String name, String logo) {
            this.name = name;
            this.logo = logo;
        }
    }

    public static JSONObject convertToJSONObject(String init) {
        try {
            if (init == null) {
                return null;
            }
            return new JSONObject(init);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static String readDataFromUrl(String url) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
