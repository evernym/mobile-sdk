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

import me.connect.sdk.java.ConnectMeVcxUpdated;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;

// todo handle errors
public class CredentialOffersViewModel extends AndroidViewModel {
    private final Database db;
    private MutableLiveData<List<CredentialOffer>> credentialOffers;
    private MutableLiveData<Boolean> newCredentialOffers;

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

    public LiveData<Boolean> getNewCredentialOffers() {
        if (newCredentialOffers == null) {
            newCredentialOffers = new MutableLiveData<>();
        }
        checkCredentialOffers();
        return newCredentialOffers;
    }

    public void acceptOffer(int offerId) { //todo liveData
        acceptCredentialOffer(offerId);
    }

    private void acceptCredentialOffer(int offerId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            CredentialOffer offer = db.credentialOffersDao().getById(offerId);
            Connection connection = db.connectionDao().getById(offer.connectionId);
            ConnectMeVcxUpdated.acceptCredentialOffer(connection.serialized, offer.serialized).handle((s, throwable) -> {
                        if (s != null) {
                            String s2 = ConnectMeVcxUpdated.awaitCredentialStatusChange(s);
                            offer.serialized = s2;
                            offer.accepted = true;
                            db.credentialOffersDao().update(offer);
                        }
                        loadCredentialOffers();
                        return null;
                    }
            );
        });
    }


    private void checkCredentialOffers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Connection> connections = db.connectionDao().getAll();
            for (Connection c : connections) {
                ConnectMeVcxUpdated.getCredentialOffers(c.serialized).handle((res, throwable) -> {
                    if (res != null) {
                        for (String credOffer : res) {
                            ConnectMeVcxUpdated.createCredentialWithOffer(c.serialized, UUID.randomUUID().toString(), credOffer).handle((co, err) -> {
                                CredDataHolder holder = extractDataFromCredentialsOfferString(credOffer);
                                if (!db.credentialOffersDao().checkOfferExists(holder.id)) {
                                    CredentialOffer offer = new CredentialOffer();
                                    offer.claimId = holder.id;
                                    offer.name = holder.name;
                                    offer.connectionId = c.id;
                                    offer.attributes = holder.attributes;
                                    offer.serialized = co;
                                    db.credentialOffersDao().insertAll(offer);
                                }
                                loadCredentialOffers();
                                return null;
                            });
                        }
                    }
                    return res;
                });
            }
            newCredentialOffers.postValue(true);
        });
    }

    private CredDataHolder extractDataFromCredentialsOfferString(String str) {
        try {
            JSONObject data = new JSONArray(str).getJSONObject(0);
            Integer id = data.getInt("claim_id");
            String name = data.getString("claim_name");
            JSONObject attributesJson = data.getJSONObject("credential_attrs");
            StringBuilder attributes = new StringBuilder();
            Iterator<String> keys = attributesJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = attributesJson.getString(key);
                attributes.append(String.format("%s: %s\n", key, value));
            }
            return new CredDataHolder(id, name, attributes.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class CredDataHolder {
        Integer id;
        String name;
        String attributes;

        public CredDataHolder(Integer id, String name, String attributes) {
            this.id = id;
            this.name = name;
            this.attributes = attributes;
        }
    }
}
