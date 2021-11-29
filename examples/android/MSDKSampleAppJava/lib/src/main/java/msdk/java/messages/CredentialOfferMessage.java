package msdk.java.messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import msdk.java.utils.CommonUtils;

public class CredentialOfferMessage {
    public String name;
    public JSONObject attributes;
    public String offer;
    public String threadId;

    public CredentialOfferMessage(String name, JSONObject attributes, String offer, String threadId) {
        this.name = name;
        this.attributes = attributes;
        this.offer = offer;
        this.threadId = threadId;
    }

    public static CredentialOfferMessage parse(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String threadId = CommonUtils.getThreadId(json);
            String name = json.getString("comment");
            JSONObject attributes = extractAttributesFromCredentialOffer(json);
            return new CredentialOfferMessage(
                    name,
                    attributes,
                    message,
                    threadId
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject extractAttributesFromCredentialOffer(JSONObject offer) {
        JSONObject attributes = new JSONObject();
        try {
            JSONArray previewAttributes = offer.getJSONObject("credential_preview").getJSONArray("attributes");

            for (int i = 0; i < previewAttributes.length(); i++) {
                JSONObject attribute = previewAttributes.getJSONObject(i);
                attributes.put(attribute.getString("name"), attribute.getString("value"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return attributes;
    }
}
