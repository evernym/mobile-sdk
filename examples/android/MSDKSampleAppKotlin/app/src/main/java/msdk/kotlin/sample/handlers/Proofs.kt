package msdk.kotlin.sample.handlers

import com.evernym.sdk.vcx.VcxException
import com.evernym.sdk.vcx.connection.ConnectionApi
import com.evernym.sdk.vcx.proof.DisclosedProofApi
import java9.util.concurrent.CompletableFuture
import msdk.kotlin.sample.logger.Logger
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Class containing methods to work with proofs.
 */
object Proofs {
    /**
     * Get proof requests
     * Deprecated. Use [Messages].getPendingMessages(String, MessageType) instead.
     *
     * @param connection serialized connection
     * @return [CompletableFuture] containing list of proof requests as JSON strings.
     */
    @Deprecated("")
    fun getRequests(connection: String): CompletableFuture<List<String>> {
        Logger.instance.i("Getting proof requests")
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
                        DisclosedProofApi.proofGetRequests(conHandle!!)
                            .whenComplete { offersJson: String?, er: Throwable? ->
                                if (er != null) {
                                    Logger.instance
                                        .e("Failed to get proof requests", er)
                                    result.completeExceptionally(er)
                                    return@whenComplete
                                }
                                Logger.instance
                                    .i("Received proof requests")
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
                            .e("Failed to get proof requests", ex)
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
     * Creates proof request
     *
     * @param sourceId custom string for this cred offer
     * @param message  proof request string
     * @return [CompletableFuture] containing serialized proof request
     */
    fun createWithRequest(
        sourceId: String,
        message: String
    ): CompletableFuture<String> {
        Logger.instance.i("Retrieving proof request")
        val result =
            CompletableFuture<String>()
        try {
            DisclosedProofApi.proofCreateWithRequest(sourceId, message)
                .whenComplete { proofHandle: Int?, err: Throwable? ->
                    if (err != null) {
                        Logger.instance
                            .e("Failed create proof with request: ", err)
                        result.completeExceptionally(err)
                        return@whenComplete
                    }
                    try {
                        DisclosedProofApi.proofSerialize(proofHandle!!)
                            .whenComplete { sp: String, e: Throwable? ->
                                if (e != null) {
                                    Logger.instance
                                        .e("Failed to serialize proof request: ", e)
                                    result.completeExceptionally(e)
                                } else {
                                    result.complete(sp)
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
     * Retrieves available credentials for proof request
     *
     * @param serializedProof proof request string
     * @return [CompletableFuture] containing string with available credentials
     */
    fun retrieveAvailableCredentials(serializedProof: String): CompletableFuture<String> {
        Logger.instance.i("Retrieving credentials for proof request")
        val result =
            CompletableFuture<String>()
        try {
            DisclosedProofApi.proofDeserialize(serializedProof)
                .whenComplete { proofHandle: Int?, err: Throwable? ->
                    if (err != null) {
                        Logger.instance
                            .e("Failed deserialize proof request: ", err)
                        result.completeExceptionally(err)
                        return@whenComplete
                    }
                    try {
                        DisclosedProofApi.proofRetrieveCredentials(proofHandle!!)
                            .whenComplete { retrievedCreds: String, e: Throwable? ->
                                if (e != null) {
                                    Logger.instance
                                        .e("Failed to retrieve proof credentials: ", e)
                                    result.completeExceptionally(e)
                                } else {
                                    if (checkProofCorrectness(retrievedCreds)) {
                                        result.complete(retrievedCreds)
                                    } else {
                                        result.completeExceptionally(Exception("Missed credential"))
                                    }
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

    private fun checkProofCorrectness(retrievedCreds: String): Boolean {
        try {
            val retrievedCredsObject = JSONObject(retrievedCreds).getJSONObject("attrs")
            val keys = retrievedCredsObject.keys()
            var result = true
            while (keys.hasNext()) {
                val key = keys.next()
                if (retrievedCredsObject.getJSONArray(key).length() == 0) {
                    result = false
                    break
                }
            }
            return result
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Respond to proof request with credentials
     *
     * @param serializedConnection   string containing serialized connection
     * @param serializedProof        string containing serialized proof request
     * @param selectedCreds          selected credentials to provide proof
     * @param selfAttestedAttributes user-defined attributes to provide proof
     * @return CompletableFuture containing serialized proof
     */
    fun send(
        serializedConnection: String,
        serializedProof: String,
        selectedCreds: String,
        selfAttestedAttributes: String
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
                        DisclosedProofApi.proofDeserialize(serializedProof)
                            .whenComplete { pHandle: Int?, er: Throwable? ->
                                if (er != null) {
                                    Logger.instance
                                        .e("Failed to deserialize proof: ", er)
                                    result.completeExceptionally(er)
                                    return@whenComplete
                                }
                                try {
                                    DisclosedProofApi.proofGenerate(
                                        pHandle!!,
                                        selectedCreds,
                                        selfAttestedAttributes
                                    ).whenComplete { v: Void?, e: Throwable? ->
                                        if (e != null) {
                                            Logger.instance
                                                .e("Failed to generate proof: ", e)
                                            result.completeExceptionally(e)
                                            return@whenComplete
                                        }
                                        try {
                                            DisclosedProofApi.proofSend(pHandle, conHandle!!)
                                                .whenComplete { r: Void?, error: Throwable? ->
                                                    if (error != null) {
                                                        Logger.instance
                                                            .e(
                                                                "Failed to send proof: ",
                                                                error
                                                            )
                                                        result.completeExceptionally(
                                                            error
                                                        )
                                                        return@whenComplete
                                                    }
                                                    try {
                                                        DisclosedProofApi.proofSerialize(
                                                            pHandle
                                                        )
                                                            .whenComplete { sp: String, th: Throwable? ->
                                                                if (th != null) {
                                                                    Logger.instance
                                                                        .e(
                                                                            "Failed to serialize proof: ",
                                                                            th
                                                                        )
                                                                    result.completeExceptionally(
                                                                        th
                                                                    )
                                                                } else {
                                                                    result.complete(
                                                                        sp
                                                                    )
                                                                }
                                                            }
                                                    } catch (ex: Exception) {
                                                        result.completeExceptionally(ex)
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
     * Reject proof request
     *
     * @param serializedConnection string containing serialized connection
     * @param serializedProof      string containing serialized proof request
     * @return CompletableFuture containing serialized proof
     */
    fun reject(
        serializedConnection: String,
        serializedProof: String
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
                        DisclosedProofApi.proofDeserialize(serializedProof)
                            .whenComplete { pHandle: Int?, e: Throwable? ->
                                if (e != null) {
                                    Logger.instance
                                        .e("Failed to deserialize proof: ", e)
                                    result.completeExceptionally(e)
                                    return@whenComplete
                                }
                                try {
                                    DisclosedProofApi.proofReject(pHandle!!, conHandle!!)
                                        .whenComplete { v: Void?, er: Throwable? ->
                                            if (er != null) {
                                                Logger.instance
                                                    .e("Failed to reject proof: ", er)
                                                result.completeExceptionally(er)
                                                return@whenComplete
                                            }
                                            try {
                                                DisclosedProofApi.proofSerialize(pHandle)
                                                    .whenComplete { sp: String, th: Throwable? ->
                                                        if (th != null) {
                                                            Logger.instance
                                                                .e(
                                                                    "Failed to serialize proof: ",
                                                                    th
                                                                )
                                                            result.completeExceptionally(th)
                                                        } else {
                                                            result.complete(sp)
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
     * Temporary method to extract credentials for proof request and prepare map that will be used later in proof request confirmation
     *
     * @param proofRequestCreds JSON string containing list of available credentials
     * @return JSON string containing prepared payload for proof request
     */
    fun mapCredentials(proofRequestCreds: String?): String {
        return try {
            val result = JSONObject()
            val resultAttrs = JSONObject()
            result.put("attrs", resultAttrs)
            val data = JSONObject(proofRequestCreds)
            val attrs = data.getJSONObject("attrs")
            val it = attrs.keys()
            while (it.hasNext()) {
                val key = it.next()
                val credArray = attrs.getJSONArray(key)
                val cred = credArray.getJSONObject(0)
                val credHolder = JSONObject()
                credHolder.put("credential", cred)
                resultAttrs.put(key, credHolder)
            }
            result.toString()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}