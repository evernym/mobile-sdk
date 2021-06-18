package me.connect.sdk.java;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProofRequests {
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
            return Utils.convertToJSONObject(requestAttachDecode);
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
