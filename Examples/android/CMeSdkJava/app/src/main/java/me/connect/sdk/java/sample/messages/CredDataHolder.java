package me.connect.sdk.java.sample.messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import me.connect.sdk.java.message.Message;

public class CredDataHolder {
    public String id;
    public String name;
    public String attributes;
    public String offer;

    public CredDataHolder(String id, String name, String attributes, String offer) {
        this.id = id;
        this.name = name;
        this.attributes = attributes;
        this.offer = offer;
    }

    public static CredDataHolder extractDataFromCredentialsOfferMessage(Message msg) {
        try {
            JSONObject data = new JSONArray(msg.getPayload()).getJSONObject(0);
            String id = data.getString("claim_id");
            String name = data.getString("claim_name");
            JSONObject attributesJson = data.getJSONObject("credential_attrs");
            StringBuilder attributes = new StringBuilder();
            Iterator<String> keys = attributesJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = attributesJson.getString(key);
                attributes.append(String.format("%s: %s\n", key, value));
            }
            return new CredDataHolder(id, name, attributes.toString(), msg.getPayload());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
