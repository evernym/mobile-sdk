package msdk.java.handlers;


import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import java9.util.concurrent.CompletableFuture;
import msdk.java.logger.Logger;
import msdk.java.messages.Message;
import msdk.java.types.MessageStatusType;
import msdk.java.types.MessageType;
import msdk.java.types.UpdateMessageStatusBody;

/**
 * Class containing methods to work with messages.
 */
public class Messages {
    public static final String TAG = "ConnectMeVcx";

    private Messages() {
    }

    /**
     * Retrieve all pending messages.
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

    /*
    * Download and find message from the specific thread
    * */
    public static CompletableFuture<Message> downloadNextMessageFromTheThread(
            MessageType messageType,
            String threadId
    ) {
        CompletableFuture<Message> result = new CompletableFuture<>();
        Messages.getAllPendingMessages().handle((messages, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                result.completeExceptionally(throwable);
            }
            Message foundMessage = null;
            for (Message message : messages) {
                try {
                    String msg = message.getPayload();
                    String pwDid = message.getPwDid();
                    JSONObject msgPayload = new JSONObject(msg);
                    String type = msgPayload.getString("@type");
                    JSONObject thread = msgPayload.getJSONObject("~thread");
                    String thid = thread.getString("thid");

                    if (messageType.equals(MessageType.CREDENTIAL) && messageType.matchesValue(type) && thid.equals(threadId)) {
                        foundMessage = message;
                        break;
                    }
                    if (messageType.equals(MessageType.CONNECTION_RESPONSE) && messageType.matchesValue(type) && pwDid.equals(threadId)) {
                        foundMessage = message;
                        break;
                    }
                    if (messageType.equals(MessageType.ACK) && messageType.matchesValue(type)) {
                        foundMessage = message;
                        break;
                    }
                    if (messageType.equals(MessageType.HANDSHAKE) && messageType.matchesValue(type) && thid.equals(threadId)) {
                        foundMessage = message;
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            result.complete(foundMessage);
            return null;
        });
        return result;
    }

    /*
    * Update status of the message as reviewed
    * */
    public static void updateMessageStatus(String pwDid, String messageId) {
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            String messagesToUpdate =
                    new UpdateMessageStatusBody()
                        .addMessage(pwDid, messageId)
                        .toJSON();
            UtilsApi.vcxUpdateMessages(MessageStatusType.REVIEWED, messagesToUpdate).whenComplete((v1, error) -> {
                if (error != null) {
                    Logger.getInstance().e("Failed to update messages", error);
                    result.completeExceptionally(error);
                } else {
                    result.complete(null);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
    }
}
