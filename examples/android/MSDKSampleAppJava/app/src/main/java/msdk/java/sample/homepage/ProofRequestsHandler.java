package msdk.java.sample.homepage;

import java.util.UUID;
import java.util.concurrent.Executors;

import msdk.java.messages.ConnectionInvitation;
import msdk.java.handlers.Connections;
import msdk.java.messages.OutOfBandInvitation;
import msdk.java.handlers.Proofs;
import msdk.java.messages.ProofRequestMessage;
import msdk.java.sample.SingleLiveData;
import msdk.java.sample.db.Database;
import msdk.java.sample.db.entity.Action;
import msdk.java.sample.db.entity.Connection;
import msdk.java.sample.db.entity.ProofRequest;
import msdk.java.sample.history.HistoryHandler;

import static msdk.java.sample.homepage.Results.CONNECTION_FAILURE;
import static msdk.java.sample.homepage.Results.CONNECTION_SUCCESS;
import static msdk.java.sample.homepage.Results.PROOF_SUCCESS;
import static msdk.java.sample.homepage.Results.PROOF_MISSED;
import static msdk.java.sample.homepage.Results.PROOF_FAILURE;

public class ProofRequestsHandler {
    public static void createProofStateObject(
            Database db,
            OutOfBandInvitation outOfBandInvite,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        ProofRequestMessage proofRequest = ProofRequestMessage.parse(outOfBandInvite.attachment.toString());

        String pwDid = null;
        if (outOfBandInvite.existingConnection != null) {
            pwDid = Connections.getPwDid(outOfBandInvite.existingConnection);
        }
        String finalPwDid = pwDid;

        Proofs.createWithRequest(UUID.randomUUID().toString(), outOfBandInvite.attachment.toString()).handle((serialized, err) -> {
            if (err != null) {
                err.printStackTrace();
            } else {
                ProofRequest proof = new ProofRequest();
                proof.serialized = serialized;
                proof.pwDid = finalPwDid;
                proof.threadId = proofRequest.threadId;
                proof.attachConnection = outOfBandInvite.invitation;
                proof.attachConnectionLogo = outOfBandInvite.userMeta.logo;
                proof.attachConnectionName = outOfBandInvite.userMeta.name;
                db.proofRequestDao().insertAll(proof);

                acceptProofRequest(proof, db, liveData, action);
            }
            return null;
        });
    }

    public static void acceptProofRequest(
            ProofRequest proof,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (proof.pwDid == null) {
                acceptProofReqAndCreateConnection(proof, db, liveData, action);
            } else {
                Connection connection = db.connectionDao().getByPwDid(proof.pwDid);
                acceptProofRequest(proof, connection, db, liveData, action);
            }
        });
    }

    public static void acceptProofRequest(
            ProofRequest proof,
            Connection connection,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        Proofs.retrieveAvailableCredentials(proof.serialized).handle((creds, err) -> {
            if (err != null) {
                liveData.postValue(PROOF_MISSED);
                return null;
            }
            // We automatically map first of each provided credentials to final structure
            // This process should be interactive in real app
            String data = Proofs.mapCredentials(creds);
            Proofs.send(connection.serialized, proof.serialized, data, "{}").handle((s, e) -> {
                if (s != null) {
                    proof.serialized = s;
                    db.proofRequestDao().update(proof);
                }
                HistoryHandler.addToHistory(
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
    }

    private static void acceptProofReqAndCreateConnection(
            ProofRequest proof,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        Connections.create(proof.attachConnection, ConnectionInvitation.InvitationType.OutOfBand)
                .handle((res, throwable) -> {
                    if (res != null) {
                        String pwDid = Connections.getPwDid(res);
                        String serializedCon = Connections.awaitConnectionCompleted(res, pwDid);

                        Connection connection = new Connection();
                        connection.name = proof.attachConnectionName;
                        connection.icon = proof.attachConnectionLogo;
                        connection.pwDid = pwDid;
                        connection.serialized = serializedCon;
                        db.connectionDao().insertAll(connection);
                        liveData.postValue(throwable == null ? CONNECTION_SUCCESS : CONNECTION_FAILURE);

                        proof.pwDid = pwDid;
                        db.proofRequestDao().update(proof);

                        HistoryHandler.addHistoryAction(
                                db,
                                proof.attachConnectionName,
                                "Connection created",
                                proof.attachConnectionLogo,
                                liveData
                        );

                        acceptProofRequest(proof, connection, db, liveData, action);
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
                liveData.postValue(PROOF_SUCCESS);
                return;
            }
            Connection con = db.connectionDao().getByPwDid(proof.pwDid);
            Proofs.reject(con.serialized, proof.serialized).handle((s, err) -> {
                if (s != null) {
                    proof.serialized = s;
                    db.proofRequestDao().update(proof);
                }
                liveData.postValue(err == null ? PROOF_SUCCESS : PROOF_FAILURE);
                return null;
            });
        });
    }
}
