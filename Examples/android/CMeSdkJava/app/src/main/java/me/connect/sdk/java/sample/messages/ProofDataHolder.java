package me.connect.sdk.java.sample.messages;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import me.connect.sdk.java.message.Message;

public class ProofDataHolder {
    public String threadId;
    public String name;
    public String attributes;
    public String proofReq;

    public ProofDataHolder(String threadId, String name, String attributes, String proofReq) {
        this.threadId = threadId;
        this.name = name;
        this.attributes = attributes;
        this.proofReq = proofReq;
    }

    public static ProofDataHolder extractRequestedFieldsFromProofMessage(Message msg) {
        try {
            JSONObject json = new JSONObject(msg.getPayload());
            JSONObject data = json.getJSONObject("proof_request_data");
            String threadId = json.getString("thread_id");
            String name = data.getString("name");
            JSONObject requestedAttrs = data.getJSONObject("requested_attributes");
            Iterator<String> keys = requestedAttrs.keys();
            StringBuilder attributes = new StringBuilder();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = requestedAttrs.getJSONObject(key).getString("name");
                attributes.append(value);
                if (keys.hasNext()) {
                    attributes.append(", ");
                }
            }
            return new ProofDataHolder(threadId, name, attributes.toString(), msg.getPayload());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
