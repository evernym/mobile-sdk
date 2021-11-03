package msdk.kotlin.sample.types

import msdk.kotlin.sample.logger.Logger.Companion.instance
import java.util.*

enum class MessageType {
    CONNECTION_INVITATION, CREDENTIAL, CONNECTION_RESPONSE, ACK, HANDSHAKE, CREDENTIAL_OFFER, PROOF_REQUEST, QUESTION;

    fun matches(type: String): Boolean {
        val listToCheck: List<String>
        listToCheck = when (this) {
            QUESTION -> QUESTION_VALUES
            PROOF_REQUEST -> PROOF_REQUEST_VALUES
            CREDENTIAL_OFFER -> CREDENTIAL_OFFER_VALUES
            else -> {
                instance.w("Message type " + this + "matching was not implemented")
                return false
            }
        }
        return listToCheck.contains(type)
    }

    fun matchesValue(type: String): Boolean {
        val valueCheck: String
        valueCheck = when (this) {
            CREDENTIAL -> CREDENTIAL_VALUES
            CONNECTION_RESPONSE -> CONNECTION_RESPONSE_VALUES
            ACK -> ACK_VALUES
            HANDSHAKE -> HANDSHAKE_VALUES
            else -> {
                instance.w("Message type " + this + "matching was not implemented")
                return false
            }
        }
        return type.contains(valueCheck)
    }

    companion object {
        private val CREDENTIAL_OFFER_VALUES =
            Arrays.asList("credOffer", "CRED_OFFER", "credential-offer")
        private val PROOF_REQUEST_VALUES =
            Arrays.asList(
                "proofReq",
                "PROOF_REQUEST",
                "presentation-request"
            )
        private val QUESTION_VALUES =
            Arrays.asList(
                "Question",
                "QUESTION",
                "committed-question",
                "question"
            )
        private const val CREDENTIAL_VALUES = "issue-credential/1.0/issue-credential"
        private const val CONNECTION_RESPONSE_VALUES = "connections/1.0/response"
        private const val ACK_VALUES = "ack"
        private const val HANDSHAKE_VALUES = "handshake-reuse-accepted"
    }
}