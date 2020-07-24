package me.connect.sdk.java.sample.connections;

import android.app.Application;
import android.net.Uri;
import android.util.Base64;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.List;
import java.util.concurrent.Executors;

import me.connect.sdk.java.ConnectMeVcxUpdated;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;

public class ConnectionsViewModel extends AndroidViewModel {
    private final Database db;
    private MutableLiveData<List<Connection>> connections;

    public ConnectionsViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<List<Connection>> getConnections() {
        if (connections == null) {
            connections = new MutableLiveData<>();
        }
        loadConnections();
        return connections;
    }

    public SingleLiveData<Boolean> newConnection(String invite) {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        createConnection(invite, data);
        return data;
    }

    private void loadConnections() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Connection> data = db.connectionDao().getAll();
            connections.postValue(data);
        });
    }

    private void createConnection(String invite, SingleLiveData<Boolean> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String parsedInvite = parseInvite(invite);
            ConnDataHolder data = extractDataFromInvite(parsedInvite);
            ConnectMeVcxUpdated.createConnection(parsedInvite, new QRConnection())
                    .handle((res, throwable) -> {
                        if (res != null) {
                            String serializedCon = ConnectMeVcxUpdated.awaitConnectionStatusChange(res, MessageState.ACCEPTED);
                            Connection c = new Connection();
                            c.name = data.name;
                            c.icon = data.logo;
                            c.serialized = serializedCon;
                            db.connectionDao().insertAll(c);
                            loadConnections();
                        }
                        liveData.postValue(throwable == null);
                        return res;
                    });
        });
    }

    private String parseInvite(String invite) {
        if (URLUtil.isValidUrl(invite)) {
            Uri uri = Uri.parse(invite);
            String param = uri.getQueryParameter("c_i");
            if (param != null) {
                return new String(Base64.decode(param, Base64.NO_WRAP));
            } else {
                return "";
            }
        } else {
            return invite;
        }
    }

    private ConnDataHolder extractDataFromInvite(String invite) {
        try {
            JSONObject json = new JSONObject(invite);
            if (json.has("label")) {
                String label = json.getString("label");
                return new ConnDataHolder(label, null);
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
}
