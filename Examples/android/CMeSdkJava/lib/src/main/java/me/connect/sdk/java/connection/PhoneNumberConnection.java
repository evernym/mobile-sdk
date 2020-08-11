package me.connect.sdk.java.connection;

public class PhoneNumberConnection implements Connection {
    private String phoneNumber;

    public PhoneNumberConnection(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getConnectionType() {
        return String.format("{\"connection_type\":\"PHONE\",\"phone\":\"%s\"}", phoneNumber);
    }
}
