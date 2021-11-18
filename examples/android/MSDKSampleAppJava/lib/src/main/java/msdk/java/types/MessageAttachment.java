package msdk.java.types;

import android.util.Base64;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;
import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;
import msdk.java.messages.Message;
import msdk.java.utils.CommonUtils;

public class MessageAttachment {
    public String type;
    public JSONObject data;

    public MessageAttachment(String type, JSONObject data) {
        this.type = type;
        this.data = data;
    }

    public static MessageAttachment parse(String invite) {
        try {
            String attachment = UtilsApi.vcxExtractAttachedMessage(invite).get();
            JSONObject attachmentJson = CommonUtils.convertToJSONObject(attachment);
            assert attachmentJson != null;
            attachmentJson.put("@id", getIdFromInvite(invite));
            String type = attachmentJson.getString("@type");
            return new MessageAttachment(type, attachmentJson);
        } catch (VcxException | JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getIdFromInvite(String invite) {
        try {
            JSONObject inviteJson = CommonUtils.convertToJSONObject(invite);

            if (inviteJson != null && !inviteJson.has("request~attach")) {
                return null;
            }

            String requestAttachCode = inviteJson.getString("request~attach");
            JSONArray requestsAttachItems = new JSONArray(requestAttachCode);
            if (requestsAttachItems.length() == 0) {
                return null;
            }

            JSONObject requestsAttachItem = requestsAttachItems.getJSONObject(0);
            return requestsAttachItem.getString("@id");
        } catch (JSONException exp) {
            exp.printStackTrace();
        }
        return null;
    }

    public boolean isCredentialAttachment() {
        return this.type.contains("issue-credential");
    }

    public boolean isProofAttachment() {
        return this.type.contains("present-proof");
    }
}
