package me.connect.sdk.java;

import android.util.Log;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;
import com.evernym.sdk.vcx.credential.CredentialApi;
import com.evernym.sdk.vcx.proof.DisclosedProofApi;
import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java9.util.concurrent.CompletableFuture;

import me.connect.sdk.java.connection.Connection;
import me.connect.sdk.java.message.MessageStatusType;
import me.connect.sdk.java.message.MessageType;
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

            ConnectionApi.vcxCreateConnectionWithInvite(invitationId, invitationDetails)
                    .exceptionally(t -> {
                        Log.e(TAG, "Failed to create connection with invite: ", t);
                        result.completeExceptionally(t);
                        return null;
                    })
                    .thenApply(handle -> {
                        if (handle == null) {
                            return null;
                        }
                        Log.i(TAG, "Received handle: " + handle);
                        try {
                            String connType = connectionType.getConnectionType();
                            return ConnectionApi.vcxAcceptInvitation(handle, connType)
                                    .exceptionally(t -> {
                                        Log.e(TAG, "Failed to accept invitation: ", t);
                                        result.completeExceptionally(t);
                                        return null;
                                    })
                                    .thenApply(invite -> {
                                        if (invite == null) {
                                            return null;
                                        }
                                        Log.i(TAG, "Received invite: " + invite);
                                        try {
                                            return ConnectionApi.connectionSerialize(handle)
                                                    .exceptionally(t -> {
                                                        Log.e(TAG, "Failed to serialize connection", t);
                                                        result.completeExceptionally(t);
                                                        return null;
                                                    })
                                                    .thenAccept(result::complete);
                                        } catch (VcxException e) {
                                            result.completeExceptionally(e);
                                            return null;
                                        }
                                    });
                        } catch (VcxException e) {
                            result.completeExceptionally(e);
                            return null;
                        }
                    });
        } catch (Exception e) {
            result.completeExceptionally(e);
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
            UtilsApi.vcxGetMessages(MessageStatusType.PENDING, null, pwDid)
                    .exceptionally(t -> {
                        Log.e(TAG, "Failed to retrieve messages: ", t);
                        result.completeExceptionally(t);
                        return null;
                    })
                    .thenAccept(messagesString -> {
                        if (messagesString == null) {
                            return;
                        }
                        try {
                            List<String> messages = new ArrayList<String>();
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
                        } catch (JSONException e) {
                            result.completeExceptionally(e);
                        }

                    });
        } catch (Exception e) {
            result.completeExceptionally(e);
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
            ConnectionApi.connectionDeserialize(connection)
                    .exceptionally(t -> {
                        Log.e(TAG, "Failed to deserialize connection", t);
                        result.completeExceptionally(t);
                        return null;
                    })
                    .thenAccept(conHandle -> {
                        try {
                            CredentialApi.credentialGetOffers(conHandle)
                                    .exceptionally(t -> {
                                        Log.e(TAG, "Failed to get credential offers", t);
                                        result.completeExceptionally(t);
                                        return null;
                                    })
                                    .thenAccept(offersJson -> {
                                        if (offersJson == null) {
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
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            result.completeExceptionally(e);
                                        }

                                    });
                        } catch (VcxException e) {
                            Log.e(TAG, "Failed to get credential offers", e);
                            result.completeExceptionally(e);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Failed to deserialize connection", e);
            result.completeExceptionally(e);
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
            ConnectionApi.connectionDeserialize(connection)
                    .exceptionally(t -> {
                        Log.e(TAG, "Failed to deserialize connection", t);
                        result.completeExceptionally(t);
                        return null;
                    })
                    .thenAccept(conHandle -> {
                        try {
                            DisclosedProofApi.proofGetRequests(conHandle)
                                    .exceptionally(t -> {
                                        Log.e(TAG, "Failed to get proof requests", t);
                                        result.completeExceptionally(t);
                                        return null;
                                    })
                                    .thenAccept(offersJson -> {
                                        if (offersJson == null) {
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
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            result.completeExceptionally(e);
                                        }

                                    });
                        } catch (VcxException e) {
                            Log.e(TAG, "Failed to get proof requests", e);
                            result.completeExceptionally(e);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Failed to deserialize connection", e);
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     * Accept credential offer
     *
     * @param serializedConnection serialized connection string
     * @param sourceId             custom string for this cred offer
     * @param message              credential offer string
     * @return serialized credential offer
     */
    public static @NonNull
    CompletableFuture<String> acceptCredentialOffer(@NonNull String serializedConnection, @NonNull String sourceId,
                                                    @NonNull String message) {
        Log.i(TAG, "Accepting credential offer");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection)
                    .exceptionally(t -> {
                        Log.e(TAG, "Failed to deserialize connection: ", t);
                        result.completeExceptionally(t);
                        return null;
                    }).thenApply(conHandle -> {
                if (conHandle == null) {
                    return null;
                }
                try {
                    CredentialApi.credentialCreateWithOffer(sourceId, message)
                            .exceptionally(t -> {
                                Log.e(TAG, "Failed to create credential with offer: ", t);
                                result.completeExceptionally(t);
                                return null;
                            })
                            .thenApply(credentialHandle -> {
                                        try {
                                            CredentialApi.credentialSerialize(credentialHandle)
                                                    .exceptionally(t -> {
                                                        Log.e(TAG, "Failed to serialize credentials: ", t);
                                                        result.completeExceptionally(t);
                                                        return null;
                                                    }).thenApply(
                                                    cred -> {
                                                        if (cred == null) {
                                                            return null;
                                                        }
                                                        try {
                                                            CredentialApi.credentialDeserialize(cred)
                                                                    .exceptionally(t -> {
                                                                        Log.e(TAG, "Failed to cred deserialize: ", t);
                                                                        result.completeExceptionally(t);
                                                                        return null;
                                                                    })
                                                                    .thenApply(credHandle -> {
                                                                                if (credHandle == null) {
                                                                                    return null;
                                                                                }
                                                                                try {
                                                                                    CredentialApi.credentialSendRequest(credHandle, conHandle, 0)
                                                                                            .exceptionally(t -> {
                                                                                                Log.e(TAG, "Failed to send credential request: ", t);
                                                                                                result.completeExceptionally(t);
                                                                                                return null;
                                                                                            })
                                                                                            .thenAccept(str -> {
                                                                                                        if (str == null) {
                                                                                                            return;
                                                                                                        }
                                                                                                        try {
                                                                                                            String data = new JSONObject(serializedConnection).getString("data");
                                                                                                            String pwDid = new JSONObject(data).getString("pw_did");
                                                                                                            String messageId = new JSONArray(message).getJSONObject(0).getString("msg_ref_id");
                                                                                                            String jsonMsg = String.format("[{\"pairwiseDID\" : \"%s\", \"uids\": [\"%s\"]}]", pwDid, messageId);
                                                                                                            UtilsApi.vcxUpdateMessages(MessageStatusType.ANSWERED, jsonMsg).thenAccept(i -> {
                                                                                                                try {
                                                                                                                    CredentialApi.credentialSerialize(credHandle)
                                                                                                                            .exceptionally(t -> {
                                                                                                                                Log.e(TAG, "Failed to serialize credentials: ", t);
                                                                                                                                result.completeExceptionally(t);
                                                                                                                                return null;
                                                                                                                            }).thenAccept(
                                                                                                                            sc -> {
                                                                                                                                if (sc == null) {
                                                                                                                                    return;
                                                                                                                                }
                                                                                                                                result.complete(sc);
                                                                                                                            });
                                                                                                                } catch (VcxException e) {
                                                                                                                    Log.e(TAG, "Failed to serialize credentials: ", e);
                                                                                                                    result.completeExceptionally(e);
                                                                                                                }
                                                                                                            });
                                                                                                            ;
                                                                                                        } catch (Exception e) {
                                                                                                            result.completeExceptionally(e);
                                                                                                        }
                                                                                                    }
                                                                                            );
                                                                                } catch (VcxException e) {
                                                                                    Log.e(TAG, "Failed to send credential request: ", e);
                                                                                    result.completeExceptionally(e);
                                                                                    return null;
                                                                                }
                                                                                return null;
                                                                            }
                                                                    );
                                                            return null;
                                                        } catch (VcxException e) {
                                                            e.printStackTrace();
                                                            return null;
                                                        }
                                                    }
                                            );
                                        } catch (VcxException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }
                            );
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
                return null;
            });


        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     * Loops indefinitely until credential request status is not changed
     *
     * @param serializedCredential string containing serialized credential request
     */
    public static void awaitCredentialStatusChange(String serializedCredential) {
        Log.i(TAG, "Awaiting cred state change");
        int count = 1;
        while (true) {
            try {
                Log.i(TAG, "Awaiting cred state change: attempt #" + count);
                Integer handle = CredentialApi.credentialDeserialize(serializedCredential).get();
                Integer state0 = CredentialApi.credentialUpdateState(handle).get();
                Log.i(TAG, "Awaiting cred state change: update state=" + state0);
                Integer state = CredentialApi.credentialGetState(handle).get();
                Log.i(TAG, "Awaiting cred state change: got state=" + state);
                if (state == 4) {
                    break;
                }
                count++;
                Thread.sleep(1000);
            } catch (Exception e) {
                Log.e(TAG, "Failed to await cred state", e);
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Loops indefinitely until proof request status is not changed
     *
     * @param serializedProof string containing serialized proof request
     */
    public static void awaitProofStatusChange(String serializedProof) {
        Log.i(TAG, "Awaiting proof state change");
        int count = 1;
        while (true) {
            try {
                Log.i(TAG, "Awaiting proof state change: attempt #" + count);
                Integer handle = DisclosedProofApi.proofDeserialize(serializedProof).get();
                Integer state0 = DisclosedProofApi.proofUpdateState(handle).get();
                Log.i(TAG, "Awaiting proof state change: update state=" + state0);
                Integer state = DisclosedProofApi.proofGetState(handle).get();
                Log.i(TAG, "Awaiting proof state change: got state=" + state);
                // Fixme state == 2 check is needed due to issues in VCX Java wrapper. Remove after fix released.
                if (state == 4 || state == 2) {
                    break;
                }
                count++;
                Thread.sleep(1000);
            } catch (Exception e) {
                Log.e(TAG, "Failed to await proof state", e);
                e.printStackTrace();
                break;
            }
        }
    }

    public static @NonNull
    CompletableFuture<ProofHolder> retrieveProofRequest(@NonNull String serializedConnection, @NonNull String sourceId,
                                                        @NonNull String message) {
        Log.i(TAG, "Retrieving proof request");
        CompletableFuture<ProofHolder> result = new CompletableFuture<>();
        try {
            ProofHolder holder = new ProofHolder();
            ConnectionApi.connectionDeserialize(serializedConnection)
                    .exceptionally(t -> {
                        Log.e(TAG, "Failed to deserialize connection: ", t);
                        result.completeExceptionally(t);
                        return null;
                    }).thenApply(conHandle -> {
                if (conHandle == null) {
                    return null;
                }
                try {
                    DisclosedProofApi.proofCreateWithRequest(sourceId, message)
                            .exceptionally(t -> {
                                Log.e(TAG, "Failed create proof with request: ", t);
                                result.completeExceptionally(t);
                                return null;
                            })
                            .thenAccept(proofHandle -> {
                                if (proofHandle == null) {
                                    return;
                                }
                                try {
                                    DisclosedProofApi.proofRetrieveCredentials(proofHandle)
                                            .exceptionally(t -> {
                                                Log.e(TAG, "Failed to retrieve proof credentials: ", t);
                                                result.completeExceptionally(t);
                                                return null;
                                            })
                                            .thenAccept(retrievedCreds -> {
                                                        if (retrievedCreds == null) {
                                                            return;
                                                        }
                                                        try {
                                                            DisclosedProofApi.proofSerialize(proofHandle)
                                                                    .exceptionally(t -> {
                                                                        Log.e(TAG, "Failed to serialize proof request: ", t);
                                                                        result.completeExceptionally(t);
                                                                        return null;
                                                                    })
                                                                    .thenAccept(sp -> {
                                                                        holder.setSerializedProof(sp);
                                                                        holder.setRetrievedCredentials(retrievedCreds);
                                                                        result.complete(holder);
                                                                    });
                                                        } catch (VcxException e) {
                                                            result.completeExceptionally(e);
                                                        }
                                                    }
                                            );
                                } catch (VcxException e) {
                                    e.printStackTrace();
                                }
                            });
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
                return null;
            });
        } catch (Exception e) {
            result.completeExceptionally(e);
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
     * @return CompletableFuture containing status code of the operation
     */
    public static
    @NonNull
    CompletableFuture<String> sendProofRequestResponse(@NonNull String serializedConnection, @NonNull String serializedProof,
                                                       @NonNull String selectedCreds, @NonNull String selfAttestedAttributes) {
        Log.i(TAG, "Sending proof request response");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection)
                    .exceptionally(t -> {
                        Log.e(TAG, "Failed to deserialize connection: ", t);
                        result.completeExceptionally(t);
                        return null;
                    }).thenApply(conHandle -> {
                if (conHandle == null) {
                    return null;
                }
                try {
                    DisclosedProofApi.proofDeserialize(serializedProof)
                            .exceptionally(t -> {
                                Log.e(TAG, "Failed to deserialize proof: ", t);
                                result.completeExceptionally(t);
                                return null;
                            })
                            .thenAccept(pHandle -> {
                                if (pHandle == null) {
                                    return;
                                }
                                try {
                                    DisclosedProofApi.proofGenerate(pHandle, selectedCreds, selfAttestedAttributes)
                                            .exceptionally(t -> {
                                                Log.e(TAG, "Failed to generate proof: ", t);
                                                result.completeExceptionally(t);
                                                return null;
                                            })
                                            .thenAccept(res -> {
                                                if (res == null) {
                                                    return;
                                                }
                                                try {
                                                    DisclosedProofApi.proofSend(pHandle, conHandle)
                                                            .exceptionally(t -> {
                                                                Log.e(TAG, "Failed to send proof: ", t);
                                                                result.completeExceptionally(t);
                                                                return null;
                                                            })
                                                            .thenAccept(r -> {
                                                                        if (r == null) {
                                                                            return;
                                                                        }
                                                                        try {
                                                                            DisclosedProofApi.proofSerialize(pHandle).exceptionally(t -> {
                                                                                Log.e(TAG, "Failed to serialize proof: ", t);
                                                                                result.completeExceptionally(t);
                                                                                return null;
                                                                            })
                                                                                    .thenAccept(sp -> {
                                                                                        if (sp == null)
                                                                                            return;
                                                                                        result.complete(sp);
                                                                                    });
                                                                        } catch (VcxException e) {
                                                                            result.completeExceptionally(e);
                                                                        }
                                                                    }
                                                            );
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
                return null;
            });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
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