package msdk.kotlin.sample.handlers

import com.evernym.sdk.vcx.VcxException
import com.evernym.sdk.vcx.connection.ConnectionApi
import com.evernym.sdk.vcx.credential.CredentialApi
import com.evernym.sdk.vcx.utils.UtilsApi
import java9.util.concurrent.CompletableFuture
import msdk.kotlin.sample.logger.Logger
import msdk.kotlin.sample.types.MessageType
import msdk.kotlin.sample.types.StateMachineState
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Class containig methods to work with credentials
 */
object Credentials {
    /**
     * Get credential offers
     * Deprecated. Use [Messages] Messages.getPendingMessages(String, MessageType) instead.
     *
     * @param connection serialized connection
     * @return [CompletableFuture] containing list of credential offers as JSON strings.
     */
    fun getOffers(connection: String): CompletableFuture<List<String>> {
        Logger.instance.i("Getting credential offers")
        val result =
            CompletableFuture<List<String>>()
        try {
            ConnectionApi.connectionDeserialize(connection)
                .whenComplete { conHandle: Int?, err: Throwable? ->
                    if (err != null) {
                        Logger.instance
                            .e("Failed to deserialize connection", err)
                        result.completeExceptionally(err)
                        return@whenComplete
                    }
                    try {
                        CredentialApi.credentialGetOffers(conHandle!!)
                            .whenComplete { offersJson: String?, e: Throwable? ->
                                if (e != null) {
                                    Logger.instance
                                        .e("Failed to get credential offers", e)
                                    result.completeExceptionally(e)
                                    return@whenComplete
                                }
                                Logger.instance
                                    .i("Received credential offers")
                                try {
                                    val offerArray = JSONArray(offersJson)
                                    val offers: MutableList<String> =
                                        ArrayList()
                                    for (i in 0 until offerArray.length()) {
                                        offers.add(offerArray.getString(i))
                                    }
                                    result.complete(offers)
                                } catch (ex: JSONException) {
                                    result.completeExceptionally(ex)
                                }
                            }
                    } catch (ex: VcxException) {
                        Logger.instance
                            .e("Failed to get credential offers", ex)
                        result.completeExceptionally(ex)
                    }
                }
        } catch (ex: Exception) {
            Logger.instance.e("Failed to deserialize connection", ex)
            result.completeExceptionally(ex)
        }
        return result
    }

    /**
     * Create credential offer
     *
     * @param sourceId             custom string for this cred offer
     * @param message              credential offer string
     * @return serialized credential offer
     */
    fun createWithOffer(
        sourceId: String,
        message: String
    ): CompletableFuture<String> {
        Logger.instance.i("Accepting credential offer")
        val result =
            CompletableFuture<String>()
        try {
            CredentialApi.credentialCreateWithOffer(sourceId, message)
                .whenComplete { credHandle: Int?, er: Throwable? ->
                    if (er != null) {
                        Logger.instance
                            .e("Failed to create credential with offer: ", er)
                        result.completeExceptionally(er)
                        return@whenComplete
                    }
                    try {
                        CredentialApi.credentialSerialize(credHandle!!)
                            .whenComplete { sc: String, e: Throwable? ->
                                if (e != null) {
                                    Logger.instance
                                        .e("Failed to serialize credentials: ", e)
                                    result.completeExceptionally(e)
                                } else {
                                    result.complete(sc)
                                }
                            }
                    } catch (ex: VcxException) {
                        result.completeExceptionally(ex)
                    }
                }
        } catch (ex: Exception) {
            result.completeExceptionally(ex)
        }
        return result
    }

