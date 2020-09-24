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
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((handle, er) -> {
                if (er != null) {
                    Logger.getInstance().e("Failed to deserialize connection: ", er);
                    result.completeExceptionally(er);
                    return;
                }
                try {
                    ConnectionApi.connectionGetPwDid(handle).whenComplete((pwDid, e) -> {
                        if (e != null) {
                            Logger.getInstance().e("Failed to get pwDid: ", e);
                            result.completeExceptionally(e);
                            return;
                        }
                        try {
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
                                                JSONObject payload = new JSONObject(message.getString("decryptedPayload"));
                                                String type = payload.getJSONObject("@type").getString("name");
                                                if (messageType.matches(type)) {
                                                    String messageUid = message.getString("uid");
                                                    String msg = payload.getString("@msg");
                                                    messages.add(new Message(messageUid, msg, type));
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
                    });
                } catch (Exception ex) {
                    result.completeExceptionally(ex);
                }
            });


        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    static String prepareUpdateMessage(String pairwiseDid, String messsageId) {
        return String.format("[{\"pairwiseDID\" : \"%s\", \"uids\": [\"%s\"]}]", pairwiseDid, messsageId);
    }
}
