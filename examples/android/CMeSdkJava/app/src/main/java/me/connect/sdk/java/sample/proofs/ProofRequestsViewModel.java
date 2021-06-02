package me.connect.sdk.java.sample.proofs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import me.connect.sdk.java.Messages;
import me.connect.sdk.java.Proofs;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.ProofRequest;
import me.connect.sdk.java.sample.messages.ProofDataHolder;

public class ProofRequestsViewModel extends AndroidViewModel {
    private final Database db;
    private LiveData<List<ProofRequest>> proofRequests;

    public ProofRequestsViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<List<ProofRequest>> getProofRequests() {
        if (proofRequests == null) {
            proofRequests = db.proofRequestDao().getAll();
        }
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
            Connection con = db.connectionDao().getByPwDid(proof.pwDid);
            Proofs.retrieveAvailableCredentials(proof.serialized).handle((creds, err) -> {
                if (err != null) {
                    liveData.postValue(false);
                    return null;
                }
                // We automatically map first of each provided credentials to final structure
                // This process should be interactive in real app
                String data = Proofs.mapCredentials(creds);
                Proofs.send(con.serialized, proof.serialized, data, "{}", proof.messageId).handle((s, e) -> {
                    if (s != null) {
                        String serializedProof = Proofs.awaitStatusChange(s, MessageState.ACCEPTED);
                        proof.accepted = true;
                        proof.serialized = serializedProof;
                        db.proofRequestDao().update(proof);
                    }
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
            Connection con = db.connectionDao().getByPwDid(proof.pwDid);
            Proofs.reject(con.serialized, proof.serialized, proof.messageId).handle((s, err) -> {
                if (s != null) {
                    String serializedProof = Proofs.awaitStatusChange(s, MessageState.REJECTED);
                    proof.serialized = serializedProof;
                    proof.accepted = false;
                    db.proofRequestDao().update(proof);
                }
                liveData.postValue(err == null);
                return null;
            });
        });
    }

    public SingleLiveData<Boolean> getNewProofRequests() {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        checkProofRequests(data);
        return data;
    }

    private void checkProofRequests(SingleLiveData<Boolean> data) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Messages.getPendingMessages(MessageType.PROOF_REQUEST).handle((res, throwable) -> {
                if (res != null) {
                    for (Message message : res) {
                        ProofDataHolder holder = ProofDataHolder.extractRequestedFieldsFromProofMessage(message);
                        String pwDid = message.getPwDid();
                        if (!db.proofRequestDao().checkExists(holder.threadId)) {
                            Proofs.createWithRequest(UUID.randomUUID().toString(), holder.proofReq).handle((pr, err) -> {
                                if (err != null) {
                                    err.printStackTrace();
                                } else {
                                    ProofRequest proof = new ProofRequest();
                                    proof.serialized = pr;
                                    proof.name = holder.name;
                                    proof.pwDid = pwDid;
                                    proof.attributes = holder.attributes;
                                    proof.threadId = holder.threadId;
                                    proof.messageId = message.getUid();
                                    db.proofRequestDao().insertAll(proof);
                                }
                                return null;
                            });
                        }
                    }
                }
                data.postValue(true);
                return null;
            });
        });
    }
}
