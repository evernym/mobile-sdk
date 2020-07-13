package me.connect.sdk.java.sample.connections;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

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
        connections.setValue(loadConnections());
        return connections;
    }

    public LiveData<Boolean> newConnection(String invite) {
        if (newConnectionState == null) {
            newConnectionState = new MutableLiveData<>();
        }
        ConnectMeVcxUpdated.createConnection(invite, new QRConnection())
                .handle((res, throwable) -> {
                    if (res != null) {
                        Connection c = new Connection();
                        c.name = "test12";
                        c.serializedConnection = res;
                        db.connectionDao().insertAll(c);
                        connections.setValue(loadConnections());
                        newConnectionState.setValue(true);
                    } else {
                        newConnectionState.setValue(false);
                    }
                    return res;
                });
        return newConnectionState;
    }

    private List<Connection> loadConnections() {
        return db.connectionDao().getAll();
    }


}
