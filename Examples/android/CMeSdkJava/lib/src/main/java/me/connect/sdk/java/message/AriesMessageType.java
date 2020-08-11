package me.connect.sdk.java.message;

public interface AriesMessageType {
    // connections
    String CONNECTION_INVITATION = "connections/1.0/invitation";
    String CONNECTION_REQUEST = "connections/1.0/request";
    String CONNECTION_RESPONSE = "connections/1.0/response";
    String CONNECTION_ACK = "connections/1.0/ack";
    String CONNECTION_PROBLEM_REPORT = "connections/1.0/problem_report";
    // trust-ping
    String PING = "trust_ping/1.0/ping";
    String PING_RESPONSE = "trust_ping/1.0/ping_response";
    // notifications
    String PROBLEM_REPORT = "notification/1.0/problem-report";
    String ACK = "notification/1.0/ack";
    // issue-credential
    String OFFER_CREDENTIAL = "issue-credential/1.0/offer-credential";
    String PROPOSE_CREDENTIAL = "issue-credential/1.1/propose-credential";
    String REQUEST_CREDENTIAL = "issue-credential/1.0/request-credential";
    String ISSUE_CREDENTIAL = "issue-credential/1.0/issue-credential";
    String CREDENTIAL_ACK = "issue-credential/1.0/ack";
    String CREDENTIAL_REJECT = "issue-credential/1.0/problem-report";
    // present-proof
    String PROPOSE_PRESENTATION = "present-proof/1.0/propose-presentation";
    String REQUEST_PRESENTATION = "present-proof/1.0/request-presentation";
    String PRESENTATION = "present-proof/1.0/presentation";
    String PRESENTATION_ACK = "present-proof/1.0/ack";
    String PRESENTATION_REJECT = "present-proof/1.0/problem-report";
    // features discover
    String QUERY = "discover-features/1.0/query";
    String DISCLOSED = "discover-features/1.0/disclose";
    // basic message
    String BASIC_MESSAGE = "basicmessage/1.0/message";
    // questionanswer
    String QUESTION = "questionanswer/1.0/question";
    String ANSWER = "questionanswer/1.0/answer";
    // commitedanswer
    String COMMITTED_QUESTION = "committedanswer/1.0/question";
    String COMMITTED_ANSWER = "committedanswer/1.0/answer";
    // out-of-band
    String OUTOFBAND_INVITATION = "out-of-band/1.0/invitation";
    String OUTOFBAND_HANDSHAKE_REUSE = "out-of-band/1.0/handshake-reuse";
    String OUTOFBAND_HANDSHAKE_REUSE_ACCEPTED = "out-of-band/1.0/handshake-reuse-accepted";
}

