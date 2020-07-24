package me.connect.sdk.java;

import android.util.Log;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import java9.util.concurrent.CompletableFuture;
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
     * @return List of messages
     */
    public static @NonNull
    CompletableFuture<List<String>> getPendingMessages(@NonNull String serializedConnection,
                                                       @NonNull MessageType messageType) {
        Log.i(TAG, "Retrieving pending messages");
        CompletableFuture<List<String>> result = new CompletableFuture<>();
        try {
            String pwDid = new JSONObject(serializedConnection).getJSONObject("data").getString("pw_did");
            UtilsApi.vcxGetMessages(MessageStatusType.PENDING, null, pwDid).whenComplete((messagesString, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed to retrieve messages: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    List<String> messages = new ArrayList<>();
                    JSONArray messagesJson = new JSONArray(messagesString);
                    for (int i = 0; i < messagesJson.length(); i++) {
                        JSONArray msgsJson = messagesJson.getJSONObject(i).optJSONArray("msgs");
                        if (msgsJson != null) {
                            for (int j = 0; j < msgsJson.length(); j++) {
                                JSONObject message = msgsJson.getJSONObject(j);
                                String type = message.getString("type");
                                String msgType;
                                //Fixme workaround to check message type in different protocols
                                if (type.equals("aries")) {
                                    String payload = message.getString("decryptedPayload");
                                    String msg = new JSONObject(payload).getString("@msg");
                                    String mt = new JSONObject(msg).getString("@type");
                                    if (!mt.startsWith("{")) {
                                        continue;
                                    }
                                    type = new JSONObject(mt).getString("name");
                                    msgType = messageType.getAries();
                                } else {
                                    msgType = messageType.getProprietary();
                                }
                                if (type.equals(msgType)) {
                                    messages.add(message.toString());
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
