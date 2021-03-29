package me.connect.sdk.java;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.connection.Connection;
import me.connect.sdk.java.message.AriesMessageType;
import me.connect.sdk.java.message.MessageState;

/**
 * Class containing methods to work with connections
 */
public class Connections {

    public static final String TAG = "ConnectMeVcx";


    public static @NonNull
    CompletableFuture<Boolean> verifyConnectionExists(@NonNull String invitationDetails,
                                                      @NonNull List<String> serializedConnections) {
        Logger.getInstance().i("Starting invite verification");
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        try {
            String existingConnection = findExistingConnection(invitationDetails, serializedConnections);
            if (isAriesInvitation(invitationDetails)) {
                connectionRedirectAries(result, invitationDetails, existingConnection);
            } else {
                connectionRedirectProprietary(result, invitationDetails, existingConnection);
            }

        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }


    private static boolean isAriesInvitation(String invite) throws Exception {
        JSONObject json = new JSONObject(invite);
        boolean hasId = json.has("@id");
        boolean typeMatches = false;
        if (json.has("@type")) {
            String type = json.getString("@type");
            typeMatches = type.contains(AriesMessageType.CONNECTION_INVITATION)
                    || type.contains(AriesMessageType.OUTOFBAND_INVITATION);
        }
        return hasId && typeMatches;
    }

    private static String findExistingConnection(String newInvite, List<String> serializedConnections) throws Exception {
        String existingConnection = null;
        for (String sc : serializedConnections) {
            int handle = ConnectionApi.connectionDeserialize(sc).get();
            String storedInvite = ConnectionApi.connectionInviteDetails(handle, 0).get();
            if (compareInvites(newInvite, storedInvite)) {
                existingConnection = sc;
            }
            ConnectionApi.connectionRelease(handle);
            if (existingConnection != null) {
                break;
            }
        }
        return existingConnection;
    }

    private static boolean compareInvites(String newInvite, String storedInvite) throws Exception {
        boolean newInviteIsAries = isAriesInvitation(newInvite);
        boolean storedInviteIsAries = isAriesInvitation(storedInvite);
        JSONObject newJson = new JSONObject(newInvite);
        JSONObject storedJson = new JSONObject(storedInvite);

        if (newInviteIsAries && storedInviteIsAries) {
            String newPublicDid = newJson.optString("public_did");
            String storedPublicDid = storedJson.optString("public_did");
            if (!storedPublicDid.isEmpty()) {
                return storedPublicDid.equals(newPublicDid);
            } else {
                String newDid = newJson.getJSONArray("recipientKeys").getString(0);
                String storedDid = storedJson.getJSONArray("recipientKeys").optString(0);
                return storedDid.equals(newDid);
            }
        }

        if (!newInviteIsAries && !storedInviteIsAries) {
            if (newJson.has("senderDetail")) {
                String newPublicDid = newJson.getJSONObject("senderDetail").optString("publicDID");
                String storedPublicDid = storedJson.getJSONObject("senderDetail").optString("publicDID");
                if (!storedPublicDid.isEmpty()) {
                    return storedPublicDid.equals(newPublicDid);
                } else {
                    String storedDid = storedJson.getJSONObject("senderDetail").getString("DID");
                    String newDid = newJson.getJSONObject("senderDetail").getString("DID");
                    return storedDid.equals(newDid);
                }
            } else { // use abbreviated
                String newPublicDid = newJson.getJSONObject("s").optString("publicDID");
                String storedPublicDid = storedJson.getJSONObject("senderDetail").optString("publicDID");
                if (!storedPublicDid.isEmpty()) {
                    return storedPublicDid.equals(newPublicDid);
                } else {
                    String storedDid = storedJson.getJSONObject("senderDetail").getString("DID");
                    String newDid = newJson.getJSONObject("s").getString("d");
                    return storedDid.equals(newDid);
                }
            }
        }
        return false;
    }


    /**
     * Redirect proprietary connection if needed
     */
    private static void connectionRedirectProprietary(CompletableFuture<Boolean> result, String invitationDetails,
                                                      String serializedConnection) {
        if (serializedConnection == null) {
            result.complete(false);
            return;
        }
        try {
            JSONObject inviteJson = new JSONObject(invitationDetails);
            String invitationId = inviteJson.getString("id");
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
                            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((oldHandle, error) -> {
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
     * Redirect aries and out-of-band connections if needed
     */
    private static void connectionRedirectAries(CompletableFuture<Boolean> result, String invitationDetails,
                                                String serializedConnection) {
        try {
            JSONObject iniviteJson = new JSONObject(invitationDetails);
            String type = iniviteJson.getString("@type");
            if (type.contains(AriesMessageType.CONNECTION_INVITATION)) {
                // For Aries invite only existence should be checked
                result.complete(serializedConnection != null);
            } else if (type.contains(AriesMessageType.OUTOFBAND_INVITATION)) {
                // Current implementation assume that 'request~attach' array is not presented
                JSONArray handshakeProtocols = iniviteJson.optJSONArray("handshake_protocols");
                if (handshakeProtocols == null) {
                    result.completeExceptionally(new Exception("Invite does not have 'handshake_protocols' entry."));
                } else if (serializedConnection == null) {
                    // Connection does not exist, could create new connection
                    result.complete(false);
                } else {
                    // Connection already exists and should be reused
                    try {
                        ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((handle, err) -> {
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
            if (invitationType != null && invitationType.contains(AriesMessageType.OUTOFBAND_INVITATION)) {
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