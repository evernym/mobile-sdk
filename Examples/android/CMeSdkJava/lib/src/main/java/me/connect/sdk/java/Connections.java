package me.connect.sdk.java;

import android.content.Context;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;
import com.evernym.sdk.vcx.credential.CredentialApi;
import com.evernym.sdk.vcx.proof.DisclosedProofApi;
import com.evernym.sdk.vcx.utils.UtilsApi;
import com.evernym.sdk.vcx.wallet.WalletApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java9.util.concurrent.CompletableFuture;

import me.connect.sdk.java.connection.Connection;
import me.connect.sdk.java.message.MessageHolder;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.message.MessageStatusType;
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.message.MessageUtils;
import me.connect.sdk.java.message.StructuredMessage;
import me.connect.sdk.java.proof.ProofHolder;

/**
 * Class containing methods to work with connections
 */
public class Connections {

    public static final String TAG = "ConnectMeVcx";

    /**
     * Creates new connection from invitation.
     *
     * @param invitationDetails String containing JSON with invitation details.
     * @param connectionType    {@link Connection} object containing information about connection type.
     * @return {@link CompletableFuture} with serialized connection handle.
     */
    public static @NonNull
    CompletableFuture<String> createConnection(@NonNull String invitationDetails,
                                               @NonNull Connection connectionType) {
        Log.i(TAG, "Starting connection creation");
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
                    Log.e(TAG, "Failed to create connection with invite: ", err);
                    result.completeExceptionally(err);
                }
                Log.i(TAG, "Received handle: " + handle);
                try {
                    String connType = connectionType.getConnectionType();
                    ConnectionApi.vcxAcceptInvitation(handle, connType).whenComplete((invite, t) -> {
                        if (t != null) {
                            Log.e(TAG, "Failed to accept invitation: ", t);
                            result.completeExceptionally(t);
                            return;
                        }
                        Log.i(TAG, "Received invite: " + invite);
                        try {
                            ConnectionApi.connectionSerialize(handle).whenComplete((serializedConn, e) -> {
                                if (e != null) {
                                    Log.e(TAG, "Failed to serialize connection", e);
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
     * Retrieve pending messages.
     *
     * @param serializedConnection String containing JSON with serialized connection details.
     * @param messageType          Type of messages to retrieve
     * @return List of messages
     */
    public static @NonNull
    CompletableFuture<List<String>> getPendingMessages(@NonNull String serializedConnection,
                                                       @NonNull MessageType messageType) {
        Log.i(TAG, "Retrieving pending messages");
        CompletableFuture<List<String>> result = new CompletableFuture<>();
        try {
            String pwDid = new JSONObject(serializedConnection).getJSONObject("data").getString("pw_did");
            UtilsApi.vcxGetMessages(MessageStatusType.PENDING, null, pwDid).whenComplete((messagesString, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed to retrieve messages: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    List<String> messages = new ArrayList<>();
                    JSONArray messagesJson = new JSONArray(messagesString);
                    for (int i = 0; i < messagesJson.length(); i++) {
                        JSONArray msgsJson = messagesJson.getJSONObject(i).optJSONArray("msgs");
                        if (msgsJson != null) {
                            for (int j = 0; j < msgsJson.length(); j++) {
                                JSONObject message = msgsJson.getJSONObject(j);
                                String type = message.getString("type");
                                String msgType;
                                //Fixme workaround to check message type in different protocols
                                if (type.equals("aries")) {
                                    String payload = message.getString("decryptedPayload");
                                    String msg = new JSONObject(payload).getString("@msg");
                                    String mt = new JSONObject(msg).getString("@type");
                                    if (!mt.startsWith("{")) {
                                        continue;
                                    }
                                    type = new JSONObject(mt).getString("name");
                                    msgType = messageType.getAries();
                                } else {
                                    msgType = messageType.getProprietary();
                                }
                                if (type.equals(msgType)) {
                                    messages.add(message.toString());
                                }
                            }
                        }
                    }
                    result.complete(messages);
                } catch (JSONException ex) {
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
    String awaitConnectionStatusChange(@NonNull String serializedConnection, MessageState messageState) {
        Log.i(TAG, "Awaiting connection state change");
        int count = 1;
        try {
            Integer handle = ConnectionApi.connectionDeserialize(serializedConnection).get();
            while (true) {
                Log.i(TAG, "Awaiting connection state change: attempt #" + count);
                Integer state = ConnectionApi.vcxConnectionUpdateState(handle).get();
                Log.i(TAG, "Awaiting connection state change: got state=" + state);
                if (messageState.matches(state)) {
                    return ConnectionApi.connectionSerialize(handle).get();
                }
                count++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to await connection state", e);
            e.printStackTrace();
        }
        return serializedConnection;
    }

}