package me.connect.sdk.java.message;

public class Message {
    private String uid;
    private String payload;

    public Message(String uid, String payload) {
        this.uid = uid;
        this.payload = payload;
    }

    public String getUid() {
        return uid;
    }

    public String getPayload() {
        return payload;
    }
}
