package me.connect.sdk.java.message;

public enum MessageType {
    PROOF_REQUEST("proofReq", "PROOF_REQUEST"),
    QUESTION("question", "QUESTION");

    private final String proprietary;
    private final String aries;

    MessageType(String proprietary, String aries) {
        this.proprietary = proprietary;
        this.aries = aries;
    }

    public String getProprietary() {
        return proprietary;
    }

    public String getAries() {
        return aries;
    }
}

