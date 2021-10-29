package msdk.kotlin.sample.types

interface AriesMessageType {
    companion object {
        // connections
        const val CONNECTION_INVITATION = "connections/1.0/invitation"
        const val CONNECTION_REQUEST = "connections/1.0/request"
        const val CONNECTION_RESPONSE = "connections/1.0/response"
        const val CONNECTION_ACK = "connections/1.0/ack"
        const val CONNECTION_PROBLEM_REPORT = "connections/1.0/problem_report"

        // trust-ping
        const val PING = "trust_ping/1.0/ping"
        const val PING_RESPONSE = "trust_ping/1.0/ping_response"

        // notifications
        const val PROBLEM_REPORT = "notification/1.0/problem-report"
        const val ACK = "notification/1.0/ack"

        // issue-credential
        const val OFFER_CREDENTIAL = "issue-credential/1.0/offer-credential"
        const val PREVIEW_CREDENTIAL = "issue-credential/1.0/credential-preview"
        const val PROPOSE_CREDENTIAL = "issue-credential/1.1/propose-credential"
        const val REQUEST_CREDENTIAL = "issue-credential/1.0/request-credential"
        const val ISSUE_CREDENTIAL = "issue-credential/1.0/issue-credential"
        const val CREDENTIAL_ACK = "issue-credential/1.0/ack"
        const val CREDENTIAL_REJECT = "issue-credential/1.0/problem-report"

        // present-proof
        const val PROPOSE_PRESENTATION = "present-proof/1.0/propose-presentation"
        const val REQUEST_PRESENTATION = "present-proof/1.0/request-presentation"
        const val PRESENTATION = "present-proof/1.0/presentation"
        const val PRESENTATION_ACK = "present-proof/1.0/ack"
        const val PRESENTATION_REJECT = "present-proof/1.0/problem-report"

        // features discover
        const val QUERY = "discover-features/1.0/query"
        const val DISCLOSED = "discover-features/1.0/disclose"

        // basic message
        const val BASIC_MESSAGE = "basicmessage/1.0/message"

        // questionanswer
        const val QUESTION = "questionanswer/1.0/question"
        const val ANSWER = "questionanswer/1.0/answer"

        // commitedanswer
        const val COMMITTED_QUESTION = "committedanswer/1.0/question"
        const val COMMITTED_ANSWER = "committedanswer/1.0/answer"

        // out-of-band
        const val OUTOFBAND_INVITATION = "out-of-band/1.0/invitation"
        const val OUTOFBAND_HANDSHAKE_REUSE = "out-of-band/1.0/handshake-reuse"
        const val OUTOFBAND_HANDSHAKE_REUSE_ACCEPTED =
            "out-of-band/1.0/handshake-reuse-accepted"
    }
}