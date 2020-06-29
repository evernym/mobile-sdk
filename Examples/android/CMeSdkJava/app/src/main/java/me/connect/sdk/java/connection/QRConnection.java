package me.connect.sdk.java.connection;

public class QRConnection implements Connection {
    public QRConnection() {
    }

    @Override
    public String getConnectionType() {
        return "{\"connection_type\":\"QR\",\"phone\":\"\"}";
    }
}
