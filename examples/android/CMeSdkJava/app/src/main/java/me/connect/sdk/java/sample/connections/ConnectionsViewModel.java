package me.connect.sdk.java.sample.connections;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import me.connect.sdk.java.Connections;
import me.connect.sdk.java.ConnectionsUtils;
import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.OutOfBandHelper;
import me.connect.sdk.java.ProofRequests;
import me.connect.sdk.java.Proofs;
import me.connect.sdk.java.Utils;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;
import me.connect.sdk.java.sample.db.entity.ProofRequest;

import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.FAILURE;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.PROOF_ATTACH;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.REDIRECT;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.REQUEST_ATTACH;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.SUCCESS;


public class ConnectionsViewModel extends AndroidViewModel {
    private final Database db;
    private LiveData<List<Connection>> connections;

    public ConnectionsViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<List<Connection>> getConnections() {
        if (connections == null) {
            connections = db.connectionDao().getAll();
        }
        return connections;
    }

    public SingleLiveData<ConnectionCreateResult> newConnection(String invite) {
        SingleLiveData<ConnectionCreateResult> data = new SingleLiveData<>();
        createConnection(invite, data);
        return data;
    }

    private void createConnection(String invite, SingleLiveData<ConnectionCreateResult> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String parsedInvite = ConnectionsUtils.parseInvite(invite);
            Connections.InvitationType invitationType = Connections.getInvitationType(parsedInvite);
            ConnectionsUtils.ConnDataHolder userMeta = ConnectionsUtils.extractUserMetaFromInvite(parsedInvite);
            List<String> serializedConns = db.connectionDao().getAllSerializedConnections();
            String existingConnection = Connections.verifyConnectionExists(parsedInvite, serializedConns);
            if (ConnectionsUtils.isProprietaryType(invitationType)) {
                if (existingConnection != null) {
                    Connections.connectionRedirectProprietary(invite, existingConnection);
                    liveData.postValue(REDIRECT);
                } else {
                    connectionCreate(parsedInvite, userMeta, liveData);
                }
                return;
            }
            if (ConnectionsUtils.isAriesConnection(invitationType)) {
                if (existingConnection != null) {
                    liveData.postValue(REDIRECT);
                    return;
                } else {
                    connectionCreate(parsedInvite, userMeta, liveData);
                }
                return;
            }
            if (ConnectionsUtils.isOutOfBandType(invitationType)) {
                String extractedAttachRequest = OutOfBandHelper.extractRequestAttach(parsedInvite);
                JSONObject attachRequestObject = Utils.convertToJSONObject(extractedAttachRequest);
                if (attachRequestObject == null) {
                    if (existingConnection != null) {
                        Connections.connectionRedirectAriesOutOfBand(parsedInvite, existingConnection);
                        liveData.postValue(REDIRECT);
                    } else {
                        connectionCreate(parsedInvite, userMeta, liveData);
                    }
                    return;
                }
                processAttachment(
                    parsedInvite,
                    extractedAttachRequest,
                    attachRequestObject,
                    existingConnection,
                    userMeta,
                    liveData
                );
            }
        });
    }

    private void processAttachment(
            String parsedInvite,
            String extractedAttachRequest,
            JSONObject attachRequestObject,
            String existingConnection,
            ConnectionsUtils.ConnDataHolder userMeta,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        try {
            OutOfBandHelper.OutOfBandInvite outOfBandInvite = OutOfBandHelper.OutOfBandInvite.builder()
                .withParsedInvite(parsedInvite)
                .withExtractedAttachRequest(extractedAttachRequest)
                .withAttach(attachRequestObject)
                .withExistingConnection(existingConnection)
                .withUserMeta(userMeta)
                .build();

            String attachType = attachRequestObject.getString("@type");
            if (ConnectionsUtils.isCredentialInviteType(attachType)) {
                processCredentialAttachment(outOfBandInvite, liveData);
                return;
            }
            if (ConnectionsUtils.isProofInviteType(attachType)) {
                processProofAttachment(outOfBandInvite, liveData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processCredentialAttachment(
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            SingleLiveData<ConnectionCreateResult> liveData
            ) {
        if (outOfBandInvite.existingConnection != null) {
            Connections.connectionRedirectAriesOutOfBand(
                    outOfBandInvite.parsedInvite,
                    outOfBandInvite.existingConnection
            );
            createCredentialStateObjectForExistingConnection(db, outOfBandInvite, liveData);
            liveData.postValue(REDIRECT);
            return;
        }
        createCredentialStateObject(db, outOfBandInvite, liveData);
    }

    private void processProofAttachment(
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        if (outOfBandInvite.existingConnection != null) {
            createProofStateObjectForExistingConnection(db, outOfBandInvite, liveData);
            liveData.postValue(REDIRECT);
            return;
        }
        createProofStateObject(db, outOfBandInvite, liveData);
    }

    public static void createCredentialStateObjectForExistingConnection(
            Database db,
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        try {
            JSONObject connection = Utils.convertToJSONObject(outOfBandInvite.existingConnection);
            assert connection != null;
            JSONObject connectionData = connection.getJSONObject("data");
            Credentials.createWithOffer(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((co, er) -> {
                if (er != null) {
                    er.printStackTrace();
                } else {
                    CredentialOffer offer = new CredentialOffer();
                    try {

                        offer.claimId = outOfBandInvite.attach.getString("@id");
                        offer.name = outOfBandInvite.attach.getString("comment");
                        offer.pwDid = connectionData.getString("pw_did");
                        JSONObject preview = outOfBandInvite.attach.getJSONObject("credential_preview");
                        offer.attributes = preview.getJSONArray("attributes").getString(0);
                        offer.serialized = co;

                        offer.attachConnectionLogo = new JSONObject(outOfBandInvite.parsedInvite)
                                .getString("profileUrl");

                        offer.messageId = null;
                        db.credentialOffersDao().insertAll(offer);
                        liveData.postValue(REQUEST_ATTACH);
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

    public static void createCredentialStateObject(
            Database db,
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        Credentials.createWithOffer(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((co, er) -> {
            if (er != null) {
                er.printStackTrace();
            } else {
                CredentialOffer offer = new CredentialOffer();
                try {
                    offer.claimId = outOfBandInvite.attach.getString("@id");
                    offer.name = outOfBandInvite.attach.getString("comment");
                    offer.pwDid = null;

                    JSONObject preview = outOfBandInvite.attach.getJSONObject("credential_preview");
                    offer.attributes = preview.getJSONArray("attributes").getString(0);

                    offer.serialized = co;
                    offer.messageId = null;

                    offer.attachConnection = outOfBandInvite.parsedInvite;
                    offer.attachConnectionName = outOfBandInvite.userMeta.name;
                    offer.attachConnectionLogo = outOfBandInvite.userMeta.logo;

                    db.credentialOffersDao().insertAll(offer);

                    liveData.postValue(REQUEST_ATTACH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    public static void createProofStateObjectForExistingConnection(
            Database db,
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            SingleLiveData<ConnectionCreateResult> liveData
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
                        JSONObject decodedProofAttach = ProofRequests.decodeProofRequestAttach(outOfBandInvite.attach);

                        proof.serialized = pr;
                        proof.name = ProofRequests.extractRequestedNameFromProofRequest(decodedProofAttach);
                        proof.pwDid = connectionData.getString("pw_did");
                        proof.attributes = ProofRequests.extractRequestedAttributesFromProofRequest(decodedProofAttach);
                        JSONObject thread = outOfBandInvite.attach.getJSONObject("~thread");
                        proof.threadId = thread.getString("thid");

                        proof.attachConnectionLogo = new JSONObject(outOfBandInvite.parsedInvite)
                                .getString("profileUrl");

                        proof.messageId = null;
                        db.proofRequestDao().insertAll(proof);

                        liveData.postValue(PROOF_ATTACH);
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
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        Proofs.createWithRequest(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((pr, err) -> {
            if (err != null) {
                err.printStackTrace();
            } else {
                ProofRequest proof = new ProofRequest();
                try {
                    JSONObject decodedProofAttach = ProofRequests.decodeProofRequestAttach(outOfBandInvite.attach);

                    proof.serialized = pr;
                    proof.name = ProofRequests.extractRequestedNameFromProofRequest(decodedProofAttach);
                    proof.pwDid = null;
                    proof.attributes = ProofRequests.extractRequestedAttributesFromProofRequest(decodedProofAttach);
                    JSONObject thread = outOfBandInvite.attach.getJSONObject("~thread");
                    proof.threadId = thread.getString("thid");

                    proof.attachConnection = outOfBandInvite.parsedInvite;
                    proof.attachConnectionName = outOfBandInvite.userMeta.name;
                    proof.attachConnectionLogo = outOfBandInvite.userMeta.logo;

                    proof.messageId = null;
                    db.proofRequestDao().insertAll(proof);

                    liveData.postValue(PROOF_ATTACH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    public void connectionCreate(
            String parsedInvite,
            ConnectionsUtils.ConnDataHolder data,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        Connections.create(parsedInvite, new QRConnection())
            .handle((res, throwable) -> {
                if (res != null) {
                    String serializedCon = Connections.awaitStatusChange(res, MessageState.ACCEPTED);
                    String pwDid = Connections.getPwDid(serializedCon);
                    Connection c = new Connection();
                    c.name = data.name;
                    c.icon = data.logo;
                    c.pwDid = pwDid;
                    c.serialized = serializedCon;
                    db.connectionDao().insertAll(c);
                }
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                liveData.postValue(throwable == null ? SUCCESS : FAILURE);
                return res;
            });
    }
}
