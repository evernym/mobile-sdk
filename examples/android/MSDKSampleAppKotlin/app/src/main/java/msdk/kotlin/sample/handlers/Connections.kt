package msdk.kotlin.sample.handlers

import com.evernym.sdk.vcx.VcxException
import com.evernym.sdk.vcx.connection.ConnectionApi
import java9.util.concurrent.CompletableFuture
import msdk.kotlin.sample.logger.Logger
import msdk.kotlin.sample.messages.ConnectionInvitation
import msdk.kotlin.sample.messages.ConnectionInvitation.InvitationType
import msdk.kotlin.sample.types.MessageType
import msdk.kotlin.sample.types.StateMachineState
import org.json.JSONObject
import java.util.concurrent.ExecutionException

/**
 * Class containing methods to work with connections
 */
object Connections {
    fun verifyConnectionExists(
        invitationDetails: String,
        serializedConnections: List<String?>
    ): String? {
        try {
            Logger.instance.i("Starting invite verification")
            for (sc in serializedConnections) {
                val handle = ConnectionApi.connectionDeserialize(sc).get()
                val storedInvite =
                    ConnectionApi.connectionInviteDetails(handle, 0).get()
                ConnectionApi.connectionRelease(handle)
                if (ConnectionInvitation.compareInvites(invitationDetails, storedInvite)) {
                    return sc
                }
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Redirect aries and out-of-band connections if needed
     */
    fun connectionRedirectAriesOutOfBand(
        invitation: String?,
        serializedConnection: String?
    ): CompletableFuture<Boolean> {
        val result =
            CompletableFuture<Boolean>()
        try {
            val inviteJson = JSONObject(invitation)
            // Current implementation assume that 'request~attach' array is not presented
            val handshakeProtocols = inviteJson.optJSONArray("handshake_protocols")
            if (serializedConnection == null) {
                // Connection does not exist, could create new connection
                result.complete(false)
                return result
            }
            if (handshakeProtocols == null) {
                result.completeExceptionally(Exception("Invite does not have 'handshake_protocols' entry."))
                return result
            }
            val threadId = inviteJson.getString("@id")

            // Connection already exists and should be reused and wait handshake reuse accepted message
            try {
                ConnectionApi.connectionDeserialize(serializedConnection)
                    .whenComplete { handle: Int?, err: Throwable? ->
                        if (err != null) {
                            Logger.instance
                                .e("Failed to deserialize stored connection: ", err)
                            result.completeExceptionally(err)
                        }
                        try {
                            ConnectionApi.connectionSendReuse(handle!!, invitation)
                                .whenComplete { res: Void?, e: Throwable? ->
                                    if (e != null) {
                                        Logger.instance
                                            .e("Failed to reuse connection: ", e)
                                        result.completeExceptionally(e)
                                    } else {
                                        while (true) {
                                            try {
                                                val message =
                                                    Messages.downloadNextMessageFromTheThread(
                                                        MessageType.HANDSHAKE,
                                                        threadId
                                                    ).get()
                                                println("Message Received " + message!!.payload)
                                                if (message != null) {
                                                    val pwDid =
                                                        getPwDid(
                                                            serializedConnection
                                                        )
                                                    Messages.updateMessageStatus(
                                                        pwDid,
                                                        message.uid
                                                    )
                                                    result.complete(true)
                                                    return@whenComplete
                                                }
                                                Thread.sleep(2000)
                                            } catch (ex: ExecutionException) {
                                                ex.printStackTrace()
                                            } catch (ex: InterruptedException) {
                                                ex.printStackTrace()
                                            }
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
        } catch (ex: Exception) {
            result.completeExceptionally(ex)
        }
        return result
    }

    /**
     * Creates new connection from invitation.
     *
     * @param invitationDetails String containing JSON with invitation details.
     * @param invitationType    Type of the invitation
     * @return [CompletableFuture] with serialized connection handle.
     */
    fun create(
        invitationDetails: String,
        invitationType: InvitationType
    ): CompletableFuture<String> {
        Logger.instance.i("Starting connection creation")
        val result =
            CompletableFuture<String>()
        try {
            val json = JSONObject(invitationDetails)
            val invitationId = json.getString("@id")
            val creationStep: CompletableFuture<Int>
            creationStep =
                if (ConnectionInvitation.isAriesOutOfBandConnectionInvitation(invitationType)) {
                    ConnectionApi.vcxCreateConnectionWithOutofbandInvite(
                        invitationId,
                        invitationDetails
                    )
                } else {
                    ConnectionApi.vcxCreateConnectionWithInvite(invitationId, invitationDetails)
                }
            creationStep.whenComplete { handle: Int, err: Throwable? ->
                if (err != null) {
                    Logger.instance
                        .e("Failed to create connection with invite: ", err)
                    result.completeExceptionally(err)
                }
                Logger.instance.i("Received handle: $handle")
                try {
                    ConnectionApi.vcxConnectionConnect(handle, "{}")
                        .whenComplete { invite: String, t: Throwable? ->
                            if (t != null) {
                                Logger.instance
                                    .e("Failed to accept invitation: ", t)
                                result.completeExceptionally(t)
                                return@whenComplete
                            }
                            Logger.instance
                                .i("Received invite: $invite")
                            try {
                                ConnectionApi.connectionSerialize(handle)
                                    .whenComplete { serialized: String, e: Throwable? ->
                                        if (e != null) {
                                            Logger.instance
                                                .e("Failed to serialize connection", e)
                                            result.completeExceptionally(e)
                                        } else {
                                            result.complete(serialized)
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
        return result
    }

    fun getPwDid(serializedConnection: String): String {
        var pwDid: String? = null
        try {
            val handle = ConnectionApi.connectionDeserialize(serializedConnection).get()
            pwDid = ConnectionApi.connectionGetPwDid(handle).get()
        } catch (e: Exception) {
            Logger.instance.e("Failed to get connection pwDID", e)
            e.printStackTrace()
        }
        return pwDid!!
    }

    /**
     * Loops indefinitely until connection status is not changed
     *
     * @param serializedConnection string containing serialized connection
     * @return string containing serialized connection
     */
    fun awaitConnectionCompleted(serializedConnection: String): String {
        Logger.instance.i("Awaiting connection state change")
        var count = 1
        try {
            val handle = ConnectionApi.connectionDeserialize(serializedConnection).get()
            while (true) {
                Logger.instance
                    .i("Awaiting connection state change: attempt #$count")
                val state = ConnectionApi.vcxConnectionUpdateState(handle).get()
                Logger.instance
                    .i("Awaiting connection state change: got state=$state")
                if (StateMachineState.ACCEPTED.matches(state)) {
                    return ConnectionApi.connectionSerialize(handle).get()
                }
                count++
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            Logger.instance.e("Failed to await connection state", e)
            e.printStackTrace()
        }
        return serializedConnection
    }

    /**
     * Loops indefinitely until connection status is not changed
     *
     * @param serializedConnection string containing serialized connection
     * @param pwDid string pwDid of connection
     * @return string containing serialized connection
     */
    fun awaitConnectionCompleted(
        serializedConnection: String,
        pwDid: String
    ): String {
        Logger.instance.i("Awaiting connection state change")
        var status = -1
        try {
            val handle = ConnectionApi.connectionDeserialize(serializedConnection).get()
            while (true) {
                try {
                    val message =
                        Messages.downloadNextMessageFromTheThread(
                            MessageType.CONNECTION_RESPONSE,
                            pwDid
                        ).get()

                    if (message != null) {
                        status = ConnectionApi.vcxConnectionUpdateStateWithMessage(
                            handle,
                            message.payload
                        ).get()
                        Messages.updateMessageStatus(
                            pwDid,
                            message.uid
                        )
                        if (StateMachineState.ACCEPTED.matches(status)) {
                            return ConnectionApi.connectionSerialize(handle).get()
                        }
                    }
                    Thread.sleep(2000)
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
        return serializedConnection
    }

    class ConnectionMetadata(var name: String, var logo: String)
}