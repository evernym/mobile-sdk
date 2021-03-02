package me.connect.sdk.java;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.connection.ConnectionApi;
import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.MessageHolder;
import me.connect.sdk.java.message.MessageStatusType;
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
     * answer structured message
     *
     * @param serializedConnection JSON string containing serialized connection
     * @param messageId            message ID
     * @param type                 message type
     * @param structuredMessage    structured message
     * @param answer               value of the answer
     * @return {@link CompletableFuture} containing message ID
     */
    public static @NonNull
    CompletableFuture<Void> answer(@NonNull String serializedConnection, @NonNull String messageId, @NonNull String type,
                                   @NonNull String structuredMessage, @NonNull String answer) {
        if ("question".equals(type)) {
            return answerAries(serializedConnection, messageId, structuredMessage, answer);
        } else {
            return answerProprietary(serializedConnection, messageId, structuredMessage, answer);
        }
    }


    private static @NonNull
    CompletableFuture<Void> answerProprietary(@NonNull String serializedConnection, @NonNull String messageId,
                                              @NonNull String structuredMessage, @NonNull String answer) {
        Logger.getInstance().i("Respond to structured message");
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            JSONObject structMessageJson = new JSONObject(structuredMessage);
            JSONArray responses = structMessageJson.getJSONArray("valid_responses");
            String nonce = null;
            String msgId = structMessageJson.getString("@id");
            for (int i = 0; i < responses.length(); i++) {
                JSONObject response = responses.getJSONObject(i);
                String text = response.getString("text");
                if (text.equals(answer)) {
                    nonce = response.getString("nonce");
                    break;
                }
            }
            if (nonce == null) {
                result.completeExceptionally(new Exception("nonce was not found for selected answer"));
                return result;
            }
            final String answerNonce = nonce;
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                byte[] encodedAnswer = Base64.encode(answerNonce.getBytes(), Base64.NO_WRAP);
                try {
                    ConnectionApi.connectionSignData(conHandle, encodedAnswer, encodedAnswer.length).whenComplete((signature, e) -> {
                        if (e != null) {
                            Logger.getInstance().e("Failed to sign data: ", e);
                            result.completeExceptionally(e);
                            return;
                        }
                        try {
                            MessageHolder msg = MessageUtils.prepareAnswer(encodedAnswer, signature, messageId, msgId);
                            ConnectionApi.connectionSendMessage(conHandle, msg.getMessage(), msg.getMessageOptions()).whenComplete((r, t) -> {
                                if (t != null) {
                                    Logger.getInstance().e("Failed to send message: ", t);
                                    result.completeExceptionally(t);
                                } else {


                                    try {
                                        ConnectionApi.connectionGetPwDid(conHandle).whenComplete((pwDid, th) -> {
                                            if (th != null) {
                                                Logger.getInstance().e("Failed to get pwDid: ", th);
                                                result.completeExceptionally(th);
                                                return;
                                            }
                                            try {
                                                String jsonMsg = Messages.prepareUpdateMessage(pwDid, messageId);
                                                UtilsApi.vcxUpdateMessages(MessageStatusType.ANSWERED, jsonMsg).whenComplete((v1, error) -> {
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
                                        });
                                    } catch (Exception ex) {
                                        result.completeExceptionally(ex);
                                    }


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

    private static @NonNull
    CompletableFuture<Void> answerAries(@NonNull String serializedConnection, @NonNull String messageId,
                                        @NonNull String structuredMessage, @NonNull String answer) {
        Logger.getInstance().i("Respond to structured message");
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    JSONObject answerJson = new JSONObject();
                    answerJson.put("text", answer);
                    ConnectionApi.connectionSendAnswer(conHandle, structuredMessage, answerJson.toString()).whenComplete((res, er) -> {
                        if (er != null) {
                            Logger.getInstance().e("Failed to send answer: ", er);
                            result.completeExceptionally(er);
                            return;
                        }
                        try {
                            ConnectionApi.connectionGetPwDid(conHandle).whenComplete((pwDid, th) -> {
                                if (th != null) {
                                    Logger.getInstance().e("Failed to get pwDid: ", th);
                                    result.completeExceptionally(th);
                                    return;
                                }
                                try {
                                    String jsonMsg = Messages.prepareUpdateMessage(pwDid, messageId);
                                    UtilsApi.vcxUpdateMessages(MessageStatusType.ANSWERED, jsonMsg).whenComplete((v1, error) -> {
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
     * @param message {@link Message}
     * @return parsed {@link StructuredMessageHolder}
     */
    public static StructuredMessageHolder extract(Message message) {
        try {
            JSONObject msg = new JSONObject(message.getPayload());
            String id = msg.getString("@id");
            String questionText = msg.getString("question_text");
            String questionDetail = msg.getString("question_detail");
            ArrayList<StructuredMessageHolder.Response> responses = new ArrayList<>();
            JSONArray jsonResponses = msg.getJSONArray("valid_responses");
            for (int i = 0; i < jsonResponses.length(); i++) {
                JSONObject response = jsonResponses.getJSONObject(i);
                String text = response.getString("text");
                String nonce = response.optString("nonce");
                StructuredMessageHolder.Response res = new StructuredMessageHolder.Response(text, nonce);
                responses.add(res);
            }
            return new StructuredMessageHolder(id, message.getType(), questionText, questionDetail, responses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
