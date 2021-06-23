package me.connect.sdk.java;


import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
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
    CompletableFuture<List<Message>> getPendingMessages(
            @NonNull MessageType messageType,
            String uids,
            String pwdids
    ) {
        Logger.getInstance().i("Retrieving pending messages");
        CompletableFuture<List<Message>> result = new CompletableFuture<>();
        try {
            if (uids == null && pwdids == null) {
                UtilsApi.vcxGetMessages(MessageStatusType.PENDING, null, null).whenComplete((messagesString, err) -> {
                    if (err != null) {
                        Logger.getInstance().e("Failed to retrieve messages: ", err);
                        result.completeExceptionally(err);
                        return;
                    }
                    try {
                        List<Message> messages = new ArrayList<>();
                        JSONArray messagesJson = new JSONArray(messagesString);
                        System.out.println(messagesString);
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

            } else {
                UtilsApi.vcxGetMessages(MessageStatusType.PENDING, uids, pwdids).whenComplete((messagesString, err) -> {
                    if (err != null) {
                        Logger.getInstance().e("Failed to get message: ", err);
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
            }
        } catch (VcxException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Retrieve pending messages.
     *
     * @return List of {@link Message}
     */
    public static @NonNull
    CompletableFuture<List<Message>> getAllPendingMessages() {
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
                    System.out.println(messagesJson + "getAllPendingMessages");

                    for (int i = 0; i < messagesJson.length(); i++) {
                        JSONArray msgsJson = messagesJson.getJSONObject(i).optJSONArray("msgs");
                        String pairwiseDID = messagesJson.getJSONObject(i).getString("pairwiseDID");
                        if (msgsJson != null) {
                            for (int j = 0; j < msgsJson.length(); j++) {
                                JSONObject message = msgsJson.getJSONObject(j);
                                JSONObject payload = new JSONObject(message.getString("decryptedPayload"));
                                String type = payload.getJSONObject("@type").getString("name");

                                String messageUid = message.getString("uid");
                                String msg = payload.getString("@msg");
                                String status = message.getString("statusCode");
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
            ex.printStackTrace();
        }
        return result;
    }

    public static @NonNull
    CompletableFuture<Boolean> waitHandshakeReuse() {
        Logger.getInstance().i("Retrieving pending messages");
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        try {
            UtilsApi.vcxGetMessages(MessageStatusType.PENDING, null, null).whenComplete((messagesString, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to retrieve messages: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    JSONArray messagesJson = new JSONArray(messagesString);
                    for (int i = 0; i < messagesJson.length(); i++) {
                        JSONArray msgsJson = messagesJson.getJSONObject(i).optJSONArray("msgs");
                        if (msgsJson != null) {
                            for (int j = 0; j < msgsJson.length(); j++) {
                                JSONObject message = msgsJson.getJSONObject(j);
                                JSONObject payload = new JSONObject(message.getString("decryptedPayload"));
                                String type = payload.getJSONObject("@type").getString("name");
                                if (type.equals("handshake-reuse-accepted")) {
                                    result.complete(true);
                                }
                            }
                        }
                    }

                } catch (JSONException ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (VcxException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static void updateMessageStatus(String pwDid, String messageId) {
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            String jsonMsg = prepareUpdateMessage(pwDid, messageId);
            UtilsApi.vcxUpdateMessages(MessageStatusType.REVIEWED, jsonMsg).whenComplete((v1, error) -> {
                if (error != null) {
                    Logger.getInstance().e("Failed to update messages", error);
                    result.completeExceptionally(error);
                    return;
                } else {
                    result.complete(null);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
    }

    static String prepareUpdateMessage(String pairwiseDid, String messsageId) {
        return String.format("[{\"pairwiseDID\" : \"%s\", \"uids\": [\"%s\"]}]", pairwiseDid, messsageId);
    }

    public static CompletableFuture<Message> downloadMessageByTypeAndThreadId(
            MessageType messageType,
            String id
    ) {
        CompletableFuture<Message> result = new CompletableFuture<>();
        Messages.getAllPendingMessages().handle((messages, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                result.completeExceptionally(throwable);
            }
            for (Message message : messages) {
                try {
                    String msg = message.getPayload();
                    JSONObject msgPayload = new JSONObject(msg);
                    String type = msgPayload.getString("@type");

                    if (messageType.equals(MessageType.CREDENTIAL) && messageType.matchesValue(type)) {
                        JSONObject thread = msgPayload.getJSONObject("~thread");
                        String thid = thread.getString("thid");

                        if (thid.equals(id)) {
                            result.complete(message);
                        }
                        result.complete(null);
                    }
                    if (messageType.equals(MessageType.CONNECTION_RESPONSE) && messageType.matchesValue(type)) {
                        String pwDid = message.getPwDid();

                        if (pwDid.equals(id)) {
                            result.complete(message);
                        }
                        result.complete(null);
                    }
                    if (messageType.equals(MessageType.ACK) && messageType.matches(type)) {
                        result.complete(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            result.complete(null);
            return null;
        });
        return result;
    }
}