    /**
     * Accept credential offer
     *
     * @param serializedConnection serialized connection string
     * @param serializedCredOffer  serialized credential offer
     * @return serialized credential offer
     */
    fun acceptOffer(
        serializedConnection: String,
        serializedCredOffer: String
    ): CompletableFuture<String> {
        Logger.instance.i("Accepting credential offer")
        val result =
            CompletableFuture<String>()
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
                        CredentialApi.credentialDeserialize(serializedCredOffer)
                            .whenComplete { credHandle: Int?, er: Throwable? ->
                                if (er != null) {
                                    Logger.instance
                                        .e("Failed to deserialize credential offer: ", er)
                                    result.completeExceptionally(er)
                                    return@whenComplete
                                }
                                try {
                                    CredentialApi.credentialSendRequest(
                                        credHandle!!,
                                        conHandle!!,
                                        0
                                    )
                                        .whenComplete { v: Void?, e: Throwable? ->
                                            if (e != null) {
                                                Logger.instance
                                                    .e("Failed to send credential request: ", e)
                                                result.completeExceptionally(e)
                                                return@whenComplete
                                            }
                                            try {
                                                CredentialApi.credentialSerialize(credHandle)
                                                    .whenComplete { sc: String, th: Throwable? ->
                                                        if (th != null) {
                                                            Logger.instance
                                                                .e(
                                                                    "Failed to serialize credentials: ",
                                                                    th
                                                                )
                                                            result.completeExceptionally(th)
                                                        } else {
                                                            result.complete(sc)
                                                        }
                                                    }
                                            } catch (ex: VcxException) {
                                                Logger.instance
                                                    .e("Failed to serialize credentials: ", ex)
                                                result.completeExceptionally(ex)
                                            }
                                        }
                                } catch (ex: VcxException) {
                                    Logger.instance
                                        .e("Failed to send credential request: ", ex)
                                    result.completeExceptionally(ex)
                                }
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

    /**
     * Reject credential offer
     *
     * @param serializedConnection serialized connection string
     * @param serializedCredOffer  serialized credential offer
     * @return serialized credential offer
     */
    fun rejectOffer(
        serializedConnection: String,
        serializedCredOffer: String
    ): CompletableFuture<String> {
        Logger.instance.i("Sending proof request response")
        val result =
            CompletableFuture<String>()
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
                        CredentialApi.credentialDeserialize(serializedCredOffer)
                            .whenComplete { credHandle: Int?, er: Throwable? ->
                                if (er != null) {
                                    Logger.instance
                                        .e("Failed to deserialize credential offer: ", er)
                                    result.completeExceptionally(er)
                                    return@whenComplete
                                }
                                try {
                                    CredentialApi.credentialReject(credHandle!!, conHandle!!, "")
                                        .whenComplete { v: Void?, e: Throwable? ->
                                            if (e != null) {
                                                Logger.instance
                                                    .e("Failed to reject proof: ", e)
                                                result.completeExceptionally(e)
                                                return@whenComplete
                                            }
                                            try {
                                                CredentialApi.credentialSerialize(credHandle)
                                                    .whenComplete { sc: String, th: Throwable? ->
                                                        if (th != null) {
                                                            Logger.instance
                                                                .e(
                                                                    "Failed to serialize credentials: ",
                                                                    th
                                                                )
                                                            result.completeExceptionally(th)
                                                        } else {
                                                            result.complete(sc)
                                                        }
                                                    }
                                            } catch (ex: VcxException) {
                                                result.completeExceptionally(ex)
                                            }
                                        }
                                } catch (ex: VcxException) {
                                    result.completeExceptionally(ex)
                                }
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

    /**
     * Loops indefinitely until credential request status is not changed
     *
     * @param serializedCredential string containing serialized credential request
     * @return string containing serialized credential
     */
    fun awaitCredentialReceived(serializedCredential: String): String {
        Logger.instance.i("Awaiting cred state change")
        var count = 1
        try {
            val handle = CredentialApi.credentialDeserialize(serializedCredential).get()
            while (true) {
                Logger.instance
                    .i("Awaiting cred state change: attempt #$count")
                val state0 = CredentialApi.credentialUpdateState(handle).get()
                Logger.instance
                    .i("Awaiting cred state change: update state=$state0")
                val state = CredentialApi.credentialGetState(handle).get()
                Logger.instance
                    .i("Awaiting cred state change: got state=$state")
                if (StateMachineState.ACCEPTED.matches(state)) {
                    UtilsApi.vcxFetchPublicEntities()
                    return CredentialApi.credentialSerialize(handle).get()
                }
                count++
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            Logger.instance.e("Failed to await cred state", e)
            e.printStackTrace()
        }
        return serializedCredential
    }

    /**
     * Loops indefinitely until credential request status is not changed
     *
     * @param serializedCredential string containing serialized credential request
     * @param threadId string claimId of credential request
     * @return string containing serialized credential
     */
    fun awaitCredentialReceived(
        serializedCredential: String,
        threadId: String,
        pwDid: String?
    ): String {
        Logger.instance.i("Awaiting cred state change")
        var status = -1
        try {
            val handle = CredentialApi.credentialDeserialize(serializedCredential).get()
            while (true) {
                try {
                    val message =
                        Messages.downloadNextMessageFromTheThread(
                            MessageType.CREDENTIAL,
                            threadId
                        ).get()
                    if (message != null) {
                        status = CredentialApi.credentialUpdateStateWithMessage(
                            handle,
                            message.payload
                        ).get()
                        Messages.updateMessageStatus(
                            pwDid,
                            message.uid
                        )
                        if (StateMachineState.ACCEPTED.matches(status)) {
                            UtilsApi.vcxFetchPublicEntities()
                            return CredentialApi.credentialSerialize(handle).get()
                        }
                    }
                    Thread.sleep(1000)
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Logger.instance.e("Failed to await cred state", e)
            e.printStackTrace()
        }
        return serializedCredential
    }
}