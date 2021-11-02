package msdk.kotlin.sample.handlers

import com.evernym.sdk.vcx.VcxException
import com.evernym.sdk.vcx.utils.UtilsApi
import java9.util.concurrent.CompletableFuture
import msdk.kotlin.sample.logger.Logger
import msdk.kotlin.sample.messages.Message
import msdk.kotlin.sample.types.MessageStatusType
import msdk.kotlin.sample.types.MessageType
import msdk.kotlin.sample.types.UpdateMessageStatusBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Class containing methods to work with messages.
 */
object Messages {
    /**
     * Retrieve all pending messages.
     *
     * @return List of [Message]
     */
    val allPendingMessages: CompletableFuture<List<Message>>
        get() {
            Logger.instance.i("Retrieving pending messages")
            val result =
                CompletableFuture<List<Message>>()
            try {
                UtilsApi.vcxGetMessages(MessageStatusType.PENDING, null, null)
                    .whenComplete { messagesString: String?, err: Throwable? ->
                        if (err != null) {
                            Logger.instance
                                .e("Failed to retrieve messages: ", err)
                            result.completeExceptionally(err)
                            return@whenComplete
                        }
                        try {
                            val messages: MutableList<Message> =
                                ArrayList()
                            val messagesJson = JSONArray(messagesString)
                            println(messagesJson.toString() + "getAllPendingMessages")
                            for (i in 0 until messagesJson.length()) {
                                val msgsJson =
                                    messagesJson.getJSONObject(i).optJSONArray("msgs")
                                val pairwiseDID =
                                    messagesJson.getJSONObject(i).getString("pairwiseDID")
                                if (msgsJson != null) {
                                    for (j in 0 until msgsJson.length()) {
                                        val message = msgsJson.getJSONObject(j)
                                        val payload =
                                            JSONObject(message.getString("decryptedPayload"))
                                        val type =
                                            payload.getJSONObject("@type").getString("name")
                                        val messageUid = message.getString("uid")
                                        val msg = payload.getString("@msg")
                                        val status = message.getString("statusCode")
                                        messages.add(
                                            Message(
                                                pairwiseDID,
                                                messageUid,
                                                msg,
                                                type,
                                                status
                                            )
                                        )
                                    }
                                }
                            }
                            result.complete(messages)
                        } catch (ex: JSONException) {
                            result.completeExceptionally(ex)
                        }
                    }
            } catch (ex: VcxException) {
                ex.printStackTrace()
            }
            return result
        }

    /*
    * Download and find message from the specific thread
    * */
    fun downloadNextMessageFromTheThread(
        messageType: MessageType,
        id: String
    ): CompletableFuture<Message?> {
        val result =
            CompletableFuture<Message?>()
        allPendingMessages
            .handle<Any?> { messages: List<Message>, throwable: Throwable? ->
                if (throwable != null) {
                    throwable.printStackTrace()
                    result.completeExceptionally(throwable)
                }
                var foundMessage: Message? = null
                for (message in messages) {
                    try {
                        val msg: String = message.payload
                        val pwDid: String = message.pwDid
                        val msgPayload = JSONObject(msg)
                        val type = msgPayload.getString("@type")
                        val thread = msgPayload.getJSONObject("~thread")
                        val thid = thread.getString("thid")

                        if (messageType == MessageType.CREDENTIAL && messageType.matchesValue(type) && thid == id) {
                            foundMessage = message
                            break
                        }
                        if (messageType == MessageType.CONNECTION_RESPONSE && messageType.matchesValue(type) && pwDid == id) {
                            foundMessage = message
                            break
                        }
                        if (messageType == MessageType.ACK && messageType.matchesValue(type)) {
                            foundMessage = message
                            break
                        }
                        if (messageType == MessageType.HANDSHAKE && messageType.matchesValue(type) && thid == id) {
                            foundMessage = message
                            break
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                result.complete(foundMessage)
                null
            }
        return result
    }

    /*
    * Update status of the message as reviewed
    * */
    fun updateMessageStatus(pwDid: String?, messageId: String?) {
        val result =
            CompletableFuture<String?>()
        try {
            val messagesToUpdate: String = UpdateMessageStatusBody()
                .addMessage(pwDid!!, messageId!!)
                .toJSON()
            UtilsApi.vcxUpdateMessages(MessageStatusType.REVIEWED, messagesToUpdate)
                .whenComplete { v1: Void?, error: Throwable? ->
                    if (error != null) {
                        Logger.instance.e("Failed to update messages", error)
                        result.completeExceptionally(error)
                    } else {
                        result.complete(null)
                    }
                }
        } catch (ex: Exception) {
            result.completeExceptionally(ex)
        }
    }
}