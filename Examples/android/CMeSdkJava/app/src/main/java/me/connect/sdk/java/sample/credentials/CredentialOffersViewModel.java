package me.connect.sdk.java.sample.credentials;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
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
            Connection connection = db.connectionDao().getById(offer.connectionId);
            Credentials.acceptOffer(connection.serialized, offer.serialized, offer.messageId).handle((s, throwable) -> {
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
            List<Connection> connections = db.connectionDao().getAllAsync();
            for (Connection c : connections) {
                Messages.getPendingMessages(c.serialized, MessageType.CREDENTIAL_OFFER).handle((res, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                    if (res != null) {
                        for (Message message : res) {
                            CredDataHolder holder = CredDataHolder.extractDataFromCredentialsOfferMessage(message);
                            if (!db.credentialOffersDao().checkOfferExists(holder.id, c.id)) {
                                Credentials.createWithOffer(c.serialized, UUID.randomUUID().toString(), holder.offer).handle((co, err) -> {
                                    if (err != null) {
                                        err.printStackTrace();
                                    } else {
                                        CredentialOffer offer = new CredentialOffer();
                                        offer.claimId = holder.id;
                                        offer.name = holder.name;
                                        offer.connectionId = c.id;
                                        offer.attributes = holder.attributes;
                                        offer.serialized = co;
                                        offer.messageId = message.getUid();
                                        db.credentialOffersDao().insertAll(offer);
                                    }
                                    return null;
                                });
                            }
                        }
                    }
                    liveData.postValue(true);
                    return res;
                });
            }
        });
    }
}
