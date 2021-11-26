package msdk.kotlin.sample.homepage

import msdk.kotlin.sample.db.ActionStatus
import msdk.kotlin.sample.db.entity.Action
import msdk.kotlin.sample.messages.CredentialOfferMessage
import msdk.kotlin.sample.messages.ProofRequestMessage
import msdk.kotlin.sample.messages.QuestionMessage
import org.json.JSONObject


object Actions {
    fun createActionWithConnectionInvitation(
        type: String?,
        name: String?,
        description: String?,
        icon: String?,
        invite: String?
    ): Action {
        val action = Action()
        action.type = type
        action.invite = invite
        action.name = name
        action.description = description
        action.icon = icon
        action.status = ActionStatus.PENDING.toString()
        return action
    }

    fun createActionWithOffer(
        type: String?,
        credentialOffer: CredentialOfferMessage,
        icon: String?,
        invite: String?
    ): Action? {
        val action = Action()
        action.type = type
        action.name = credentialOffer.name
        action.description = "To issue the credential"
        action.icon = icon
        action.details = buildCredentialOfferAttributesDetailsString(credentialOffer.attributes)
        action.threadId = credentialOffer.threadId
        action.invite = invite
        action.status = ActionStatus.PENDING.toString()
        return action
    }

    fun createActionWithProof(
        type: String?,
        proofRequest: ProofRequestMessage,
        icon: String?,
        invite: String?
    ): Action? {
        val action = Action()
        action.type = type
        action.name = proofRequest.name
        action.description = "Share the proof"
        action.icon = icon
        action.details = buildProofRequestDetailsString(proofRequest.attributes)
        action.threadId = proofRequest.threadId
        action.invite = invite
        action.status = ActionStatus.PENDING.toString()
        return action
    }

    fun createActionWithQuestion(
        type: String?,
        name: String?,
        details: String?,
        pwDid: String?,
        entryId: String?,
        messageAnswers: List<QuestionMessage.Response>?
    ): Action {
        val action = Action()
        action.invite = null
        action.type = type
        action.name = name
        action.description = "Answer the questions"
        action.details = details
        action.pwDid = pwDid
        action.entryId = entryId
        action.messageAnswers = messageAnswers
        action.status = ActionStatus.PENDING.toString()
        return action
    }

    fun buildCredentialOfferAttributesDetailsString(attributes: JSONObject): String {
        val attributesString = StringBuilder()
        val keys = attributes.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = attributes.optString(key)
            attributesString.append(String.format("%s: %s\n", key, value))
        }
        return attributesString.toString()
    }

    fun buildProofRequestDetailsString(requestedAttributes: JSONObject): String {
        val attributesString = StringBuilder()
        val keys = requestedAttributes.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val requestedAttribute = requestedAttributes.optJSONObject(key)
            attributesString.append(
                String.format(
                    "%s\n",
                    requestedAttribute.optString("name")
                )
            )
        }
        return attributesString.toString()
    }
}