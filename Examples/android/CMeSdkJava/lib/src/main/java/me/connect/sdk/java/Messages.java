package me.connect.sdk.java;


import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.MessageStatusType;
import me.connect.sdk.java.message.MessageType;

/**
 * Class containing methods to work with messages.
 */
public class Messages {
    public static final String TAG = "ConnectMeVcx";

    private Messages() {
    }

    /**
     * Retrieve pending messages.
     *
     * @param serializedConnection String containing JSON with serialized connection details.
     * @param messageType          Type of messages to retrieve
     * @return List of {@link Message}
     */
    public static @NonNull
    CompletableFuture<List<Message>> getPendingMessages(@NonNull String serializedConnection,
                                                        @NonNull MessageType messageType) {
        Logger.getInstance().i("Retrieving pending messages");
        CompletableFuture<List<Message>> result = new CompletableFuture<>();
        try {
            String pwDid = new JSONObject(serializedConnection).getJSONObject("data").getString("pw_did");
            UtilsApi.vcxGetMessages(MessageStatusType.PENDING, null, pwDid).whenComplete((messagesString, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to retrieve messages: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    List<Message> messages = new ArrayList<>();
                    JSONArray messagesJson = new JSONArray(messagesString);
                    for (int i = 0; i < messagesJson.length(); i++) {
                        JSONArray msgsJson = messagesJson.getJSONObject(i).optJSONArray("msgs");
                        if (msgsJson != null) {
                            for (int j = 0; j < msgsJson.length(); j++) {
                                JSONObject message = msgsJson.getJSONObject(j);
                                String type = message.getString("type");
                                String msgType;
                                String messageUid = message.getString("uid");
                                String payload = message.getString("decryptedPayload");
                                String msg = new JSONObject(payload).getString("@msg");
                                //Fixme workaround to check message type in different protocols
                                if (type.equals("aries")) {
                                    if (msg.startsWith("[")) { // cred offers have array as msg
                                        type = MessageType.CREDENTIAL_OFFER.getAries();
                                    } else {
                                        JSONObject msgJson = new JSONObject(msg);
                                        String mt = msgJson.getString("@type");
                                        if (!mt.startsWith("{")) {
                                            continue;
                                        }
                                        type = new JSONObject(mt).getString("name");
                                    }
                                    msgType = messageType.getAries();
                                } else {
                                    msgType = messageType.getProprietary();
                                }
                                if (type.equals(msgType)) {
                                    messages.add(new Message(messageUid, msg));
                                }
                            }
                        }
                    }
                    result.complete(messages);
                } catch (JSONException ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }
}
