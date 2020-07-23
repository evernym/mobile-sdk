package me.connect.sdk.java.sample.proofs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import me.connect.sdk.java.ConnectMeVcxUpdated;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.ProofRequest;

public class ProofRequestsViewModel extends AndroidViewModel {
    private final Database db;
    private MutableLiveData<List<ProofRequest>> proofRequests;

    public ProofRequestsViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<List<ProofRequest>> getProofRequests() {
        if (proofRequests == null) {
            proofRequests = new MutableLiveData<>();
        }
        loadProofRequests();
        return proofRequests;
    }

    public SingleLiveData<Boolean> acceptProofRequest(int proofId) {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        acceptProofReq(proofId, data);
        return data;
    }

    private void acceptProofReq(int proofId, SingleLiveData<Boolean> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ProofRequest proof = db.proofRequestDao().getById(proofId);
            Connection con = db.connectionDao().getById(proof.connectionId);
            ConnectMeVcxUpdated.retrieveCredentialsForProof(proof.serialized).handle((creds, err) -> {
                if (err != null) {
                    liveData.postValue(false);
                    return null;
                }
                // We automatically map first of each provided credentials to final structure
                // This process should be interactive in real app
                String data = ConnectMeVcxUpdated.mapCredentials(creds);
                ConnectMeVcxUpdated.sendProof(con.serialized, proof.serialized, data, "{}").handle((s, e) -> {
                    if (s != null) {
                        String serializedProof = ConnectMeVcxUpdated.awaitProofStatusChange(s, MessageState.ACCEPTED);
                        proof.accepted = true;
                        proof.serialized = serializedProof;
                        db.proofRequestDao().update(proof);
                    }
                    loadProofRequests();
                    liveData.postValue(e == null);
                    return null;
                });
                return null;
            });
        });
    }

    public SingleLiveData<Boolean> rejectProofRequest(int proofId) {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        rejectProofReq(proofId, data);
        return data;
    }

    private void rejectProofReq(int proofId, SingleLiveData<Boolean> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ProofRequest proof = db.proofRequestDao().getById(proofId);
            Connection con = db.connectionDao().getById(proof.connectionId);
            ConnectMeVcxUpdated.rejectProof(con.serialized, proof.serialized).handle((s, err) -> {
                if (s != null) {
                    String serializedProof = ConnectMeVcxUpdated.awaitProofStatusChange(s, MessageState.REJECTED);
                    proof.serialized = serializedProof;
                    proof.accepted = false;
                    db.proofRequestDao().update(proof);
                }
                loadProofRequests();
                liveData.postValue(err == null);
                return null;
            });
        });
    }

    private void loadProofRequests() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ProofRequest> data = db.proofRequestDao().getAll();
            proofRequests.postValue(data);
        });
    }

    public SingleLiveData<Boolean> getNewProofRequests() {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        checkProofRequests(data);
        return data;
    }

    private void checkProofRequests(SingleLiveData<Boolean> data) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Connection> connections = db.connectionDao().getAll();
            for (Connection c : connections) {
                ConnectMeVcxUpdated.getProofRequests(c.serialized).handle((res, throwable) -> {
                    if (res != null) {
                        for (String proofReq : res) {
                            ProofDataHolder holder = extractRequestedFieldsFromProof(proofReq);
                            if (!db.proofRequestDao().checkExists(holder.threadId)) {
                                ConnectMeVcxUpdated.createProofWithRequest(UUID.randomUUID().toString(), proofReq).handle((pr, err) -> {
                                    ProofRequest proof = new ProofRequest();
                                    proof.serialized = pr;
                                    proof.name = holder.name;
                                    proof.connectionId = c.id;
                                    proof.attributes = holder.attributes;
                                    proof.threadId = holder.threadId;
                                    db.proofRequestDao().insertAll(proof);
                                    loadProofRequests();
                                    return null;
                                });
                            }
                        }
                    }
                    data.postValue(true);
                    return null;
                });
            }
        });
    }

    private ProofDataHolder extractRequestedFieldsFromProof(String proofRequest) {
        try {
            JSONObject json = new JSONObject(proofRequest);
            JSONObject data = json.getJSONObject("proof_request_data");
            String threadId = json.getString("thread_id");
            String name = data.getString("name");
            JSONObject requestedAttrs = data.getJSONObject("requested_attributes");
            Iterator<String> keys = requestedAttrs.keys();
            StringBuilder attributes = new StringBuilder();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = requestedAttrs.getJSONObject(key).getString("name");
                attributes.append(value);
                if (keys.hasNext()) {
                    attributes.append(", ");
                }
            }
            return new ProofDataHolder(threadId, name, attributes.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class ProofDataHolder {
        String threadId;
        String name;
        String attributes;

        public ProofDataHolder(String threadId, String name, String attributes) {
            this.threadId = threadId;
            this.name = name;
            this.attributes = attributes;
        }
    }
}
