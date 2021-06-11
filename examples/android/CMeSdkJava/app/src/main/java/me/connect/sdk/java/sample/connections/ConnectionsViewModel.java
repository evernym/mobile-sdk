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
            System.out.println(parsedInvite + "extractedAttachRequest");
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
            OutOfBandInvite outOfBandInvite = OutOfBandInvite.builder()
                .withParsedInvite(parsedInvite)
                .withExtractedAttachRequest(extractedAttachRequest)
                .withAttach(attachRequestObject)
                .withExistingConnection(existingConnection)
                .withUserMeta(userMeta)
                .withLiveData(liveData)
                .build();

            String attachType = attachRequestObject.getString("@type");
            if (Utils.isCredentialInviteType(attachType)) {
                processCredentialAttachment(outOfBandInvite);
                return;
            }
            if (Utils.isProofInviteType(attachType)) {
                processProofAttachment(outOfBandInvite);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processCredentialAttachment(OutOfBandInvite outOfBandInvite) {
        if (outOfBandInvite.existingConnection != null) {
            Connections.connectionRedirectAriesOutOfBand(
                    outOfBandInvite.parsedInvite,
                    outOfBandInvite.existingConnection
            );
            OutOfBandHelper.createCredentialStateObjectForExistingConnection(db, outOfBandInvite);
            outOfBandInvite.liveData.postValue(REDIRECT);
            return;
        }
        OutOfBandHelper.createCredentialStateObject(db, outOfBandInvite);
    }

    private void processProofAttachment(OutOfBandInvite outOfBandInvite) {
        if (outOfBandInvite.existingConnection != null) {
            OutOfBandHelper.createProofStateObjectForExistingConnection(db, outOfBandInvite);
            outOfBandInvite.liveData.postValue(REDIRECT);
            return;
        }
        OutOfBandHelper.createProofStateObject(db, outOfBandInvite);
    }

    public void connectionCreate(
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

    public static class OutOfBandInviteBuilder {
        private String parsedInvite;
        private String extractedAttachRequest;
        private JSONObject attach;
        private String existingConnection;
        private Utils.ConnDataHolder userMeta;
        private SingleLiveData<ConnectionCreateResult> liveData;

        private OutOfBandInviteBuilder() {
        }

        public @NonNull
        OutOfBandInviteBuilder withParsedInvite(@NonNull String parsedInvite) {
            this.parsedInvite = parsedInvite;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withExtractedAttachRequest(@NonNull String extractedAttachRequest) {
            this.extractedAttachRequest = extractedAttachRequest;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withAttach(@NonNull JSONObject attach) {
            this.attach = attach;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withUserMeta(Utils.ConnDataHolder userMeta) {
            this.userMeta = userMeta;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withLiveData(@NonNull SingleLiveData<ConnectionCreateResult> liveData) {
            this.liveData = liveData;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withExistingConnection(String existingConnection) {
            this.existingConnection = existingConnection;
            return this;
        }

        public @NonNull
        OutOfBandInvite build() {
            return new OutOfBandInvite(
                    parsedInvite,
                    extractedAttachRequest,
                    attach,
                    userMeta,
                    existingConnection,
                    liveData
            );
        }
    }

    public static class OutOfBandInvite {
        public String parsedInvite;
        public String extractedAttachRequest;
        public JSONObject attach;
        public Utils.ConnDataHolder userMeta;
        public String existingConnection;
        public SingleLiveData<ConnectionCreateResult> liveData;

        public OutOfBandInvite(
                String parsedInvite,
                String extractedAttachRequest,
                JSONObject attach,
                Utils.ConnDataHolder userMeta,
                String existingConnection,
                SingleLiveData<ConnectionCreateResult> liveData
        ) {
            this.parsedInvite = parsedInvite;
            this.extractedAttachRequest = extractedAttachRequest;
            this.attach = attach;
            this.existingConnection = existingConnection;
            this.userMeta = userMeta;
            this.liveData = liveData;
        }

        public static OutOfBandInviteBuilder builder() {
            return new OutOfBandInviteBuilder();
        }

    }
}
