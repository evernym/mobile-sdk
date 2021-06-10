package me.connect.sdk.java.sample.credentials;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import me.connect.sdk.java.Connections;
import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.Messages;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;
import me.connect.sdk.java.sample.messages.CredDataHolder;

import static me.connect.sdk.java.sample.credentials.CredentialCreateResult.FAILURE;
import static me.connect.sdk.java.sample.credentials.CredentialCreateResult.SUCCESS;
import static me.connect.sdk.java.sample.credentials.CredentialCreateResult.FAILURE_CONNECTION;
import static me.connect.sdk.java.sample.credentials.CredentialCreateResult.SUCCESS_CONNECTION;

// todo handle errors
public class CredentialOffersViewModel extends AndroidViewModel {
    private final Database db;
    private LiveData<List<CredentialOffer>> credentialOffers;

    public CredentialOffersViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<List<CredentialOffer>> getCredentialOffers() {
        if (credentialOffers == null) {
            credentialOffers = db.credentialOffersDao().getAll();
        }
        return credentialOffers;
    }

    public SingleLiveData<CredentialCreateResult> getNewCredentialOffers() {
        SingleLiveData<CredentialCreateResult> data = new SingleLiveData<>();
        checkCredentialOffers(data);
        return data;
    }

    public SingleLiveData<CredentialCreateResult> acceptOffer(int offerId) {
        SingleLiveData<CredentialCreateResult> data = new SingleLiveData<>();
        acceptCredentialOffer(offerId, data);
        return data;
    }

    private void acceptCredentialOfferAndCreateConnection(
            CredentialOffer offer,
            SingleLiveData<CredentialCreateResult> data
        ) {
        Connections.create(offer.attachConnection, new QRConnection())
            .handle((res, throwable) -> {
                if (res != null) {
                    String serializedCon = Connections.awaitStatusChange(res, MessageState.ACCEPTED);

                    String pwDid = Connections.getPwDid(serializedCon);
                    Connection c = new Connection();
                    c.name = offer.attachConnectionName;
                    c.icon = offer.attachConnectionLogo;
                    c.pwDid = pwDid;
                    c.serialized = serializedCon;
                    db.connectionDao().insertAll(c);
                    data.postValue(throwable == null ? SUCCESS_CONNECTION : FAILURE_CONNECTION);

                    Credentials.acceptOffer(serializedCon, offer.serialized).handle((s, thr) -> {
                            if (s != null) {
                                offer.serialized = Credentials.awaitStatusChange(s, MessageState.ACCEPTED);
                                offer.pwDid = pwDid;
                                offer.accepted = true;
                                db.credentialOffersDao().update(offer);
                            }
                            data.postValue(thr == null ? SUCCESS: FAILURE);
                            return null;
                        }
                    );
                }
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                return null;
            });
    }

    private void acceptCredentialOffer(int offerId, SingleLiveData<CredentialCreateResult> data) {
        Executors.newSingleThreadExecutor().execute(() -> {
            CredentialOffer offer = db.credentialOffersDao().getById(offerId);
            if (offer.attachConnection != null) {
                acceptCredentialOfferAndCreateConnection(offer, data);
                return;
            }
            Connection connection = db.connectionDao().getByPwDid(offer.pwDid);
            Credentials.acceptOffer(connection.serialized, offer.serialized).handle((s, throwable) -> {
                    if (s != null) {
                        offer.serialized = Credentials.awaitStatusChange(s, MessageState.ACCEPTED);
                        offer.accepted = true;
                        db.credentialOffersDao().update(offer);
                    }
                    data.postValue(throwable == null ? SUCCESS: FAILURE);
                    return null;
                }
            );
        });
    }

    private void checkCredentialOffers(SingleLiveData<CredentialCreateResult> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Messages.getPendingMessages(MessageType.CREDENTIAL_OFFER, null, null).handle((res, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                if (res != null) {
                    for (Message message : res) {
                        CredDataHolder holder = CredDataHolder.extractDataFromCredentialsOfferMessage(message);
                        String pwDid = message.getPwDid();
                        if (!db.credentialOffersDao().checkOfferExists(pwDid)) {
                            try {
                                Credentials.createWithOffer(UUID.randomUUID().toString(), holder.offer).handle((co, err) -> {
                                    if (err != null) {
                                        err.printStackTrace();
                                    } else {
                                        CredentialOffer offer = new CredentialOffer();
                                        offer.claimId = holder.id;
                                        offer.name = holder.name;
                                        offer.pwDid = pwDid;
                                        offer.attributes = holder.attributes;
                                        offer.serialized = co;
                                        offer.messageId = message.getUid();
                                        db.credentialOffersDao().insertAll(offer);

                                        Messages.updateMessageStatus(pwDid, message.getUid());

                                    }
                                    return null;
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                liveData.postValue(SUCCESS);
                return res;
            });
        });
    }
}
