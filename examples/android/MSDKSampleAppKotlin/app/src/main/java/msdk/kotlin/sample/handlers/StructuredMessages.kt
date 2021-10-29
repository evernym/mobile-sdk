package msdk.kotlin.sample.handlers

import com.evernym.sdk.vcx.connection.ConnectionApi
import java9.util.concurrent.CompletableFuture
import msdk.kotlin.sample.logger.Logger
import org.json.JSONObject

/**
 * Class containing methods to work with structured messages;
 */
object StructuredMessages {
    /**
     * answer structured message
     *
     * @param serializedConnection JSON string containing serialized connection
     * @param structuredMessage    structured message
     * @param answer               value of the answer
     * @return [CompletableFuture] containing message ID
     */
    fun answer(
        serializedConnection: String,
        structuredMessage: String,
        answer: JSONObject
    ): CompletableFuture<Void?> {
        Logger.instance.i("Respond to structured message")
        val result =
            CompletableFuture<Void?>()
        try {
            ConnectionApi.connectionDeserialize(serializedConnection)
                .whenComplete { conHandle: Int?, err: Throwable? ->
                    if (err != null) {
                        Logger.instance
                            .e("Failed to deserialize connection: ", err)
                        result.completeExceptionally(err)
                        return@whenComplete
                    }
                    try {
                        println(answer.toString() + "CompletableFuture1")
                        ConnectionApi.connectionSendAnswer(
                            conHandle!!,
                            structuredMessage,
                            answer.toString()
                        ).whenComplete { res: Void?, er: Throwable? ->
                            if (er != null) {
                                Logger.instance
                                    .e("Failed to send answer: ", er)
                                result.completeExceptionally(er)
                            }
                            println(answer.toString() + "CompletableFuture2")
                            result.complete(null)
                        }
                    } catch (ex: Exception) {
                        result.completeExceptionally(ex)
                    }
                }
        } catch (ex: Exception) {
            result.completeExceptionally(ex)
        }
        return result
    }
}