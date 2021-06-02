package me.connect.sdk.java;


import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;
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
     * @param messageType          Type of messages to retrieve
     * @return List of {@link Message}
     */
    public static @NonNull
    CompletableFuture<List<Message>> getPendingMessages(@NonNull MessageType messageType) {
        Logger.getInstance().i("Retrieving pending messages");
        CompletableFuture<List<Message>> result = new CompletableFuture<>();
        try {
            UtilsApi.vcxGetMessages(MessageStatusType.PENDING, null, null).whenComplete((messagesString, err) -> {
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
                        String pairwiseDID = messagesJson.getJSONObject(i).getString("pairwiseDID");
                        if (msgsJson != null) {
                            for (int j = 0; j < msgsJson.length(); j++) {
                                JSONObject message = msgsJson.getJSONObject(j);
                                JSONObject payload = new JSONObject(message.getString("decryptedPayload"));
                                String type = payload.getJSONObject("@type").getString("name");
                                if (messageType.matches(type)) {
                                    String messageUid = message.getString("uid");
                                    String msg = payload.getString("@msg");
                                    String status = message.getString("statusCode");
                                    messages.add(new Message(pairwiseDID, messageUid, msg, type,status));
                                }
                            }
                        }
                    }
                    result.complete(messages);
                } catch (JSONException ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (VcxException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Download message.
     *
     * @param messageId message ID
     * @return List of {@link Message}
     */
    public static CompletableFuture<List<Message>> downloadMessage(@NonNull String messageId) {
        CompletableFuture<List<Message>> result = new CompletableFuture<>();
        try {

            UtilsApi.vcxGetMessage(messageId).whenComplete((messageString, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to get message: ", err);
                    return;
                }
                try {
                    List<Message> messages = new ArrayList<>();
                    JSONArray messagesJson = new JSONArray(messageString);
                    for (int i = 0; i < messagesJson.length(); i++) {
                        JSONArray msgsJson = messagesJson.getJSONObject(i).optJSONArray("msgs");
                        String pairwiseDID = messagesJson.getJSONObject(i).getString("pairwiseDID");
                        if (msgsJson != null) {
                            for (int j = 0; j < msgsJson.length(); j++) {
                                JSONObject message = msgsJson.getJSONObject(j);
                                JSONObject payload = new JSONObject(message.getString("decryptedPayload"));
                                String type = payload.getJSONObject("@type").getString("name");
                                String messageUid = message.getString("uid");
                                String status = message.getString("statusCode");
                                String msg = payload.getString("@msg");
                                messages.add(new Message(pairwiseDID, messageUid, msg, type, status));
                            }
                        }
                    }
                    result.complete(messages);
                } catch (JSONException ex) {
                    result.completeExceptionally(ex);
                }

            });
        } catch (VcxException ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    static String prepareUpdateMessage(String pairwiseDid, String messsageId) {
        return String.format("[{\"pairwiseDID\" : \"%s\", \"uids\": [\"%s\"]}]", pairwiseDid, messsageId);
    }
}
