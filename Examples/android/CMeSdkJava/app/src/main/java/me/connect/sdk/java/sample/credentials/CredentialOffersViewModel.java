package me.connect.sdk.java.sample.credentials;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

// todo handle errors
public class CredentialOffersViewModel extends AndroidViewModel {
    private final Database db;
    private MutableLiveData<List<CredentialOffer>> credentialOffers;

    public CredentialOffersViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<List<CredentialOffer>> getCredentialOffers() {
        if (credentialOffers == null) {
            credentialOffers = new MutableLiveData<>();
        }
        loadCredentialOffers();
        return credentialOffers;
    }

    private void loadCredentialOffers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CredentialOffer> data = db.credentialOffersDao().getAll();
            credentialOffers.postValue(data);
        });
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
                        loadCredentialOffers();
                        data.postValue(throwable == null);
                        return null;
                    }
            );
        });
    }


    private void checkCredentialOffers(SingleLiveData<Boolean> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Connection> connections = db.connectionDao().getAll();
            for (Connection c : connections) {
                Messages.getPendingMessages(c.serialized, MessageType.CREDENTIAL_OFFER).handle((res, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                    if (res != null) {
                        for (Message message : res) {
                            CredDataHolder holder = extractDataFromCredentialsOfferMessage(message);
                            if (!db.credentialOffersDao().checkOfferExists(holder.id, c.id)) {
                                Credentials.createWithOffer(UUID.randomUUID().toString(), holder.offer).handle((co, err) -> {
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
                                    loadCredentialOffers();
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

    private CredDataHolder extractDataFromCredentialsOfferMessage(Message msg) {
        try {
            JSONObject data = new JSONArray(msg.getPayload()).getJSONObject(0);
            String id = data.getString("claim_id");
            String name = data.getString("claim_name");
            JSONObject attributesJson = data.getJSONObject("credential_attrs");
            StringBuilder attributes = new StringBuilder();
            Iterator<String> keys = attributesJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = attributesJson.getString(key);
                attributes.append(String.format("%s: %s\n", key, value));
            }
            return new CredDataHolder(id, name, attributes.toString(), msg.getPayload());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class CredDataHolder {
        String id;
        String name;
        String attributes;
        String offer;

        public CredDataHolder(String id, String name, String attributes, String offer) {
            this.id = id;
            this.name = name;
            this.attributes = attributes;
            this.offer = offer;
        }
    }
}
