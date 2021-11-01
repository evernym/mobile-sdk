package msdk.java.messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class CredentialOffer {
    public String id;
    public String name;
    public String attributes;
    public String offer;
    public String threadId;

    public CredentialOffer(String id, String name, String attributes, String offer, String threadId) {
        this.id = id;
        this.name = name;
        this.attributes = attributes;
        this.offer = offer;
        this.threadId = threadId;
    }

    public static CredentialOffer parseCredentialOfferMessage(Message msg) {
        try {
            JSONObject data = new JSONArray(msg.getPayload()).getJSONObject(0);
            String id = data.getString("claim_id");
            String thread_id = data.getString("thread_id");
            String name = data.getString("claim_name");
            JSONObject attributesJson = data.getJSONObject("credential_attrs");
            StringBuilder attributes = new StringBuilder();
            Iterator<String> keys = attributesJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = attributesJson.getString(key);
                attributes.append(String.format("%s: %s\n", key, value));
            }
            return new CredentialOffer(
                    id,
                    name,
                    attributes.toString(),
                    msg.getPayload(),
                    thread_id
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
