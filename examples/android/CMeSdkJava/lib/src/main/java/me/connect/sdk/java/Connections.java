package me.connect.sdk.java;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.StateMachineState;
import me.connect.sdk.java.message.MessageType;

/**
 * Class containing methods to work with connections
 */
public class Connections {
    public static String verifyConnectionExists(
            @NonNull String invitationDetails,
            @NonNull List<String> serializedConnections
    ) {
        try {
            Logger.getInstance().i("Starting invite verification");
            for (String sc : serializedConnections) {
                int handle = ConnectionApi.connectionDeserialize(sc).get();
                String storedInvite = ConnectionApi.connectionInviteDetails(handle, 0).get();
                ConnectionApi.connectionRelease(handle);
                if (ConnectionInvitations.compareInvites(invitationDetails, storedInvite)) {
                    return sc;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Redirect aries and out-of-band connections if needed
     */
    public static CompletableFuture<Boolean> connectionRedirectAriesOutOfBand(String invitation, String serializedConnection) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        try {
            JSONObject inviteJson = new JSONObject(invitation);
            // Current implementation assume that 'request~attach' array is not presented
            JSONArray handshakeProtocols = inviteJson.optJSONArray("handshake_protocols");

            if (serializedConnection == null) {
                // Connection does not exist, could create new connection
                result.complete(false);
                return result;
            }
            if (handshakeProtocols == null) {
                result.completeExceptionally(new Exception("Invite does not have 'handshake_protocols' entry."));
                return result;
            }

            String threadId = inviteJson.getString("@id");

            // Connection already exists and should be reused and wait handshake reuse accepted message
            try {
                ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((handle, err) -> {
                    if (err != null) {
                        Logger.getInstance().e("Failed to deserialize stored connection: ", err);
                        result.completeExceptionally(err);
                    }
                    try {
                        ConnectionApi.connectionSendReuse(handle, invitation).whenComplete((res, e) -> {
                            if (e != null) {
                                Logger.getInstance().e("Failed to reuse connection: ", e);
                                result.completeExceptionally(e);
                            } else {
                                while (true) {
                                    try {
                                        Message message = Messages.downloadMessage(MessageType.HANDSHAKE, threadId).get();
                                        System.out.println("Message Received " + message.getPayload());
                                        if (message != null) {
                                            String pwDid = Connections.getPwDid(serializedConnection);
                                            Messages.updateMessageStatus(pwDid, message.getUid());
                                            result.complete(true);
                                            return;
                                        }
                                        Thread.sleep(2000);
                                    } catch (ExecutionException | InterruptedException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        });
                    } catch (VcxException ex) {
                        result.completeExceptionally(ex);
                    }
                });
            } catch (Exception ex) {
                result.completeExceptionally(ex);
            }
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Creates new connection from invitation.
     *
     * @param invitationDetails String containing JSON with invitation details.
     * @param invitationType    Type of the invitation
     * @return {@link CompletableFuture} with serialized connection handle.
     */
    public static @NonNull
    CompletableFuture<String> create(@NonNull String invitationDetails,
                                     ConnectionInvitations.InvitationType invitationType) {
        Logger.getInstance().i("Starting connection creation");
        CompletableFuture<String> result = new CompletableFuture<>();

        try {
            JSONObject json = new JSONObject(invitationDetails);
            String invitationId = json.getString("@id");

            CompletableFuture<Integer> creationStep;
            if (ConnectionInvitations.isAriesOutOfBandConnectionInvitation(invitationType)) {
                creationStep = ConnectionApi.vcxCreateConnectionWithOutofbandInvite(invitationId, invitationDetails);
            } else {
                creationStep = ConnectionApi.vcxCreateConnectionWithInvite(invitationId, invitationDetails);
            }
            creationStep.whenComplete((handle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to create connection with invite: ", err);
                    result.completeExceptionally(err);
                }
                Logger.getInstance().i("Received handle: " + handle);
                try {
                    ConnectionApi.vcxConnectionConnect(handle, "{}").whenComplete((invite, t) -> {
                        if (t != null) {
                            Logger.getInstance().e("Failed to accept invitation: ", t);
                            result.completeExceptionally(t);
                            return;
                        }
                        Logger.getInstance().i("Received invite: " + invite);
                        try {
                            ConnectionApi.connectionSerialize(handle).whenComplete((serialized, e) -> {
                                if (e != null) {
                                    Logger.getInstance().e("Failed to serialize connection", e);
                                    result.completeExceptionally(e);
                                } else {
                                    result.complete(serialized);
                                }
                            });
                        } catch (VcxException ex) {
                            result.completeExceptionally(ex);
                        }
                    });
                } catch (VcxException ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    public static @NonNull
    String getPwDid(@NonNull String serializedConnection) {
        String pwDid = null;
        try {
            Integer handle = ConnectionApi.connectionDeserialize(serializedConnection).get();
            pwDid = ConnectionApi.connectionGetPwDid(handle).get();
        } catch (Exception e) {
            Logger.getInstance().e("Failed to get connection pwDID", e);
            e.printStackTrace();
        }
        return pwDid;
    }


    /**
     * Loops indefinitely until connection status is not changed
     *
     * @param serializedConnection string containing serialized connection
     * @return string containing serialized connection
     */
    public static @NonNull
    String awaitConnectionCompleted(@NonNull String serializedConnection) {
        Logger.getInstance().i("Awaiting connection state change");
        int count = 1;
        try {
            Integer handle = ConnectionApi.connectionDeserialize(serializedConnection).get();
            while (true) {
                Logger.getInstance().i("Awaiting connection state change: attempt #" + count);
                Integer state = ConnectionApi.vcxConnectionUpdateState(handle).get();
                Logger.getInstance().i("Awaiting connection state change: got state=" + state);
                if (StateMachineState.ACCEPTED.matches(state)) {
                    return ConnectionApi.connectionSerialize(handle).get();
                }
                count++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Logger.getInstance().e("Failed to await connection state", e);
            e.printStackTrace();
        }
        return serializedConnection;
    }

    /**
     * Loops indefinitely until connection status is not changed
     *
     * @param serializedConnection string containing serialized connection
     * @param pwDid string pwDid of connection
     * @return string containing serialized connection
     */
    public static String awaitConnectionCompleted(String serializedConnection, String pwDid) {
        Logger.getInstance().i("Awaiting connection state change");
        int status = -1;
        try {
            Integer handle = ConnectionApi.connectionDeserialize(serializedConnection).get();
            while (true) {
                try {
                    Message message = Messages.downloadMessage(MessageType.CONNECTION_RESPONSE, pwDid).get();
                        status = ConnectionApi.vcxConnectionUpdateStateWithMessage(handle, message.getPayload()).get();
                        Messages.updateMessageStatus(pwDid, message.getUid());
                        if (StateMachineState.ACCEPTED.matches(status)) {
                            return ConnectionApi.connectionSerialize(handle).get();
                        }
                    Thread.sleep(2000);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Logger.getInstance().e("Failed to await cred state", e);
            e.printStackTrace();
        }
        return serializedConnection;
    }
}