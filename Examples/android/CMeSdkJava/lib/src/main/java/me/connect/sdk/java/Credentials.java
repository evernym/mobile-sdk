package me.connect.sdk.java;

import android.util.Log;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;
import com.evernym.sdk.vcx.credential.CredentialApi;
import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.message.MessageStatusType;

/**
 * Class containig methods to work with credentials
 */
public class Credentials {

    public static final String TAG = "ConnectMeVcx";

    private Credentials() {

    }

    /**
     * Get credential offers
     *
     * @param connection serialized connection
     * @return {@link CompletableFuture} containing list of credential offers as JSON strings.
     */
    public static @NonNull
    CompletableFuture<List<String>> getOffers(@NonNull String connection) {
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
     * Create credential offer
     *
     * @param serializedConnection serialized connection string
     * @param sourceId             custom string for this cred offer
     * @param message              credential offer string
     * @return serialized credential offer
     */
    public static @NonNull
    CompletableFuture<String> createWithOffer(@NonNull String serializedConnection, @NonNull String sourceId,
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
    CompletableFuture<String> acceptOffer(@NonNull String serializedConnection, @NonNull String serializedCredOffer) {
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
    public static String awaitStatusChange(String serializedCredential, MessageState messageState) {
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
}
