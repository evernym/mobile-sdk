package msdk.java.messages;

public class Message {
    private String uid;
    private String payload;
    private String type;
    private String status;
    private String pairwiseDID;

    public Message(String pairwiseDID, String uid, String payload, String type, String status) {
        this.uid = uid;
        this.payload = payload;
        this.type = type;
        this.status = status;
        this.pairwiseDID = pairwiseDID;
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

    public String getPwDid() {
        return pairwiseDID;
    }
}
