package me.connect.sdk.java.sample.connections;

import android.app.Application;

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
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;

public class ConnectionsViewModel extends AndroidViewModel {
    private final Database db;
    private MutableLiveData<List<Connection>> connections;
    private MutableLiveData<Boolean> newConnectionState;

    public ConnectionsViewModel(@NonNull Application application) {
        super(application);
        db = Database.newInstance(application);
    }

    public LiveData<List<Connection>> getConnections() {
        if (connections == null) {
            connections = new MutableLiveData<>();
        }
        loadConnections();
        return connections;
    }

    public LiveData<Boolean> newConnection(String invite) {
        if (newConnectionState == null) {
            newConnectionState = new MutableLiveData<>();
        }
        createConnection(invite);

        return newConnectionState;
    }

    private void loadConnections() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Connection> data = db.connectionDao().getAll();
            connections.postValue(data);
        });
    }

    private void createConnection(String invite) {
        Executors.newSingleThreadExecutor().execute(() -> {
            ConnectMeVcxUpdated.createConnection(invite, new QRConnection())
                    .handle((res, throwable) -> {
                        if (res != null) {
                            ConnDataHolder data = extractDataFromConnectionString(res);
                            Connection c = new Connection();
                            c.name = data.name;
                            c.icon = data.logo;
                            c.serializedConnection = res;
                            db.connectionDao().insertAll(c);
                            loadConnections();
                            newConnectionState.setValue(true);
                        } else {
                            newConnectionState.setValue(false);
                        }
                        return res;
                    });
        });
    }

    private ConnDataHolder extractDataFromConnectionString(String str) {
        try {
            JSONObject data = new JSONObject(str);
            JSONObject details = data.getJSONObject("data").getJSONObject("invite_detail").getJSONObject("senderDetail");
            return new ConnDataHolder(details.getString("name"), details.getString("logoUrl"));
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
