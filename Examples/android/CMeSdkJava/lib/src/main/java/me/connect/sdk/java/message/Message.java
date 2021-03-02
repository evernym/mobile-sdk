package me.connect.sdk.java.message;

public class Message {
    private String uid;
    private String payload;
    private String type;
    private String status;

    public Message(String uid, String payload, String type, String status) {
        this.uid = uid;
        this.payload = payload;
        this.type = type;
        this.status = status;
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

    public String getStatus() {
        return status;
    }
}
