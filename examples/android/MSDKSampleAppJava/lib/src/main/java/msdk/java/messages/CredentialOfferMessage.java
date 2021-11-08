package msdk.java.messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class CredentialOfferMessage {
    public String id;
    public String name;
    public JSONObject attributes;
    public String offer;
    public String threadId;

    public CredentialOfferMessage(String id, String name, JSONObject attributes, String offer, String threadId) {
        this.id = id;
        this.name = name;
        this.attributes = attributes;
        this.offer = offer;
        this.threadId = threadId;
    }

    public static CredentialOfferMessage parse(Message msg) {
        try {
            JSONObject data = new JSONArray(msg.getPayload()).getJSONObject(0);
            String id = data.getString("claim_id");
            String thread_id = data.getString("thread_id");
            String name = data.getString("claim_name");
            JSONObject attributes = data.getJSONObject("credential_attrs");
            return new CredentialOfferMessage(
                    id,
                    name,
                    attributes,
                    msg.getPayload(),
                    thread_id
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
