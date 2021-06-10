package me.connect.sdk.java.sample.proofs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import me.connect.sdk.java.Connections;
import me.connect.sdk.java.Messages;
import me.connect.sdk.java.Proofs;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.credentials.CredentialCreateResult;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.ProofRequest;
import me.connect.sdk.java.sample.messages.ProofDataHolder;

import static me.connect.sdk.java.sample.proofs.ProofCreateResult.FAILURE;
import static me.connect.sdk.java.sample.proofs.ProofCreateResult.FAILURE_CONNECTION;
import static me.connect.sdk.java.sample.proofs.ProofCreateResult.SUCCESS;
import static me.connect.sdk.java.sample.proofs.ProofCreateResult.SUCCESS_CONNECTION;

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

    public SingleLiveData<ProofCreateResult> acceptProofRequest(int proofId) {
        SingleLiveData<ProofCreateResult> data = new SingleLiveData<>();
        acceptProofReq(proofId, data);
        return data;
    }

    private void acceptProofReqAndCreateConnection(
            ProofRequest proof,
            SingleLiveData<ProofCreateResult> liveData
    ) {
        Connections.create(proof.attachConnection, new QRConnection())
            .handle((res, throwable) -> {
                if (res != null) {
                    String serializedCon = Connections.awaitStatusChange(res, MessageState.ACCEPTED);

                    String pwDid = Connections.getPwDid(serializedCon);
                    Connection c = new Connection();
                    c.name = proof.attachConnectionName;
                    c.icon = proof.attachConnectionLogo;
                    c.pwDid = pwDid;
                    c.serialized = serializedCon;
                    db.connectionDao().insertAll(c);
                    liveData.postValue(throwable == null ? SUCCESS_CONNECTION : FAILURE_CONNECTION);

                    Proofs.retrieveAvailableCredentials(proof.serialized).handle((creds, err) -> {
                        if (err != null) {
                            liveData.postValue(FAILURE);
                            return null;
                        }
                        // We automatically map first of each provided credentials to final structure
                        // This process should be interactive in real app
                        String data = Proofs.mapCredentials(creds);
                        Proofs.send(serializedCon, proof.serialized, data, "{}").handle((s, e) -> {
                            if (s != null) {
                                String serializedProof = Proofs.awaitStatusChange(s, MessageState.ACCEPTED);
                                proof.accepted = true;
                                proof.pwDid = pwDid;
                                proof.serialized = serializedProof;
                                db.proofRequestDao().update(proof);
                            }
                            liveData.postValue(e == null ? SUCCESS: FAILURE);
                            return null;
                        });
                        return null;
                    });
                }
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                return null;
            });
    }

    private void acceptProofReq(int proofId, SingleLiveData<ProofCreateResult> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ProofRequest proof = db.proofRequestDao().getById(proofId);
            if (proof.attachConnection != null) {
                acceptProofReqAndCreateConnection(proof, liveData);
                return;
            }
            Connection con = db.connectionDao().getByPwDid(proof.pwDid);
            Proofs.retrieveAvailableCredentials(proof.serialized).handle((creds, err) -> {
                if (err != null) {
                    liveData.postValue(FAILURE);
                    return null;
                }
                // We automatically map first of each provided credentials to final structure
                // This process should be interactive in real app
                String data = Proofs.mapCredentials(creds);
                Proofs.send(con.serialized, proof.serialized, data, "{}").handle((s, e) -> {
                    if (s != null) {
                        String serializedProof = Proofs.awaitStatusChange(s, MessageState.ACCEPTED);
                        proof.accepted = true;
                        proof.serialized = serializedProof;
                        db.proofRequestDao().update(proof);
                    }
                    liveData.postValue(e == null ? SUCCESS: FAILURE);
                    return null;
                });
                return null;
            });
        });
    }

    public SingleLiveData<ProofCreateResult> rejectProofRequest(int proofId) {
        SingleLiveData<ProofCreateResult> data = new SingleLiveData<>();
        rejectProofReq(proofId, data);
        return data;
    }

    private void rejectProofReq(int proofId, SingleLiveData<ProofCreateResult> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ProofRequest proof = db.proofRequestDao().getById(proofId);
            if (proof.pwDid == null) {
                proof.accepted = false;
                db.proofRequestDao().update(proof);
                liveData.postValue(SUCCESS);
                return;
            }
            Connection con = db.connectionDao().getByPwDid(proof.pwDid);
            Proofs.reject(con.serialized, proof.serialized).handle((s, err) -> {
                if (s != null) {
                    String serializedProof = Proofs.awaitStatusChange(s, MessageState.REJECTED);
                    proof.serialized = serializedProof;
                    proof.accepted = false;
                    db.proofRequestDao().update(proof);
                }
                liveData.postValue(err == null ? SUCCESS : FAILURE);
                return null;
            });
        });
    }

    public SingleLiveData<ProofCreateResult> getNewProofRequests() {
        SingleLiveData<ProofCreateResult> data = new SingleLiveData<>();
        checkProofRequests(data);
        return data;
    }

    private void checkProofRequests(SingleLiveData<ProofCreateResult> data) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Messages.getPendingMessages(MessageType.PROOF_REQUEST, null, null).handle((res, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
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

                                    Messages.updateMessageStatus(pwDid, message.getUid());
                                }
                                return null;
                            });
                        }
                    }
                }
                data.postValue(SUCCESS);
                return null;
            });
        });
    }
}
