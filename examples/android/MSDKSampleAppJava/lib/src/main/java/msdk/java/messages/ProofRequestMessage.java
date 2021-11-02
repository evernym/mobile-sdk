package msdk.java.messages;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import msdk.java.utils.CommonUtils;

public class ProofRequestMessage {
    public String threadId;
    public String name;
    public String attributes;
    public String proofReq;

    public ProofRequestMessage(String threadId, String name, String attributes, String proofReq) {
        this.threadId = threadId;
        this.name = name;
        this.attributes = attributes;
        this.proofReq = proofReq;
    }

    public static ProofRequestMessage parse(Message msg) {
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
            return new ProofRequestMessage(threadId, name, attributes.toString(), msg.getPayload());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject decodeProofRequestAttach(JSONObject proofAttach) {
        try {
            String requestAttachCode = proofAttach.getString("request_presentations~attach");
            JSONArray requestsAttachItems = new JSONArray(requestAttachCode);
            if (requestsAttachItems.length() == 0) {
                return null;
            }
            JSONObject requestsAttachItem = requestsAttachItems.getJSONObject(0);
            JSONObject requestsAttachItemData = requestsAttachItem.getJSONObject("data");
            String requestsAttachItemBase = requestsAttachItemData.getString("base64");
            String requestAttachDecode = new String(Base64.decode(requestsAttachItemBase, Base64.NO_WRAP));
            return CommonUtils.convertToJSONObject(requestAttachDecode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String extractRequestedAttributesFromProofRequest(JSONObject decodedProofAttach) {
        try {
            if (decodedProofAttach != null) {
                return decodedProofAttach.getJSONObject("requested_attributes").toString();
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String extractRequestedNameFromProofRequest(JSONObject decodedProofAttach) {
        try {
            if (decodedProofAttach != null) {
                return decodedProofAttach.getString("name");
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
