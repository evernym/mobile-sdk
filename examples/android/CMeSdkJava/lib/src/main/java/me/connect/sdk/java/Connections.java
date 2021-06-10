package me.connect.sdk.java;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.connection.Connection;
import me.connect.sdk.java.message.AriesMessageType;
import me.connect.sdk.java.message.MessageState;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Class containing methods to work with connections
 */
public class Connections {

    public static final String TAG = "ConnectMeVcx";

    public enum InvitationType {
        Proprietary,
        Connection,
        OutOfBand
    }

//    public static void connectionRedirectByType(
//            @NonNull String invite,
//            @NonNull String existingConnection
//    ) {
//        if (isAriesInvitation(invite)) {
//            connectionRedirectAries(invite, existingConnection);
//        } else {
//            connectionRedirectProprietary(invite, existingConnection);
//        }
//    }

    public static String verifyConnectionExists(
            @NonNull String invitationDetails,
            @NonNull List<String> serializedConnections
    ) {
        try {
            Logger.getInstance().i("Starting invite verification");
            return findExistingConnection(invitationDetails, serializedConnections);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InvitationType getInvitationType(String invite) {
        try {
            JSONObject json = new JSONObject(invite);
            boolean hasId = json.has("@id");
            if (json.has("@type") && hasId) {
                String invitationType = json.getString("@type");
                if (invitationType.contains(AriesMessageType.OUTOFBAND_INVITATION)) {
                    return InvitationType.OutOfBand;
                }
                if (invitationType.contains(AriesMessageType.CONNECTION_INVITATION)) {
                    return InvitationType.Connection;
                } else {
                    return InvitationType.Proprietary;
                }
            } else {
                throw new Exception("Invalid invite format");
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isAriesInvitation(String invite) {
        InvitationType type = getInvitationType(invite);
        return type == InvitationType.Connection || type == InvitationType.OutOfBand;
    }

    public static String findExistingConnection(String newInvite, List<String> serializedConnections) throws Exception {
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
    public static void connectionRedirectProprietary(String invitationDetails, String serializedConnection) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
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
                } catch (VcxException ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
    }

    /**
     * Redirect aries connections if needed
     */
    public static void connectionRedirectAries(String invitationDetails, String serializedConnection) {
        // there is nothing to do here
    }

    /**
     * Redirect aries and out-of-band connections if needed
     */
    public static void connectionRedirectAriesOutOfBand(String invitationDetails, String serializedConnection) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        try {
            JSONObject inviteJson = new JSONObject(invitationDetails);
            // Current implementation assume that 'request~attach' array is not presented
            JSONArray handshakeProtocols = inviteJson.optJSONArray("handshake_protocols");

            if (serializedConnection == null) {
                // Connection does not exist, could create new connection
                result.complete(false);
                return;
            }
            if (handshakeProtocols == null) {
                result.completeExceptionally(new Exception("Invite does not have 'handshake_protocols' entry."));
                return;
            }

            // Connection already exists and should be reused
            try {
                ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((handle, err) -> {
                    if (err != null) {
                        Logger.getInstance().e("Failed to deserialize stored connection: ", err);
                        result.completeExceptionally(err);
                    }
                    try {
                        ConnectionApi.connectionSendReuse(handle, invitationDetails).whenComplete((res, e) -> {
                            // TODO: await for handshake accepted
                            if (e != null) {
                                Logger.getInstance().e("Failed to reuse connection: ", e);
                                result.completeExceptionally(e);
                            } else {
                                System.out.println(res + "connectionSendReuse");
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