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

import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.Messages;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;
import me.connect.sdk.java.sample.messages.CredDataHolder;

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

    public SingleLiveData<Boolean> getNewCredentialOffers() {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        checkCredentialOffers(data);
        return data;
    }

    public SingleLiveData<Boolean> acceptOffer(int offerId) {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        acceptCredentialOffer(offerId, data);
        return data;
    }

    private void acceptCredentialOffer(int offerId, SingleLiveData<Boolean> data) {
        Executors.newSingleThreadExecutor().execute(() -> {
            CredentialOffer offer = db.credentialOffersDao().getById(offerId);
            Connection connection = db.connectionDao().getByPwDid(offer.pwDid);
            System.out.println("acceptCredentialOffer" + offer.serialized);
            Credentials.acceptOffer(connection.serialized, offer.serialized).handle((s, throwable) -> {
                        if (s != null) {
                            String s2 = Credentials.awaitStatusChange(s, MessageState.ACCEPTED);
                            offer.serialized = s2;
                            offer.accepted = true;
                            db.credentialOffersDao().update(offer);
                        }
                        data.postValue(throwable == null);
                        return null;
                    }
            );
        });
    }

    private void checkCredentialOffers(SingleLiveData<Boolean> liveData) {
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
                liveData.postValue(true);
                return res;
            });
        });
    }
}
