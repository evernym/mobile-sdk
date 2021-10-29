package msdk.java.types;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
            JSONObject json = CommonUtils.convertToJSONObject(invite);
            if (json == null || !json.has("request~attach")) {
                return null;
            }

            String requestAttachCode = json.getString("request~attach");
            JSONArray requestsAttachItems = new JSONArray(requestAttachCode);
            if (requestsAttachItems.length() == 0) {
                return null;
            }

            JSONObject requestsAttachItem = requestsAttachItems.getJSONObject(0);
            JSONObject requestsAttachItemData = requestsAttachItem.getJSONObject("data");
            String requestsAttachItemBase = requestsAttachItemData.getString("base64");
            String requestAttachDecode = new String(Base64.decode(requestsAttachItemBase, Base64.NO_WRAP));
            JSONObject attachment = CommonUtils.convertToJSONObject(requestAttachDecode);
            if (attachment == null) {
                return null;
            }
            attachment.put("@id", requestsAttachItem.getString("@id"));
            String type = attachment.getString("@type");
            return new MessageAttachment(type, attachment);
        } catch (JSONException e) {
            e.printStackTrace();
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
