package me.connect.sdk.java.message;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class MessageUtils {
    private MessageUtils() {
    }

    /**
     * Prepare answer to be sent in response to structured messasge
     *
     * @param encodedAnswer byte array containing Base-64 encoded answer nonce
     * @param signature     byte array containing Base-64 encoded signature of answer
     * @param refMessageId  String containing reference message ID of question
     * @param id            question ID
     * @return {@link MessageHolder} containing message and message options Strings used to answer the structured message
     * @throws org.json.JSONException if failed to create JSON object during message string creation
     */
    public static MessageHolder prepareAnswer(byte[] encodedAnswer, byte[] signature, String refMessageId, String id) throws JSONException {
        JSONObject sig = new JSONObject();
        sig.put("signature", Base64.encodeToString(signature, Base64.NO_WRAP));
        sig.put("sig_data", new String(encodedAnswer));
        long time = System.currentTimeMillis() / 1000;
        sig.put("timestamp", String.valueOf(time));
        JSONObject message = new JSONObject();
        message.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/committedanswer/1.0/answer");
        message.put("response.@sig", sig);
        message.put("@id", UUID.randomUUID().toString());
        JSONObject thread = new JSONObject();
        thread.put("thid", id);
        message.put("~thread", thread);

        JSONObject messageOptions = new JSONObject();
        messageOptions.put("msg_type", "Answer");
        messageOptions.put("msg_title", "Peer sent answer");
        messageOptions.put("ref_msg_id", refMessageId);
        return new MessageHolder(message.toString(), messageOptions.toString());
    }
}
