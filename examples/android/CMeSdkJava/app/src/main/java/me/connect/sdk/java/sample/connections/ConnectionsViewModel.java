package me.connect.sdk.java.sample.connections;

import android.app.Application;
import android.net.Uri;
import android.util.Base64;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.Connections;
import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.Messages;
import me.connect.sdk.java.Proofs;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;
import me.connect.sdk.java.sample.db.entity.ProofRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.FAILURE;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.REDIRECT;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.SUCCESS;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.REQUEST_ATTACH;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.PROOF_ATTACH;


public class ConnectionsViewModel extends AndroidViewModel {
    private final Database db;
    private String requestsAttach = null;
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

    private boolean isProprietaryType(Connections.InvitationType type) {
        return type == Connections.InvitationType.Proprietary;
    }

    private boolean isAriesConnection(Connections.InvitationType type) {
        return type == Connections.InvitationType.Connection;
    }

    private boolean isOutOfBandType(Connections.InvitationType type) {
        return type == Connections.InvitationType.OutOfBand;
    }

    private boolean isCredentialInviteType(String type) {
        return type.contains("issue-credential");
    }

    private boolean isProofInviteType(String type) {
        return type.contains("present-proof");
    }

    private void createConnection(String invite, SingleLiveData<ConnectionCreateResult> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String parsedInvite = parseInvite(invite);
            Connections.InvitationType invitationType = Connections.getInvitationType(parsedInvite);
            ConnDataHolder data = extractUserMetaFromInvite(parsedInvite);
            List<String> serializedConns = db.connectionDao().getAllSerializedConnections();
            String existingConnection = Connections.verifyConnectionExists(parsedInvite, serializedConns);

            if (isProprietaryType(invitationType)) {
                if (existingConnection != null) {
                    Connections.connectionRedirectProprietary(invite, existingConnection);
                    liveData.postValue(REDIRECT);
                } else {
                    connectionCreate(parsedInvite, data, liveData);
                }
                return;
            }

            if (isAriesConnection(invitationType)) {
                if (existingConnection != null) {
                    Connections.connectionRedirectAries(invite, existingConnection);
                    liveData.postValue(REDIRECT);
                    return;
                } else {
                    connectionCreate(parsedInvite, data, liveData);
                }
                return;
            }

            if (isOutOfBandType(invitationType)) {
                try {
                    requestsAttach = extractRequestAttach(parsedInvite);
                    JSONObject offerAttach = convertToJSONObject(requestsAttach);

                    if (offerAttach == null) {
                        if (existingConnection != null) {
                            Connections.connectionRedirectAriesOutOfBand(parsedInvite, existingConnection);
                            liveData.postValue(REDIRECT);
                        } else {
                            connectionCreate(parsedInvite, data, liveData);
                        }
                        return;
                    }
                    String attachType = offerAttach.getString("@type");

                    if (existingConnection != null) {
                        Connections.connectionRedirectAriesOutOfBand(parsedInvite, existingConnection);
                        processAttachement(parsedInvite, serializedConns, liveData, offerAttach, attachType);
                        liveData.postValue(REDIRECT);
                        return;
                    }
                    if (isCredentialInviteType(attachType)) {
                        createCredentialStateObject(parsedInvite, data, liveData, offerAttach);
                        return;
                    }
                    if (isProofInviteType(attachType)) {
                        createProofStateObject(parsedInvite, data, liveData, offerAttach);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createCredentialStateObjectForExistingConnection(
        JSONObject existingConnection,
        SingleLiveData<ConnectionCreateResult> liveData,
        JSONObject offerAttach
    ) {
        try {
            JSONObject data = existingConnection.getJSONObject("data");
            Credentials.createWithOffer(UUID.randomUUID().toString(), requestsAttach).handle((co, er) -> {
                if (er != null) {
                    er.printStackTrace();
                } else {
                    CredentialOffer offer = new CredentialOffer();
                    try {
                        offer.claimId = offerAttach.getString("@id");
                        offer.name = offerAttach.getString("comment");
                        offer.pwDid = data.getString("pw_did");
                        JSONObject preview = offerAttach.getJSONObject("credential_preview");
                        offer.attributes = preview.getString("attributes");
                        offer.serialized = co;
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

    private void createCredentialStateObject(
            String parsedInvite,
            ConnDataHolder data,
            SingleLiveData<ConnectionCreateResult> liveData,
            JSONObject offerAttach
    ) {
        Credentials.createWithOffer(UUID.randomUUID().toString(), requestsAttach).handle((co, er) -> {
            if (er != null) {
                er.printStackTrace();
            } else {
                CredentialOffer offer = new CredentialOffer();
                try {
                    offer.claimId = offerAttach.getString("@id");
                    offer.name = offerAttach.getString("comment");
                    offer.pwDid = null;

                    JSONObject preview = offerAttach.getJSONObject("credential_preview");
                    offer.attributes = preview.getString("attributes");

                    offer.serialized = co;
                    offer.messageId = null;

                    offer.attachConnection = parsedInvite;
                    offer.attachConnectionName = data.name;
                    offer.attachConnectionLogo = data.logo;

                    db.credentialOffersDao().insertAll(offer);

                    liveData.postValue(REQUEST_ATTACH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });

    }

    private void processAttachement(
            String parsedInvite,
            List<String> serializedConns,
            SingleLiveData<ConnectionCreateResult> liveData,
            JSONObject offerAttach,
            String attachType
    ) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Messages.waitHandshakeReuse().whenComplete((handshake, e) -> {
            if (e != null) {
                e.printStackTrace();
            } else {
                if (handshake) {
                    try {
                        String existingConnection = Connections.findExistingConnection(parsedInvite, serializedConns);
                        JSONObject connection = convertToJSONObject(existingConnection);
                        if (isCredentialInviteType(attachType)) {
                            createCredentialStateObjectForExistingConnection(connection, liveData, offerAttach);
                        }
                        if (isProofInviteType(attachType)) {
                            createProofStateObjectForExistingConnection(connection, liveData, offerAttach);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    result.complete(null);
                }
            }
        });
    }

    private void createProofStateObject(
        String parsedInvite,
        ConnDataHolder data,
        SingleLiveData<ConnectionCreateResult> liveData,
        JSONObject proofAttach
    ) {
        Proofs.createWithRequest(UUID.randomUUID().toString(), requestsAttach).handle((pr, err) -> {
            if (err != null) {
                err.printStackTrace();
            } else {
                    ProofRequest proof = new ProofRequest();
                    try {
                    proof.serialized = pr;
                    proof.name = proofAttach.getString("comment");
                    proof.pwDid = null;
                    proof.attributes = extractAttachmentFromProofAttach(proofAttach);
                    JSONObject thread = proofAttach.getJSONObject("~thread");
                    proof.threadId = thread.getString("thid");

                    proof.attachConnection = parsedInvite;
                    proof.attachConnectionName = data.name;
                    proof.attachConnectionLogo = data.logo;

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

    private void createProofStateObjectForExistingConnection(
            JSONObject existingConnection,
            SingleLiveData<ConnectionCreateResult> liveData,
            JSONObject proofAttach
    ) {
        try {
            JSONObject data = existingConnection.getJSONObject("data");
            Proofs.createWithRequest(UUID.randomUUID().toString(), requestsAttach).handle((pr, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    ProofRequest proof = new ProofRequest();
                    try {
                        proof.serialized = pr;
                        proof.name = proofAttach.getString("comment");
                        proof.pwDid = data.getString("pw_did");
                        proof.attributes = extractAttachmentFromProofAttach(proofAttach);
                        JSONObject thread = proofAttach.getJSONObject("~thread");
                        proof.threadId = thread.getString("thid");

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

    private void connectionCreate(
            String parsedInvite,
            ConnDataHolder data,
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

    private String parseInvite(String invite) {
        if (URLUtil.isValidUrl(invite)) {
            Uri uri = Uri.parse(invite);
            String ariesConnection = uri.getQueryParameter("c_i");
            String ariesOutOfBand = uri.getQueryParameter("oob");
            if (ariesConnection != null) {
                return new String(Base64.decode(ariesConnection, Base64.NO_WRAP));
            }
            if (ariesOutOfBand != null) {
                return new String(Base64.decode(ariesOutOfBand, Base64.NO_WRAP));
            }
            return readDataFromUrl(invite);
        } else {
            return invite;
        }
    }

    private String extractRequestAttach(String invite) {
        try {
            JSONObject json = convertToJSONObject(invite);
            if (json != null && json.has("request~attach")) {
                String requestAttachCode = json.getString("request~attach");
                JSONArray requestsAttachItems = new JSONArray(requestAttachCode);
                if (requestsAttachItems.length() == 0) {
                    return null;
                }
                JSONObject requestsAttachItem = requestsAttachItems.getJSONObject(0);
                JSONObject requestsAttachItemData = requestsAttachItem.getJSONObject("data");
                String requestsAttachItemBase = requestsAttachItemData.getString("base64");
                String requestAttachDecode = new String(Base64.decode(requestsAttachItemBase, Base64.NO_WRAP));
                JSONObject result = convertToJSONObject(requestAttachDecode);
                if (result != null) {
                    result.put("@id", requestsAttachItem.getString("@id"));
                    return result.toString();
                }
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractAttachmentFromProofAttach(JSONObject proofAttach) {
        try {
            if (proofAttach != null && proofAttach.has("request_presentations~attach")) {
                String requestAttachCode = proofAttach.getString("request_presentations~attach");
                JSONArray requestsAttachItems = new JSONArray(requestAttachCode);
                if (requestsAttachItems.length() == 0) {
                    return null;
                }
                JSONObject requestsAttachItem = requestsAttachItems.getJSONObject(0);
                JSONObject requestsAttachItemData = requestsAttachItem.getJSONObject("data");
                String requestsAttachItemBase = requestsAttachItemData.getString("base64");
                String requestAttachDecode = new String(Base64.decode(requestsAttachItemBase, Base64.NO_WRAP));
                JSONObject result = convertToJSONObject(requestAttachDecode);
                if (result != null) {
                    return result.getJSONObject("requested_attributes").toString();
                }
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ConnDataHolder extractUserMetaFromInvite(String invite) {
        try {
            JSONObject json = convertToJSONObject(invite);
            if (json != null && json.has("label")) {
                String label = json.getString("label");
                String logo = null;
                if (json.has("profileUrl")) {
                    logo = json.getString("profileUrl");
                }
                return new ConnDataHolder(label, logo);
            }
            JSONObject data = json.optJSONObject("s");
            if (data != null) {
                return new ConnDataHolder(data.getString("n"), data.getString("l"));
            } else {
                // workaround in case details missing
                String sourceId = json.getString("id");
                return new ConnDataHolder(sourceId, null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class ConnDataHolder {
        String name;
        String logo;

        public ConnDataHolder(String name, String logo) {
            this.name = name;
            this.logo = logo;
        }
    }

    private static JSONObject convertToJSONObject(String init) {
        try {
            if (init == null) {
                return null;
            }
            return new JSONObject(init);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readDataFromUrl(String url) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
