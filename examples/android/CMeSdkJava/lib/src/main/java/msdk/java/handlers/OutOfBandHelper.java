package msdk.java.handlers;

import android.util.Base64;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import msdk.java.utils.CommonUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OutOfBandHelper {
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

    public static JSONObject extractRequestAttach(String invite) {
        try {
            JSONObject json = CommonUtils.convertToJSONObject(invite);
            if (json != null && json.has("request~attach")) {
                String requestAttachCode = json.getString("request~attach");
                JSONArray requestsAttachItems = new JSONArray(requestAttachCode);
                if (requestsAttachItems.length() == 0) {
                    return null;
                }
                JSONObject requestsAttachItem = requestsAttachItems.getJSONObject(0);
                JSONObject requestsAttachItemData = requestsAttachItem.getJSONObject("data");
                String requestsAttachItemBase = requestsAttachItemData.getString("base64");
                String requestAttachDecode = new String(Base64.decode(requestsAttachItemBase, Base64.NO_WRAP));
                JSONObject result = CommonUtils.convertToJSONObject(requestAttachDecode);
                if (result != null) {
                    result.put("@id", requestsAttachItem.getString("@id"));
                    return result;
                }
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class OutOfBandInviteBuilder {
        private String parsedInvite;
        private String extractedAttachRequest;
        private JSONObject attach;
        private String existingConnection;
        private Connections.ConnectionMetadata userMeta;

        private OutOfBandInviteBuilder() {
        }

        public @NonNull
        OutOfBandInviteBuilder withParsedInvite(@NonNull String parsedInvite) {
            this.parsedInvite = parsedInvite;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withExtractedAttachRequest(String extractedAttachRequest) {
            this.extractedAttachRequest = extractedAttachRequest;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withAttach(@NonNull JSONObject attach) {
            this.attach = attach;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withUserMeta(Connections.ConnectionMetadata userMeta) {
            this.userMeta = userMeta;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withExistingConnection(String existingConnection) {
            this.existingConnection = existingConnection;
            return this;
        }

        public @NonNull
        OutOfBandInvite build() {
            return new OutOfBandInvite(
                    parsedInvite,
                    extractedAttachRequest,
                    attach,
                    userMeta,
                    existingConnection
            );
        }
    }

    public static class OutOfBandInvite {
        public String parsedInvite;
        public String extractedAttachRequest;
        public JSONObject attach;
        public Connections.ConnectionMetadata userMeta;
        public String existingConnection;

        public OutOfBandInvite(
                String parsedInvite,
                String extractedAttachRequest,
                JSONObject attach,
                Connections.ConnectionMetadata userMeta,
                String existingConnection
        ) {
            this.parsedInvite = parsedInvite;
            this.extractedAttachRequest = extractedAttachRequest;
            this.attach = attach;
            this.existingConnection = existingConnection;
            this.userMeta = userMeta;
        }

        public static OutOfBandInviteBuilder builder() {
            return new OutOfBandInviteBuilder();
        }
    }
}
