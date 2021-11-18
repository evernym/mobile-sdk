package msdk.java.messages;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import msdk.java.utils.CommonUtils;

public class ProofRequestMessage {
    public String threadId;
    public String name;
    public JSONObject attributes;
    public String proofReq;

    public ProofRequestMessage(String threadId, String name, JSONObject attributes, String proofReq) {
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
            return new ProofRequestMessage(threadId, name, requestedAttrs, msg.getPayload());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject decodeProofRequestAttach(JSONObject proofAttach) {
        try {
            String attachment = UtilsApi.vcxExtractAttachedMessage(proofAttach.toString()).get();
            return CommonUtils.convertToJSONObject(attachment);
        } catch (VcxException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject extractRequestedAttributesFromProofRequest(JSONObject proofRequest) {
        try {
            if (proofRequest != null) {
                return proofRequest.getJSONObject("requested_attributes");
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
