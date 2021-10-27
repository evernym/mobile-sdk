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
     * @param structuredMessage    structured message
     * @param answer               value of the answer
     * @return {@link CompletableFuture} containing message ID
     */
    public static @NonNull
    CompletableFuture<Void> answer(
            @NonNull String serializedConnection,
            @NonNull String structuredMessage,
            @NonNull JSONObject answer
    ) {
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
                    System.out.println(answer + "CompletableFuture1");
                    ConnectionApi.connectionSendAnswer(conHandle, structuredMessage, answer.toString()).whenComplete((res, er) -> {
                        if (er != null) {
                            Logger.getInstance().e("Failed to send answer: ", er);
                            result.completeExceptionally(er);
                        }
                        System.out.println(answer + "CompletableFuture2");
                        result.complete(null);
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
