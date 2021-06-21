package me.connect.sdk.java.sample.homepage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.Executors;

import me.connect.sdk.java.Connections;
import me.connect.sdk.java.OutOfBandHelper;
import me.connect.sdk.java.Proofs;
import me.connect.sdk.java.Utils;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Action;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.ProofRequest;
import static me.connect.sdk.java.sample.homepage.Results.CONNECTION_FAILURE;
import static me.connect.sdk.java.sample.homepage.Results.CONNECTION_SUCCESS;
import static me.connect.sdk.java.sample.homepage.Results.PROOF_SUCCESS;
import static me.connect.sdk.java.sample.homepage.Results.PROOF_MISSED;
import static me.connect.sdk.java.sample.homepage.Results.PROOF_FAILURE;

public class StateProofRequests {
    public static void createProofStateObjectForExistingConnection(
            Database db,
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        try {
            JSONObject connection = Utils.convertToJSONObject(outOfBandInvite.existingConnection);
            assert connection != null;
            JSONObject connectionData = connection.getJSONObject("data");
            Proofs.createWithRequest(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((pr, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    ProofRequest proof = new ProofRequest();
                    try {
                        JSONObject decodedProofAttach = me.connect.sdk.java.ProofRequests.decodeProofRequestAttach(outOfBandInvite.attach);

                        proof.serialized = pr;
                        proof.name = me.connect.sdk.java.ProofRequests.extractRequestedNameFromProofRequest(decodedProofAttach);
                        proof.pwDid = connectionData.getString("pw_did");
                        proof.attributes = me.connect.sdk.java.ProofRequests.extractRequestedAttributesFromProofRequest(decodedProofAttach);
                        JSONObject thread = outOfBandInvite.attach.getJSONObject("~thread");
                        proof.threadId = thread.getString("thid");

                        proof.attachConnectionLogo = new JSONObject(outOfBandInvite.parsedInvite)
                                .getString("profileUrl");

                        proof.messageId = null;
                        db.proofRequestDao().insertAll(proof);

                        acceptProofReq(proof, db, liveData, action);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void createProofStateObject(
            Database db,
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        Proofs.createWithRequest(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((pr, err) -> {
            if (err != null) {
                err.printStackTrace();
            } else {
                ProofRequest proof = new ProofRequest();
                try {
                    JSONObject decodedProofAttach = me.connect.sdk.java.ProofRequests.decodeProofRequestAttach(outOfBandInvite.attach);

                    proof.serialized = pr;
                    proof.name = me.connect.sdk.java.ProofRequests.extractRequestedNameFromProofRequest(decodedProofAttach);
                    proof.pwDid = null;
                    proof.attributes = me.connect.sdk.java.ProofRequests.extractRequestedAttributesFromProofRequest(decodedProofAttach);
                    JSONObject thread = outOfBandInvite.attach.getJSONObject("~thread");
                    proof.threadId = thread.getString("thid");

                    proof.attachConnection = outOfBandInvite.parsedInvite;
                    proof.attachConnectionName = outOfBandInvite.userMeta.name;
                    proof.attachConnectionLogo = outOfBandInvite.userMeta.logo;

                    proof.messageId = null;
                    db.proofRequestDao().insertAll(proof);

                    acceptProofReq(proof, db, liveData, action);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    public static void acceptProofReq(
            ProofRequest proof,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (proof.attachConnection != null && proof.pwDid == null) {
                acceptProofReqAndCreateConnection(proof, db, liveData, action);
                return;
            }
            Connection con = db.connectionDao().getByPwDid(proof.pwDid);
            Proofs.retrieveAvailableCredentials(proof.serialized).handle((creds, err) -> {
                if (err != null) {
                    liveData.postValue(PROOF_MISSED);
                    return null;
                }
                // We automatically map first of each provided credentials to final structure
                // This process should be interactive in real app
                String data = Proofs.mapCredentials(creds);
                Proofs.send(con.serialized, proof.serialized, data, "{}").handle((s, e) -> {
                    if (s != null) {
                        proof.accepted = true;
                        proof.serialized = s;
                        db.proofRequestDao().update(proof);
                    }
                    HomePageViewModel.addToHistory(
                            action.id,
                            "Proofs send",
                            db,
                            liveData
                    );
                    liveData.postValue(e == null ? PROOF_SUCCESS: PROOF_FAILURE);
                    return null;
                });
                return null;
            });
        });
    }

    private static void acceptProofReqAndCreateConnection(
            ProofRequest proof,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        Connections.create(proof.attachConnection, new QRConnection())
                .handle((res, throwable) -> {
                    if (res != null) {
                        String serializedCon = Connections.awaitStatusChange(res);

                        String pwDid = Connections.getPwDid(serializedCon);
                        Connection c = new Connection();
                        c.name = proof.attachConnectionName;
                        c.icon = proof.attachConnectionLogo;
                        c.pwDid = pwDid;
                        c.serialized = serializedCon;
                        db.connectionDao().insertAll(c);
                        liveData.postValue(throwable == null ? CONNECTION_SUCCESS : CONNECTION_FAILURE);

                        proof.pwDid = pwDid;
                        db.proofRequestDao().update(proof);

                        HomePageViewModel.addHistoryAction(
                                db,
                                proof.attachConnectionName,
                                "Connection created",
                                proof.attachConnectionLogo,
                                liveData
                        );

                        Proofs.retrieveAvailableCredentials(proof.serialized).handle((creds, err) -> {
                            if (err != null) {
                                liveData.postValue(PROOF_MISSED);
                                return null;
                            }
                            // We automatically map first of each provided credentials to final structure
                            // This process should be interactive in real app
                            String data = Proofs.mapCredentials(creds);
                            Proofs.send(serializedCon, proof.serialized, data, "{}").handle((s, e) -> {
                                if (s != null) {
                                    proof.accepted = true;
                                    proof.serialized = s;
                                    db.proofRequestDao().update(proof);
                                }

                                HomePageViewModel.addToHistory(
                                        action.id,
                                        "Credential accept",
                                        db,
                                        liveData
                                );

                                liveData.postValue(e == null ? PROOF_SUCCESS: PROOF_FAILURE);
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

    public static void rejectProofReq(ProofRequest proof, Database db, SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (proof.pwDid == null) {
                proof.accepted = false;
                db.proofRequestDao().update(proof);
                liveData.postValue(PROOF_SUCCESS);
                return;
            }
            Connection con = db.connectionDao().getByPwDid(proof.pwDid);
            Proofs.reject(con.serialized, proof.serialized).handle((s, err) -> {
                if (s != null) {
                    proof.serialized = s;
                    proof.accepted = false;
                    db.proofRequestDao().update(proof);
                }
                liveData.postValue(err == null ? PROOF_SUCCESS : PROOF_FAILURE);
                return null;
            });
        });
    }
}
