package me.connect.sdk.java.message;

public class Message {
    private String uid;
    private String payload;
    private String type;

    public Message(String uid, String payload, String type) {
        this.uid = uid;
        this.payload = payload;
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public String getPayload() {
        return payload;
    }

    public String getType() {
        return type;
    }
}
