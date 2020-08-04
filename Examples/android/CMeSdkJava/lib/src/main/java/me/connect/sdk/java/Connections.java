package me.connect.sdk.java;

import android.util.Log;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.connection.Connection;
import me.connect.sdk.java.message.MessageState;

/**
 * Class containing methods to work with connections
 */
public class Connections {

    public static final String TAG = "ConnectMeVcx";
    private static final String ARIES_CONNECTION_TYPE = "connections";
    private static final String ARIES_OUT_OF_BAND_TYPE = "out-of-band";

    public static @NonNull
    CompletableFuture<Boolean> verifyConnectionExists(@NonNull String invitationDetails,
                                                      @NonNull List<String> serializedConnections) {
        Logger.getInstance().i("Starting invite verification");
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        try {
            JSONObject json = new JSONObject(invitationDetails);
            // Proprietary protocol uses 'id' field, aries uses '@id' field
            if (json.has("id")) {
                String invitationId = json.getString("id");
                connectionVerifyProprietary(result, invitationId, invitationDetails, serializedConnections);
            } else {
                connectionVerifyAries(result, invitationDetails, serializedConnections);
            }
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Check proprietary connection already established and perform redirect if needed
     */
    private static void connectionVerifyProprietary(CompletableFuture<Boolean> result, String invitationId,
                                                    String invitationDetails, List<String> serializedConnections) {
        try {
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
                            String sameSerializedConn = null;
                            for (String serializedConn : serializedConnections) {
                                JSONObject connJson = new JSONObject(serializedConn);
                                String version = connJson.getString("version");
                                if (!version.equals("1.0")) {
                                    continue;
                                }
                                String theirPwDid = connJson.getJSONObject("data").getString("their_pw_did");
                                if (theirPwDid.equals(pwDid)) {
                                    sameSerializedConn = serializedConn;
                                    break;
                                }
                            }
                            if (sameSerializedConn == null) {
                                result.complete(false);
                            } else {
                                ConnectionApi.connectionDeserialize(sameSerializedConn).whenComplete((oldHandle, error) -> {
                                    if (error != null) {
                                        Logger.getInstance().e("Failed to deserialize stored connection: ", error);
                                        result.completeExceptionally(error);
                                    }
                                    try {
                                        ConnectionApi.vcxConnectionRedirect(handle, oldHandle).whenComplete((res, t) -> {
                                            if (t != null) {
                                                Logger.getInstance().e("Failed to redirect connection: ", t);
                                                result.completeExceptionally(t);
                                            } else {
                                                result.complete(true);
                                            }
                                        });
                                    } catch (Exception ex) {
                                        result.completeExceptionally(ex);
                                    }
                                });
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
    }

    /**
     * Check aries connection already established and perform reuse if needed
     */
    private static void connectionVerifyAries(CompletableFuture<Boolean> result, String invitationDetails,
                                              List<String> serializedConnections) {
        try {
            JSONObject iniviteJson = new JSONObject(invitationDetails);
            String recipientKey;
            String type = iniviteJson.getString("@type");
            if (type.contains(ARIES_CONNECTION_TYPE)) {
                recipientKey = iniviteJson.getJSONArray("recipientKeys").getString(0);
            } else {
                recipientKey = iniviteJson.getJSONArray("service").getJSONObject(0).getJSONArray("recipientKeys").getString(0);
            }

            String sameSerializedConn = null;
            for (String serializedConn : serializedConnections) {
                JSONObject connJson = new JSONObject(serializedConn);
                String version = connJson.getString("version");
                if (!version.equals("2.0")) {
                    continue;
                }
                JSONObject state = connJson.getJSONObject("state");
                JSONObject invitee = state.getJSONObject("Invitee");
                JSONObject completed = invitee.getJSONObject("Completed");
                List<String> recepientKeys = new ArrayList<>();
                if (completed.has("invitation")) {
                    JSONObject invitation = completed.getJSONObject("invitation").getJSONObject("OutofbandInvitation");
                    String invitationRecipientKey = invitation.getJSONArray("service").getJSONObject(0)
                            .getJSONArray("recipientKeys").getString(0);
                    recepientKeys.add(invitationRecipientKey);
                }
                JSONObject didDoc = completed.getJSONObject("did_doc");
                String didDocRecipientKey = didDoc.getJSONArray("service").getJSONObject(0)
                        .getJSONArray("recipientKeys").getString(0);
                recepientKeys.add(didDocRecipientKey);
                if (recepientKeys.contains(recipientKey)) {
                    sameSerializedConn = serializedConn;
                    break;
                }
            }
            if (type.contains(ARIES_CONNECTION_TYPE)) {
                // For Aries invite only existence should be checked
                result.complete(sameSerializedConn != null);
            } else if (type.contains(ARIES_OUT_OF_BAND_TYPE)) {
                // Current implementation assume that 'request~attach' array is not presented
                JSONArray handshakeProtocols = iniviteJson.optJSONArray("handshake_protocols");
                if (handshakeProtocols == null) {
                    result.completeExceptionally(new Exception("Invite does not have 'handshake_protocols' entry."));
                } else if (sameSerializedConn == null) {
                    // Connection does not exist, could create new connection
                    result.complete(false);
                } else {
                    // Connection already exists and should be reused
                    try {
                        ConnectionApi.connectionDeserialize(sameSerializedConn).whenComplete((handle, err) -> {
                            if (err != null) {
                                Logger.getInstance().e("Failed to deserialize stored connection: ", err);
                                result.completeExceptionally(err);
                            }
                            try {
                                ConnectionApi.connectionSendReuse(handle, invitationDetails).whenComplete((res, e) -> {
                                    if (e != null) {
                                        Logger.getInstance().e("Failed to reuse connection: ", e);
                                        result.completeExceptionally(e);
                                    } else {
                                        result.complete(true);
                                    }
                                });
                            } catch (VcxException ex) {
                                result.completeExceptionally(ex);
                            }
                        });
                    } catch (Exception ex) {
                        result.completeExceptionally(ex);
                    }
                }
            } else {
                // unsupported invitation type
                result.completeExceptionally(new Exception("Unsupported invite aries invite type: " + type));
            }
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
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
            String invitationType = null;
            if (json.has("id")) { //proprietary
                invitationId = json.getString("id");
            } else { // aries
                invitationId = json.getString("@id");
                invitationType = json.getString("@type");
            }

            CompletableFuture<Integer> creationStep;
            if (invitationType != null && invitationType.contains(ARIES_OUT_OF_BAND_TYPE)) {
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
                    String connType = connectionType.getConnectionType();
                    ConnectionApi.vcxConnectionConnect(handle, connType).whenComplete((invite, t) -> {
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