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
 * WIP class to provide simplified methods for LibVCX interaction.
 * Should be merged into ConnectMeVcx
 */
public class ConnectMeVcxUpdated {

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
     * Get credential offers
     *
     * @param connection serialized connection
     * @return {@link CompletableFuture} containing list of credential offers as JSON strings.
     */
    public static @NonNull
    CompletableFuture<List<String>> getCredentialOffers(@NonNull String connection) {
        Log.i(TAG, "Getting credential offers");
        CompletableFuture<List<String>> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(connection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed to deserialize connection", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    CredentialApi.credentialGetOffers(conHandle).whenComplete((offersJson, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Failed to get credential offers", e);
                            result.completeExceptionally(e);
                            return;
                        }
                        Log.i(TAG, "Received credential offers");
                        try {
                            JSONArray offerArray = new JSONArray(offersJson);
                            List<String> offers = new ArrayList<>();
                            for (int i = 0; i < offerArray.length(); i++) {
                                offers.add(offerArray.getString(i));
                            }
                            result.complete(offers);
                        } catch (JSONException ex) {
                            result.completeExceptionally(ex);
                        }
                    });
                } catch (VcxException ex) {
                    Log.e(TAG, "Failed to get credential offers", ex);
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Failed to deserialize connection", ex);
            result.completeExceptionally(ex);
        }
        return result;
    }


    /**
     * Get proof requests
     *
     * @param connection serialized connection
     * @return {@link CompletableFuture} containing list of proof requests as JSON strings.
     */
    public static @NonNull
    CompletableFuture<List<String>> getProofRequests(@NonNull String connection) {
        Log.i(TAG, "Getting proof requests");
        CompletableFuture<List<String>> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(connection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed to deserialize connection", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    DisclosedProofApi.proofGetRequests(conHandle).whenComplete((offersJson, er) -> {
                        if (er != null) {
                            Log.e(TAG, "Failed to get proof requests", er);
                            result.completeExceptionally(er);
                            return;
                        }
                        Log.i(TAG, "Received proof requests");
                        try {
                            JSONArray offerArray = new JSONArray(offersJson);
                            List<String> offers = new ArrayList<>();
                            for (int i = 0; i < offerArray.length(); i++) {
                                offers.add(offerArray.getString(i));
                            }
                            result.complete(offers);
                        } catch (JSONException ex) {
                            result.completeExceptionally(ex);
                        }

                    });
                } catch (VcxException ex) {
                    Log.e(TAG, "Failed to get proof requests", ex);
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Failed to deserialize connection", ex);
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Create credential offer
     *
     * @param serializedConnection serialized connection string
     * @param sourceId             custom string for this cred offer
     * @param message              credential offer string
     * @return serialized credential offer
     */
    public static @NonNull
    CompletableFuture<String> createCredentialWithOffer(@NonNull String serializedConnection, @NonNull String sourceId,
                                                        @NonNull String message) {
        Log.i(TAG, "Accepting credential offer");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    CredentialApi.credentialCreateWithOffer(sourceId, message).whenComplete((credHandle, er) -> {
                        if (er != null) {
                            Log.e(TAG, "Failed to create credential with offer: ", er);
                            result.completeExceptionally(er);
                            return;
                        }
                        try {
                            CredentialApi.credentialSerialize(credHandle).whenComplete((sc, e) -> {
                                if (e != null) {
                                    Log.e(TAG, "Failed to serialize credentials: ", e);
                                    result.completeExceptionally(e);
                                } else {
                                    result.complete(sc);
                                }

                            });
                        } catch (VcxException ex) {
                            result.completeExceptionally(ex);
                        }
                    });
                } catch (Exception ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Accept credential offer
     *
     * @param serializedConnection serialized connection string
     * @param serializedCredOffer  serialized credential offer
     * @return serialized credential offer
     */
    public static @NonNull
    CompletableFuture<String> acceptCredentialOffer(@NonNull String serializedConnection, @NonNull String serializedCredOffer) {
        Log.i(TAG, "Accepting credential offer");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    CredentialApi.credentialDeserialize(serializedCredOffer).whenComplete((credHandle, er) -> {
                        if (er != null) {
                            Log.e(TAG, "Failed to deserialize credential offer: ", er);
                            result.completeExceptionally(er);
                            return;
                        }
                        try {
                            CredentialApi.credentialSendRequest(credHandle, conHandle, 0).whenComplete((v, e) -> {
                                if (e != null) {
                                    Log.e(TAG, "Failed to send credential request: ", e);
                                    result.completeExceptionally(e);
                                    return;
                                }
                                try {
                                    ConnectionApi.connectionGetPwDid(conHandle).whenComplete((pwDid, t) -> {
                                        if (t != null) {
                                            Log.e(TAG, "Failed to get pwDid: ", t);
                                            result.completeExceptionally(t);
                                            return;
                                        }
                                        try {
                                            String messageId = new JSONObject(serializedCredOffer)
                                                    .getJSONObject("data")
                                                    .getJSONObject("credential_offer")
                                                    .getString("msg_ref_id");
                                            String jsonMsg = String.format("[{\"pairwiseDID\" : \"%s\", \"uids\": [\"%s\"]}]", pwDid, messageId);
                                            UtilsApi.vcxUpdateMessages(MessageStatusType.ANSWERED, jsonMsg).whenComplete((v1, error) -> {
                                                if (error != null) {
                                                    Log.e(TAG, "Failed to update messages", error);
                                                    result.completeExceptionally(error);
                                                    return;
                                                }
                                                try {
                                                    CredentialApi.credentialSerialize(credHandle).whenComplete((sc, th) -> {
                                                        if (th != null) {
                                                            Log.e(TAG, "Failed to serialize credentials: ", th);
                                                            result.completeExceptionally(th);
                                                        } else {
                                                            result.complete(sc);
                                                        }
                                                    });
                                                } catch (VcxException ex) {
                                                    Log.e(TAG, "Failed to serialize credentials: ", ex);
                                                    result.completeExceptionally(ex);
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
                            Log.e(TAG, "Failed to send credential request: ", ex);
                            result.completeExceptionally(ex);
                        }
                    });
                } catch (Exception ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Loops indefinitely until credential request status is not changed
     *
     * @param serializedCredential string containing serialized credential request
     * @return string containing serialized credential
     */
    public static String awaitCredentialStatusChange(String serializedCredential, MessageState messageState) {
        Log.i(TAG, "Awaiting cred state change");
        int count = 1;
        try {
            Integer handle = CredentialApi.credentialDeserialize(serializedCredential).get();
            // fixme all await methods should be cancellable
            //       need to limit amount of retries or maximum time spent waiting
            while (true) {
                Log.i(TAG, "Awaiting cred state change: attempt #" + count);
                Integer state0 = CredentialApi.credentialUpdateState(handle).get();
                Log.i(TAG, "Awaiting cred state change: update state=" + state0);
                Integer state = CredentialApi.credentialGetState(handle).get();
                Log.i(TAG, "Awaiting cred state change: got state=" + state);
                if (messageState.matches(state)) {
                    return CredentialApi.credentialSerialize(handle).get();
                }
                count++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to await cred state", e);
            e.printStackTrace();
        }
        return serializedCredential;
    }

    /**
     * Loops indefinitely until proof request status is not changed
     *
     * @param serializedProof string containing serialized proof request
     * @param messageState    desired message state
     * @return string containing serialized proof request
     */
    public static String awaitProofStatusChange(String serializedProof, MessageState messageState) {
        Log.i(TAG, "Awaiting proof state change");
        int count = 1;
        try {
            Integer handle = DisclosedProofApi.proofDeserialize(serializedProof).get();
            while (true) {
                Log.i(TAG, "Awaiting proof state change: attempt #" + count);
                Integer state0 = DisclosedProofApi.proofUpdateState(handle).get();
                Log.i(TAG, "Awaiting proof state change: update state=" + state0);
                Integer state = DisclosedProofApi.proofGetState(handle).get();
                Log.i(TAG, "Awaiting proof state change: got state=" + state);
                if (messageState.matches(state)) {
                    return DisclosedProofApi.proofSerialize(handle).get();
                }
                count++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to await proof state", e);
            e.printStackTrace();
        }
        return serializedProof;
    }

    /**
     * Creates proof request
     *
     * @param sourceId custom string for this cred offer
     * @param message  proof request string
     * @return {@link CompletableFuture} containing serialized proof request
     */
    public static @NonNull
    CompletableFuture<String> createProofWithRequest(@NonNull String sourceId,
                                                     @NonNull String message) {
        Log.i(TAG, "Retrieving proof request");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            DisclosedProofApi.proofCreateWithRequest(sourceId, message).whenComplete((proofHandle, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed create proof with request: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    DisclosedProofApi.proofSerialize(proofHandle).whenComplete((sp, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Failed to serialize proof request: ", e);
                            result.completeExceptionally(e);
                        } else {
                            result.complete(sp);
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
     * Retrieves available credentials for proof request
     *
     * @param serializedProof proof request string
     * @return {@link CompletableFuture} containing string with available credentials
     */
    public static @NonNull
    CompletableFuture<String> retrieveCredentialsForProof(@NonNull String serializedProof) {
        Log.i(TAG, "Retrieving credentials for proof request");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            DisclosedProofApi.proofDeserialize(serializedProof).whenComplete((proofHandle, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed deserialize proof request: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    DisclosedProofApi.proofRetrieveCredentials(proofHandle).whenComplete((retrievedCreds, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Failed to retrieve proof credentials: ", e);
                            result.completeExceptionally(e);
                        } else {
                            result.complete(retrievedCreds);
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
     * Respond to proof request with credentials
     *
     * @param serializedConnection   string containing serialized connection
     * @param serializedProof        string containing serialized proof request
     * @param selectedCreds          selected credentials to provide proof
     * @param selfAttestedAttributes user-defined attributes to provide proof
     * @return CompletableFuture containing serialized proof
     */
    public static
    @NonNull
    CompletableFuture<String> sendProof(@NonNull String serializedConnection, @NonNull String serializedProof,
                                        @NonNull String selectedCreds, @NonNull String selfAttestedAttributes) {
        Log.i(TAG, "Sending proof request response");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    DisclosedProofApi.proofDeserialize(serializedProof).whenComplete((pHandle, er) -> {
                        if (er != null) {
                            Log.e(TAG, "Failed to deserialize proof: ", er);
                            result.completeExceptionally(er);
                            return;
                        }
                        try {
                            DisclosedProofApi.proofGenerate(pHandle, selectedCreds, selfAttestedAttributes).whenComplete((v, e) -> {
                                if (e != null) {
                                    Log.e(TAG, "Failed to generate proof: ", e);
                                    result.completeExceptionally(e);
                                    return;
                                }
                                try {
                                    DisclosedProofApi.proofSend(pHandle, conHandle).whenComplete((r, error) -> {
                                        if (error != null) {
                                            Log.e(TAG, "Failed to send proof: ", error);
                                            result.completeExceptionally(error);
                                            return;
                                        }
                                        try {
                                            DisclosedProofApi.proofSerialize(pHandle).whenComplete((sp, t) -> {
                                                if (t != null) {
                                                    Log.e(TAG, "Failed to serialize proof: ", t);
                                                    result.completeExceptionally(t);
                                                } else {
                                                    result.complete(sp);
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
                        } catch (VcxException ex) {
                            result.completeExceptionally(ex);
                        }
                    });
                } catch (Exception ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Reject proof request
     *
     * @param serializedConnection string containing serialized connection
     * @param serializedProof      string containing serialized proof request
     * @return CompletableFuture containing serialized proof
     */
    public static
    @NonNull
    CompletableFuture<String> rejectProof(@NonNull String serializedConnection, @NonNull String serializedProof) {
        Log.i(TAG, "Sending proof request response");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    DisclosedProofApi.proofDeserialize(serializedProof).whenComplete((pHandle, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Failed to deserialize proof: ", e);
                            result.completeExceptionally(e);
                            return;
                        }
                        try {
                            DisclosedProofApi.proofReject(pHandle, conHandle).whenComplete((v, er) -> {
                                if (er != null) {
                                    Log.e(TAG, "Failed to reject proof: ", er);
                                    result.completeExceptionally(er);
                                    return;
                                }
                                try {
                                    DisclosedProofApi.proofSerialize(pHandle).whenComplete((sp, t) -> {
                                        if (t != null) {
                                            Log.e(TAG, "Failed to serialize proof: ", t);
                                            result.completeExceptionally(t);
                                        } else {
                                            result.complete(sp);
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
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * @param serializedConnection JSON string containing serialized connection
     * @param messageId            message ID
     * @param answer               nonce value of the answer
     * @return {@link CompletableFuture} containing message ID
     */
    public static @NonNull
    CompletableFuture<String> answerStructuredMessage(@NonNull String serializedConnection, @NonNull String messageId,
                                                      @NonNull String answer) {
        Log.i(TAG, "Respond to structured message");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Log.e(TAG, "Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                byte[] encodedAnswer = Base64.encode(answer.getBytes(), Base64.NO_WRAP);
                try {
                    ConnectionApi.connectionSignData(conHandle, encodedAnswer, encodedAnswer.length).whenComplete((signature, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Failed to sign data: ", e);
                            result.completeExceptionally(e);
                            return;
                        }
                        try {
                            MessageHolder msg = MessageUtils.prepareAnswer(encodedAnswer, signature, messageId);
                            ConnectionApi.connectionSendMessage(conHandle, msg.getMessage(), msg.getMessageOptions()).whenComplete((r, t) -> {
                                if (t != null) {
                                    Log.e(TAG, "Failed to send message: ", t);
                                    result.completeExceptionally(t);
                                } else {
                                    result.complete(r);
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
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Create wallet backup
     *
     * @param sourceId  - String with institution's personal identification for the user
     * @param backupKey - String representing the User's Key for securing  the exported wallet
     * @return CompletableFuture containing JSON string with serialized wallet backup
     */
    @ExperimentalWalletBackup
    static @NonNull
    CompletableFuture<String> createBackup(@NonNull String sourceId, @NonNull String backupKey) {
        Log.i(TAG, "Creating backup");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            WalletApi.createWalletBackup(sourceId, backupKey)
                    .exceptionally(t -> {
                        Log.e(TAG, "Failed to create wallet backup: ", t);
                        result.completeExceptionally(t);
                        return null;
                    })
                    .thenAccept(backupHandle -> {
                        if (backupHandle == null) {
                            return;
                        }
                        try {
                            WalletApi.serializeBackupWallet(backupHandle)
                                    .exceptionally(t -> {
                                        Log.e(TAG, "Failed to serialize wallet backup: ", t);
                                        result.completeExceptionally(t);
                                        return null;
                                    })
                                    .thenAccept(serializedHandle -> {
                                        if (serializedHandle == null) {
                                            return;
                                        }
                                        result.complete(serializedHandle);
                                    });
                        } catch (VcxException e) {
                            result.completeExceptionally(e);
                        }
                    });
        } catch (VcxException e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     * Generate ZIP archive in directories relative to library root directory.
     *
     * @param context      Context
     * @param relativePath relative path to file or directory to put into archive
     * @param archivePath  relative archive path
     * @return absolute path of archive
     * @throws IOException in case there are problems during working with files
     */
    static String generateBackupArchive(Context context, String relativePath, String archivePath) throws IOException {
        String walletPath = Utils.getRootDir(context) + "/" + relativePath;
        String backupPath = Utils.getRootDir(context) + "/" + archivePath;
        Utils.zipFiles(walletPath, backupPath);
        return backupPath;
    }

    // Todo Context object should be taken from ConnectMeVcx after merge

    /**
     * Perform backup process
     *
     * @param context          context
     * @param serializedBackup String containing serialized backup wallet handle
     * @return CompletableFuture containig serialized backup wallet handle
     */
    @ExperimentalWalletBackup
    static @NonNull
    CompletableFuture<String> performBackup(@NonNull Context context, @NonNull String serializedBackup) {
        Log.i(TAG, "Performing backup");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            String pathToArchive = generateBackupArchive(context, "indy_client", "backup-" + System.currentTimeMillis());
            WalletApi.deserializeBackupWallet(serializedBackup)
                    .exceptionally(t -> {
                        Log.e(TAG, "Failed to deserialize wallet backup: ", t);
                        result.completeExceptionally(t);
                        return null;
                    })
                    .thenAccept(backupHandle -> {
                        if (backupHandle == null) {
                            return;
                        }
                        try {
                            WalletApi.backupWalletBackup(backupHandle, pathToArchive)
                                    .whenComplete((v, err) -> {
                                        if (err != null) {
                                            Log.e(TAG, "Failed to backup wallet: ", err);
                                            result.completeExceptionally(err);
                                            return;
                                        }
                                        try {
                                            WalletApi.serializeBackupWallet(backupHandle)
                                                    .exceptionally(t -> {
                                                        Log.e(TAG, "Failed to serialize wallet backup: ", t);
                                                        result.completeExceptionally(t);
                                                        return null;
                                                    })
                                                    .thenAccept(serializedHandle -> {
                                                        if (serializedHandle == null) {
                                                            return;
                                                        }
                                                        result.complete(serializedHandle);
                                                    });
                                        } catch (VcxException e) {
                                            result.completeExceptionally(e);
                                        }
                                    });
                        } catch (VcxException e) {
                            result.completeExceptionally(e);
                        }
                    });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }


    /**
     * Restores wallet
     *
     * @param context   context
     * @param backupKey backup wallet key used during backup wallet creation
     * @return CompletableFuture with nothing
     */
    @ExperimentalWalletBackup
    static @NonNull
    CompletableFuture<Void> restoreBackup(@NonNull Context context, @NonNull String backupKey) {
        // Todo Context object should be taken from ConnectMeVcx after merge
        Log.i(TAG, "Restoring backup");
        CompletableFuture<Void> result = new CompletableFuture<>();

        try {
            JSONObject config = new JSONObject();
            config.put("wallet_name", "restoredWalletName");
            config.put("wallet_key", "restoredWalletKey");
            config.put("backup_key", backupKey);
            config.put("exported_wallet_path", Utils.getRootDir(context) + "/restored");
            WalletApi.restoreWalletBackup(config.toString())
                    .whenComplete((v, err) -> {
                        if (err != null) {
                            Log.e(TAG, "Failed to backup wallet: ", err);
                            result.completeExceptionally(err);
                        } else {
                            result.complete(null);
                        }
                    });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    /**
     * Loops indefinitely until wallet backup status is not changed
     *
     * @param serializedBackup string containing serialized wallet backup
     * @return string containing serialized wallet backup
     */
    @ExperimentalWalletBackup
    static @NonNull
    String awaitBackupStatusChange(@NonNull String serializedBackup) {
        Log.i(TAG, "Awaiting backup state change");
        int count = 1;
        try {
            Integer handle = WalletApi.deserializeBackupWallet(serializedBackup).get();
            while (true) {
                Log.i(TAG, "Awaiting backup state change: attempt #" + count);
                Integer state = WalletApi.updateWalletBackupState(handle).get();
                Log.i(TAG, "Awaiting backup state change: update state=" + state);
                if (state == 4 || state == 2) {
                    return WalletApi.serializeBackupWallet(handle).get();
                }
                count++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to await proof state", e);
            e.printStackTrace();
        }
        return serializedBackup;
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

    /**
     * Temporary method to parse structured question message JSON string and extract {@link StructuredMessage} from it.
     *
     * @param message JSON string containing Structured question message
     * @return parsed {@link StructuredMessage}
     */
    public static StructuredMessage extractStructuredMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String payload = jsonObject.getString("decryptedPayload");
            String msgString = new JSONObject(payload).getString("@msg");
            JSONObject msg = new JSONObject(msgString);
            String messageId = jsonObject.getString("uid");
            String id = msg.getString("@id");
            String questionText = msg.getString("question_text");
            String questionDetail = msg.getString("question_detail");
            ArrayList<StructuredMessage.Response> responses = new ArrayList<>();
            JSONArray jsonResponses = msg.getJSONArray("valid_responses");
            for (int i = 0; i < jsonResponses.length(); i++) {
                JSONObject response = jsonResponses.getJSONObject(i);
                String text = response.getString("text");
                String nonce = response.getString("nonce");
                StructuredMessage.Response res = new StructuredMessage.Response(text, nonce);
                responses.add(res);
            }
            return new StructuredMessage(id, questionText, questionDetail, responses, messageId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Temporary method to extract credentials for proof request and prepare map that will be used later in proof request confirmation
     *
     * @param proofRequestCreds JSON string containing list of available credentials
     * @return JSON string containing prepared payload for proof request
     */
    public static String mapCredentials(String proofRequestCreds) {
        try {
            JSONObject result = new JSONObject();
            JSONObject resultAttrs = new JSONObject();
            result.put("attrs", resultAttrs);
            JSONObject data = new JSONObject(proofRequestCreds);
            JSONObject attrs = data.getJSONObject("attrs");
            for (Iterator<String> it = attrs.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONArray credArray = attrs.getJSONArray(key);
                JSONObject cred = credArray.getJSONObject(0);
                JSONObject credHolder = new JSONObject();
                credHolder.put("credential", cred);
                resultAttrs.put(key, credHolder);
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}