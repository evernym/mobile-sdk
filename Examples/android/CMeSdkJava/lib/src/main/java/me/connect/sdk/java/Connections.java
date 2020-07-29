package me.connect.sdk.java;

import android.util.Log;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;

import org.json.JSONObject;

import java.util.List;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.connection.Connection;
import me.connect.sdk.java.message.MessageState;

/**
 * Class containing methods to work with connections
 */
public class Connections {

    public static final String TAG = "ConnectMeVcx";

    public static @NonNull
    CompletableFuture<String> verifyConnectionExists(@NonNull String invitationDetails,
                                                     @NonNull List<String> serializedConnections) {
        Logger.getInstance().i("Starting invite verification");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            JSONObject json = new JSONObject(invitationDetails);
            String invitationId;
            if (json.has("id")) {
                invitationId = json.getString("id");
            } else {
                invitationId = json.getString("@id");
            }
            ConnectionApi.vcxCreateConnectionWithInvite(invitationId, invitationDetails).whenComplete((handle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to create connection with invite: ", err);
                    result.completeExceptionally(err);
                }
                try {
                    ConnectionApi.connectionGetTheirPwDid(handle).whenComplete((pwDid, e) -> {
                        if (e != null) {
                            Logger.getInstance().e("Failed to obtain pwDid for connection: ", e);
                            result.completeExceptionally(e);
                        }
                        try {
                            for (String serializedConn : serializedConnections) {
                                JSONObject connJson = new JSONObject(serializedConn);
                                // TODO!!
                                result.complete(pwDid);
                            }
                        } catch (Exception ex) {
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

    /**
     * Creates new connection from invitation.
     *
     * @param invitationDetails String containing JSON with invitation details.
     * @param connectionType    {@link Connection} object containing information about connection type.
     * @return {@link CompletableFuture} with serialized connection handle.
     */
    public static @NonNull
    CompletableFuture<String> create(@NonNull String invitationDetails,
                                     @NonNull Connection connectionType) {
        Logger.getInstance().i("Starting connection creation");
        CompletableFuture<String> result = new CompletableFuture<>();

        try {
            JSONObject json = new JSONObject(invitationDetails);
            String invitationId;
            if (json.has("id")) {
                invitationId = json.getString("id");
            } else {
                invitationId = json.getString("@id");
            }

            ConnectionApi.vcxCreateConnectionWithInvite(invitationId, invitationDetails).whenComplete((handle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to create connection with invite: ", err);
                    result.completeExceptionally(err);
                }
                Logger.getInstance().i("Received handle: " + handle);
                try {
                    String connType = connectionType.getConnectionType();
                    ConnectionApi.vcxAcceptInvitation(handle, connType).whenComplete((invite, t) -> {
                        if (t != null) {
                            Logger.getInstance().e("Failed to accept invitation: ", t);
                            result.completeExceptionally(t);
                            return;
                        }
                        Logger.getInstance().i("Received invite: " + invite);
                        try {
                            ConnectionApi.connectionSerialize(handle).whenComplete((serializedConn, e) -> {
                                if (e != null) {
                                    Logger.getInstance().e("Failed to serialize connection", e);
                                    result.completeExceptionally(e);
                                } else {
                                    result.complete(serializedConn);
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

    /**
     * Loops indefinitely until connection status is not changed
     *
     * @param serializedConnection string containing serialized connection
     * @return string containing serialized connection
     */
    @ExperimentalWalletBackup
    public static @NonNull
    String awaitStatusChange(@NonNull String serializedConnection, MessageState messageState) {
        Logger.getInstance().i("Awaiting connection state change");
        int count = 1;
        try {
            Integer handle = ConnectionApi.connectionDeserialize(serializedConnection).get();
            while (true) {
                Logger.getInstance().i("Awaiting connection state change: attempt #" + count);
                Integer state = ConnectionApi.vcxConnectionUpdateState(handle).get();
                Logger.getInstance().i("Awaiting connection state change: got state=" + state);
                if (messageState.matches(state)) {
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
}