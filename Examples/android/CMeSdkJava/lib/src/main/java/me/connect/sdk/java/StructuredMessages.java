package me.connect.sdk.java;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.connection.ConnectionApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.message.MessageHolder;
import me.connect.sdk.java.message.MessageUtils;
import me.connect.sdk.java.message.StructuredMessageHolder;

/**
 * Class containing methods to work with structured messages;
 */
public class StructuredMessages {
    public static final String TAG = "ConnectMeVcx";

    private StructuredMessages() {

    }

    /**
     * @param serializedConnection JSON string containing serialized connection
     * @param messageId            message ID
     * @param answer               nonce value of the answer
     * @return {@link CompletableFuture} containing message ID
     */
    public static @NonNull
    CompletableFuture<String> answer(@NonNull String serializedConnection, @NonNull String messageId,
                                     @NonNull String answer) {
        Logger.getInstance().i("Respond to structured message");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                byte[] encodedAnswer = Base64.encode(answer.getBytes(), Base64.NO_WRAP);
                try {
                    ConnectionApi.connectionSignData(conHandle, encodedAnswer, encodedAnswer.length).whenComplete((signature, e) -> {
                        if (e != null) {
                            Logger.getInstance().e("Failed to sign data: ", e);
                            result.completeExceptionally(e);
                            return;
                        }
                        try {
                            MessageHolder msg = MessageUtils.prepareAnswer(encodedAnswer, signature, messageId);
                            ConnectionApi.connectionSendMessage(conHandle, msg.getMessage(), msg.getMessageOptions()).whenComplete((r, t) -> {
                                if (t != null) {
                                    Logger.getInstance().e("Failed to send message: ", t);
                                    result.completeExceptionally(t);
                                } else {
                                    result.complete(r);
                                }
                            });
                        } catch (Exception ex) {
                            result.completeExceptionally(ex);
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

    /**
     * Temporary method to parse structured question message JSON string and extract {@link StructuredMessageHolder} from it.
     *
     * @param message JSON string containing Structured question message
     * @return parsed {@link StructuredMessageHolder}
     */
    public static StructuredMessageHolder extract(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String payload = jsonObject.getString("decryptedPayload");
            String msgString = new JSONObject(payload).getString("@msg");
            JSONObject msg = new JSONObject(msgString);
            String messageId = jsonObject.getString("uid");
            String id = msg.getString("@id");
            String questionText = msg.getString("question_text");
            String questionDetail = msg.getString("question_detail");
            ArrayList<StructuredMessageHolder.Response> responses = new ArrayList<>();
            JSONArray jsonResponses = msg.getJSONArray("valid_responses");
            for (int i = 0; i < jsonResponses.length(); i++) {
                JSONObject response = jsonResponses.getJSONObject(i);
                String text = response.getString("text");
                String nonce = response.getString("nonce");
                StructuredMessageHolder.Response res = new StructuredMessageHolder.Response(text, nonce);
                responses.add(res);
            }
            return new StructuredMessageHolder(id, questionText, questionDetail, responses, messageId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
