package msdk.java.handlers;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;
import com.evernym.sdk.vcx.credential.CredentialApi;
import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;
import msdk.java.logger.Logger;
import msdk.java.messages.Message;
import msdk.java.types.StateMachineState;
import msdk.java.types.MessageType;

/**
 * Class containig methods to work with credentials
 */
public class Credentials {

    private Credentials() {

    }

    /**
     * Get credential offers
     * Deprecated. Use {@link Messages} Messages.getPendingMessages(String, MessageType) instead.
     *
     * @param connection serialized connection
     * @return {@link CompletableFuture} containing list of credential offers as JSON strings.
     */
    public static @NonNull
    CompletableFuture<List<String>> getOffers(@NonNull String connection) {
        Logger.getInstance().i("Getting credential offers");
        CompletableFuture<List<String>> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(connection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to deserialize connection", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    CredentialApi.credentialGetOffers(conHandle).whenComplete((offersJson, e) -> {
                        if (e != null) {
                            Logger.getInstance().e("Failed to get credential offers", e);
                            result.completeExceptionally(e);
                            return;
                        }
                        Logger.getInstance().i("Received credential offers");
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
                    Logger.getInstance().e("Failed to get credential offers", ex);
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            Logger.getInstance().e("Failed to deserialize connection", ex);
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Create credential offer
     *
     * @param sourceId             custom string for this cred offer
     * @param message              credential offer string
     * @return serialized credential offer
     */
    public static @NonNull
    CompletableFuture<String> createWithOffer(@NonNull String sourceId,
                                              @NonNull String message) {
        Logger.getInstance().i("Accepting credential offer");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            CredentialApi.credentialCreateWithOffer(sourceId, message).whenComplete((credHandle, er) -> {
                if (er != null) {
                    Logger.getInstance().e("Failed to create credential with offer: ", er);
                    result.completeExceptionally(er);
                    return;
                }
                try {
                    CredentialApi.credentialSerialize(credHandle).whenComplete((sc, e) -> {
                        if (e != null) {
                            Logger.getInstance().e("Failed to serialize credentials: ", e);
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
    CompletableFuture<String> acceptOffer(
            @NonNull String serializedConnection,
            @NonNull String serializedCredOffer
    ) {
        Logger.getInstance().i("Accepting credential offer");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    CredentialApi.credentialDeserialize(serializedCredOffer).whenComplete((credHandle, er) -> {
                        if (er != null) {
                            Logger.getInstance().e("Failed to deserialize credential offer: ", er);
                            result.completeExceptionally(er);
                            return;
                        }
                        try {
                            CredentialApi.credentialSendRequest(credHandle, conHandle, 0).whenComplete((v, e) -> {
                                if (e != null) {
                                    Logger.getInstance().e("Failed to send credential request: ", e);
                                    result.completeExceptionally(e);
                                    return;
                                }
                                try {
                                    CredentialApi.credentialSerialize(credHandle).whenComplete((sc, th) -> {
                                        if (th != null) {
                                            Logger.getInstance().e("Failed to serialize credentials: ", th);
                                            result.completeExceptionally(th);
                                        } else {
                                            result.complete(sc);
                                        }
                                    });
                                } catch (VcxException ex) {
                                    Logger.getInstance().e("Failed to serialize credentials: ", ex);
                                    result.completeExceptionally(ex);
                                }
                            });
                        } catch (VcxException ex) {
                            Logger.getInstance().e("Failed to send credential request: ", ex);
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
     * Reject credential offer
     *
     * @param serializedConnection serialized connection string
     * @param serializedCredOffer  serialized credential offer
     * @return serialized credential offer
     */
    public static
    @NonNull
    CompletableFuture<String> rejectOffer (
            @NonNull String serializedConnection,
            @NonNull String serializedCredOffer
    ) {
        Logger.getInstance().i("Sending proof request response");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    CredentialApi.credentialDeserialize(serializedCredOffer).whenComplete((credHandle, er) -> {
                        if (er != null) {
                            Logger.getInstance().e("Failed to deserialize credential offer: ", er);
                            result.completeExceptionally(er);
                            return;
                        }
                        try {
                            CredentialApi.credentialReject(credHandle, conHandle, "").whenComplete((v, e) -> {
                                if (e != null) {
                                    Logger.getInstance().e("Failed to reject proof: ", e);
                                    result.completeExceptionally(e);
                                    return;
                                }
                                try {
                                    CredentialApi.credentialSerialize(credHandle).whenComplete((sc, th) -> {
                                        if (th != null) {
                                            Logger.getInstance().e("Failed to serialize credentials: ", th);
                                            result.completeExceptionally(th);
                                        } else {
                                            result.complete(sc);
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
     * Loops indefinitely until credential request status is not changed
     *
     * @param serializedCredential string containing serialized credential request
     * @return string containing serialized credential
     */
    public static String awaitCredentialReceived(String serializedCredential) {
        Logger.getInstance().i("Awaiting cred state change");
        int count = 1;
        try {
            Integer handle = CredentialApi.credentialDeserialize(serializedCredential).get();
            while (true) {
                Logger.getInstance().i("Awaiting cred state change: attempt #" + count);
                Integer state0 = CredentialApi.credentialUpdateState(handle).get();
                Logger.getInstance().i("Awaiting cred state change: update state=" + state0);
                Integer state = CredentialApi.credentialGetState(handle).get();
                Logger.getInstance().i("Awaiting cred state change: got state=" + state);
                if (StateMachineState.ACCEPTED.matches(state)) {
                    UtilsApi.vcxFetchPublicEntities();
                    return CredentialApi.credentialSerialize(handle).get();
                }
                count++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Logger.getInstance().e("Failed to await cred state", e);
            e.printStackTrace();
        }
        return serializedCredential;
    }

    /**
     * Loops indefinitely until credential request status is not changed
     *
     * @param serializedCredential string containing serialized credential request
     * @param threadId string claimId of credential request
     * @return string containing serialized credential
     */
    public static String awaitCredentialReceived(String serializedCredential, String threadId, String pwDid) {
        Logger.getInstance().i("Awaiting cred state change");
        int status = -1;
        try {
            Integer handle = CredentialApi.credentialDeserialize(serializedCredential).get();
            while (true) {
                try {
                    Message message = Messages.downloadMessage(MessageType.CREDENTIAL, threadId).get();
                    if (message != null) {
                        status = CredentialApi.credentialUpdateStateWithMessage(handle, message.getPayload()).get();
                        Messages.updateMessageStatus(pwDid, message.getUid());
                        if (StateMachineState.ACCEPTED.matches(status)) {
                            UtilsApi.vcxFetchPublicEntities();
                            return CredentialApi.credentialSerialize(handle).get();
                        }
                    }
                    Thread.sleep(1000);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Logger.getInstance().e("Failed to await cred state", e);
            e.printStackTrace();
        }
        return serializedCredential;
    }
}
