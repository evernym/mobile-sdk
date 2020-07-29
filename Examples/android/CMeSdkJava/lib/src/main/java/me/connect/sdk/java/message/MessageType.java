package me.connect.sdk.java.message;

public enum MessageType {
    CREDENTIAL_OFFER("credOffer", "CRED_OFFER"),
    PROOF_REQUEST("proofReq", "PROOF_REQUEST"),
    QUESTION("Question", "QUESTION");

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
