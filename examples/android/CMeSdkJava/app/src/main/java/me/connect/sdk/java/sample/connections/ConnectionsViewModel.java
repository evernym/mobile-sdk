package me.connect.sdk.java.sample.connections;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executors;

import me.connect.sdk.java.Connections;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;

import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.FAILURE;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.REDIRECT;
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
            String parsedInvite = Utils.parseInvite(invite);
            Connections.InvitationType invitationType = Connections.getInvitationType(parsedInvite);
            Utils.ConnDataHolder userMeta = Utils.extractUserMetaFromInvite(parsedInvite);
            List<String> serializedConns = db.connectionDao().getAllSerializedConnections();
            String existingConnection = Connections.verifyConnectionExists(parsedInvite, serializedConns);
            if (Utils.isProprietaryType(invitationType)) {
                if (existingConnection != null) {
                    Connections.connectionRedirectProprietary(invite, existingConnection);
                    liveData.postValue(REDIRECT);
                } else {
                    connectionCreate(parsedInvite, userMeta, liveData);
                }
                return;
            }
            if (Utils.isAriesConnection(invitationType)) {
                if (existingConnection != null) {
                    liveData.postValue(REDIRECT);
                    return;
                } else {
                    connectionCreate(parsedInvite, userMeta, liveData);
                }
                return;
            }
            if (Utils.isOutOfBandType(invitationType)) {
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
            Utils.ConnDataHolder userMeta,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        try {
            String attachType = attachRequestObject.getString("@type");
            if (Utils.isCredentialInviteType(attachType)) {
                processCredentialAttachment(
                    parsedInvite,
                    extractedAttachRequest,
                    attachRequestObject,
                    existingConnection,
                    userMeta,
                    liveData
                );
                return;
            }
            if (Utils.isProofInviteType(attachType)) {
                processProofAttachment(
                    parsedInvite,
                    extractedAttachRequest,
                    attachRequestObject,
                    existingConnection,
                    userMeta,
                    liveData
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processCredentialAttachment(
            String parsedInvite,
            String extractedAttachRequest,
            JSONObject offerAttach,
            String existingConnection,
            Utils.ConnDataHolder userMeta,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        if (existingConnection != null) {
            Connections.connectionRedirectAriesOutOfBand(parsedInvite, existingConnection);
            OutOfBandHelper.createCredentialStateObjectForExistingConnection(
                    db,
                    extractedAttachRequest,
                    offerAttach,
                    existingConnection,
                    liveData
            );
            liveData.postValue(REDIRECT);
            return;
        }
        OutOfBandHelper.createCredentialStateObject(
                db,
                parsedInvite,
                extractedAttachRequest,
                offerAttach,
                userMeta,
                liveData
        );
    }

    private void processProofAttachment(
            String parsedInvite,
            String extractedAttachRequest,
            JSONObject proofAttach,
            String existingConnection,
            Utils.ConnDataHolder userMeta,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        if (existingConnection != null) {
            OutOfBandHelper.createProofStateObjectForExistingConnection(
                    db,
                    extractedAttachRequest,
                    proofAttach,
                    existingConnection,
                    liveData
            );
            liveData.postValue(REDIRECT);
            return;
        }
        OutOfBandHelper.createProofStateObject(
                db,
                parsedInvite,
                extractedAttachRequest,
                proofAttach,
                userMeta,
                liveData);
    }

    private void connectionCreate(
            String parsedInvite,
            Utils.ConnDataHolder data,
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
