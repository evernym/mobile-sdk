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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.Connections;
import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.Messages;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;

import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.FAILURE;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.REDIRECT;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.SUCCESS;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.REQUEST_ATTACH;

public class ConnectionsViewModel extends AndroidViewModel {
    private final Database db;
    private Boolean isOoB = null;
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

    private void createConnection(String invite, SingleLiveData<ConnectionCreateResult> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String parsedInvite = parseInvite(invite);

            try {
                isOoB = Connections.isOutOfBand(parsedInvite);
            } catch (Exception e) {
                e.printStackTrace();
            }

            requestsAttach = extractRequestAttach(parsedInvite);
            JSONObject offerAttach = convertToJSONObject(requestsAttach);

            ConnDataHolder data = extractDataFromInvite(parsedInvite);
            List<String> serializedConns = db.connectionDao().getAllSerializedConnections();
            Connections.verifyConnectionExists(parsedInvite, serializedConns)
                .handle((exists, err) -> {
                    if (err != null) {
                        err.printStackTrace();
                        liveData.postValue(FAILURE);
                    } else {
                        if (isOoB && offerAttach != null) {
                            try {
                                String type = offerAttach.getString("@type");
                                if (exists) {
                                    CompletableFuture<Void> result = new CompletableFuture<>();
                                    Messages.waitHandshakeReuse().whenComplete((handshake, e) -> {
                                        if (e != null) {
                                            e.printStackTrace();
                                        } else {
                                            if (handshake) {
                                                try {
                                                    String existingConnection = Connections.findExistingConnection(parsedInvite, serializedConns);
                                                    JSONObject connection = convertToJSONObject(existingConnection);
                                                    if (type.contains("credential")) {
                                                        System.out.println("existingConnectionExistingConnection" + connection);
                                                        createWithCredentialOffer(connection, liveData, offerAttach);
                                                    }
                                                } catch (Exception exception) {
                                                    exception.printStackTrace();
                                                }
                                                result.complete(null);
                                            }
                                            try {
                                                TimeUnit.SECONDS.sleep(1);
                                            } catch (InterruptedException interruptedException) {
                                                interruptedException.printStackTrace();
                                            }
                                        }
                                    });


                                    liveData.postValue(REDIRECT);
                                } else {
                                    if (type.matches("credential")) {
                                        connectionCreateWithCredentialOffer(parsedInvite, data, liveData, offerAttach);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (exists) {
                                liveData.postValue(REDIRECT);
                            } else {
                                connectionCreate(parsedInvite, data, liveData);
                            }
                        }
                    }
                    return null;
                });
        });
    }

    private void createWithCredentialOffer(
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

    private void connectionCreateWithCredentialOffer(
            String parsedInvite,
            ConnDataHolder data,
            SingleLiveData<ConnectionCreateResult> liveData,
            JSONObject offerAttach
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
                    liveData.postValue(throwable == null ? SUCCESS : FAILURE);

                    Credentials.createWithOffer(UUID.randomUUID().toString(), requestsAttach).handle((co, er) -> {
                        if (er != null) {
                            er.printStackTrace();
                        } else {
                            CredentialOffer offer = new CredentialOffer();
                            try {
                                offer.claimId = offerAttach.getString("@id");
                                offer.name = offerAttach.getString("comment");
                                offer.pwDid = pwDid;

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
                }
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                return res;
            });
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
            String param = uri.getQueryParameter("c_i");
            if (param != null) {
                return new String(Base64.decode(param, Base64.NO_WRAP));
            } else {
                return Connections.readDataFromUrl(invite);
            }
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

    private ConnDataHolder extractDataFromInvite(String invite) {
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
            return new JSONObject(init);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
