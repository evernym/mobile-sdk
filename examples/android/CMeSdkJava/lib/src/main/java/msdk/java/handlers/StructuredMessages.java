package msdk.java.handlers;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.connection.ConnectionApi;

import org.json.JSONObject;

import java9.util.concurrent.CompletableFuture;
import msdk.java.logger.Logger;

/**
 * Class containing methods to work with structured messages;
 */
public class StructuredMessages {
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
}
