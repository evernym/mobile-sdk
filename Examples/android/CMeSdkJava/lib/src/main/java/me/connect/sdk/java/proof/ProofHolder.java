package me.connect.sdk.java.proof;

public class ProofHolder {
    private String serializedProof;
    private String retrievedCredentials;

    public String getSerializedProof() {
        return serializedProof;
    }

    public void setSerializedProof(String serializedProof) {
        this.serializedProof = serializedProof;
    }

    public String getRetrievedCredentials() {
        return retrievedCredentials;
    }

    public void setRetrievedCredentials(String retrievedCredentials) {
        this.retrievedCredentials = retrievedCredentials;
    }
}
