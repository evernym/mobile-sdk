package msdk.kotlin.sample.homepage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import msdk.kotlin.sample.handlers.Credentials
import msdk.kotlin.sample.handlers.Messages
import msdk.kotlin.sample.handlers.Proofs
import msdk.kotlin.sample.handlers.StructuredMessages
import msdk.kotlin.sample.messages.Message
import msdk.kotlin.sample.types.MessageAttachment
import msdk.kotlin.sample.types.MessageType
import msdk.kotlin.sample.SingleLiveData
import msdk.kotlin.sample.db.ActionStatus.HISTORIZED
import msdk.kotlin.sample.db.ActionStatus.PENDING
import msdk.kotlin.sample.db.Database
import msdk.kotlin.sample.db.entity.*
import msdk.kotlin.sample.homepage.Results.*
import msdk.kotlin.sample.messages.ConnectionInvitation.getConnectionInvitationFromData
import msdk.kotlin.sample.messages.ConnectionInvitation.getInvitationType
import msdk.kotlin.sample.messages.ConnectionInvitation.isAriesOutOfBandConnectionInvitation
import msdk.kotlin.sample.messages.CredentialOfferMessage
import msdk.kotlin.sample.messages.ProofRequestMessage
import msdk.kotlin.sample.messages.ProofRequestMessage.Companion.decodeProofRequestAttach
import msdk.kotlin.sample.messages.ProofRequestMessage.Companion.extractRequestedAttributesFromProofRequest
import msdk.kotlin.sample.messages.ProofRequestMessage.Companion.extractRequestedNameFromProofRequest
import msdk.kotlin.sample.utils.wrap
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class HomePageViewModel(application: Application) : AndroidViewModel(application) {
    private val db: Database = Database.getInstance(application)
    private val actionsLiveData by lazy {
        db.actionDao().getActionsByStatus(PENDING.toString())
    }

    fun getActions(): LiveData<List<Action>> = actionsLiveData

    fun accept(actionId: Int): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        handleAcceptButton(actionId, data)
        return data
    }

    fun reject(actionId: Int): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        handleRejectButton(actionId, data)
        return data
    }

    fun newAction(invite: String): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        createAction(invite, data)
        return data
    }

    fun checkMessages(): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        checkForNewEntryMessages(data)
        return data
    }

    fun answerMessage(actionId: Int, answer: JSONObject): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        answerQuestion(actionId, answer, data)
        return data
    }

    private fun checkForNewEntryMessages(liveData: SingleLiveData<Results>) = viewModelScope.launch(Dispatchers.IO) {
        val messages = Messages.allPendingMessages.wrap().await()
        liveData.postValue(SUCCESS)
        for (message in messages) {
            if (MessageType.CREDENTIAL_OFFER.matches(message.type)) {
                handleReceivedCredentialOffer(message, liveData)
            }
            if (MessageType.PROOF_REQUEST.matches(message.type)) {
                handleReceivedProofRequest(message, liveData)
            }
            if (MessageType.QUESTION.matches(message.type)) {
                handleReceivedQuestion(message, liveData)
            }
        }
    }

    private suspend fun handleReceivedCredentialOffer(message: Message, liveData: SingleLiveData<Results>) {
        try {
            val credentialOffer = CredentialOfferMessage.parse(message)
            val pwDid: String = message.pwDid
            val connection: Connection = db.connectionDao().getByPwDid(pwDid)
            val co = Credentials.createWithOffer(UUID.randomUUID().toString(), credentialOffer!!.offer!!).wrap().await()
            val offer = CredentialOffer(
                claimId = credentialOffer.id,
                pwDid = pwDid,
                serialized = co,
                attachConnectionLogo = connection.icon,
                threadId = credentialOffer.threadId
            )
            db.credentialOffersDao().insertAll(offer)

            Messages.updateMessageStatus(pwDid, message.uid)

            createActionWithOffer(
                MessageType.CREDENTIAL_OFFER.toString(),
                credentialOffer.name,
                connection.icon!!,
                credentialOffer.attributes,
                credentialOffer.id,
                pwDid,
                liveData
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun handleReceivedProofRequest(message: Message, liveData: SingleLiveData<Results>) {
        try {
            val proofRequest = ProofRequestMessage.parse(message)!!
            val pwDid: String = message.pwDid
            val connection: Connection = db.connectionDao().getByPwDid(pwDid)
            val pr = Proofs.createWithRequest(UUID.randomUUID().toString(), proofRequest.proofReq!!).wrap().await()
            val proof = ProofRequest(
                    serialized = pr,
                    pwDid = pwDid,
                    threadId = proofRequest.threadId,
                    attachConnectionLogo = connection.icon
            )
            db.proofRequestDao().insertAll(proof)

            Messages.updateMessageStatus(pwDid, message.uid)

            createActionWithProof(
                MessageType.CREDENTIAL_OFFER.toString(),
                proofRequest.name,
                connection.icon!!,
                proofRequest.threadId,
                liveData
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun handleReceivedQuestion(
        message: Message,
        liveData: SingleLiveData<Results>
    ) {
        val question = msdk.kotlin.sample.messages.QuestionMessage.parse(message)
        val pwDid = message.pwDid
        try {
            val sm = StructuredMessage(
                pwDid = pwDid,
                entryId = question.id,
                type = question.type!!,
                serialized = message.payload,
                answers = question.responses
            )

            db.structuredMessageDao().insertAll(sm)
            Messages.updateMessageStatus(pwDid, message.uid)
            createActionWithQuestion(
                MessageType.CREDENTIAL_OFFER.toString(),
                question.questionText,
                question.questionDetail,
                pwDid,
                question.id,
                question.responses,
                liveData
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun handleAcceptButton(
        actionId: Int,
        liveData: SingleLiveData<Results>
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val action = db.actionDao().getActionsById(actionId)
            if (action.type == null) {
                StateConnections.handleConnectionInvitation(action, db, liveData)
                return@launch
            }
            if (action.type == MessageType.CREDENTIAL_OFFER.toString()) {
                val offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid)
                StateCredentialOffers.processCredentialOffer(offer!!, db, liveData, action)
                return@launch
            }
            if (action.type == MessageType.PROOF_REQUEST.toString()) {
                val proof = db.proofRequestDao().getByThreadId(action.threadId)
                StateProofRequests.processProofRequest(proof!!, db, liveData, action)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }

    private fun handleRejectButton(
        actionId: Int,
        liveData: SingleLiveData<Results>
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val action = db.actionDao().getActionsById(actionId)
            if (action.type == null) {
                HistoryActions.addToHistory(actionId, "Rejected", db, liveData)
                return@launch
            }
            if (action.type == MessageType.CREDENTIAL_OFFER.toString()) {
                val offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid)
                StateCredentialOffers.rejectCredentialOffer(offer!!, db, liveData)
                return@launch
            }
            if (action.type == MessageType.PROOF_REQUEST.toString()) {
                val proof = db.proofRequestDao().getByThreadId(action.threadId)
                StateProofRequests.rejectProofReq(proof!!, db, liveData)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }

    }

    private fun answerQuestion(actionId: Int, answer: JSONObject, liveData: SingleLiveData<Results>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val action = db.actionDao().getActionsById(actionId)
            val con = db.connectionDao().getByPwDid(action.pwDid!!)
            val sm = db.structuredMessageDao().getByEntryIdAndPwDid(action.entryId, action.pwDid)

            StructuredMessages.answer(con.serialized, sm!!.serialized, answer).wrap().await()
            sm.selectedAnswer = answer.getString("text")
            db.structuredMessageDao().update(sm)
            liveData.postValue(QUESTION_SUCCESS)
            HistoryActions.addToHistory(actionId, "Ask to question", db, liveData)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(QUESTION_FAILURE)
        }
    }

    private fun createAction(
        invite: String,
        liveData: SingleLiveData<Results>
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val parsedInvite = getConnectionInvitationFromData(invite)
            val inviteObject = JSONObject(parsedInvite)
            val invitationType = getInvitationType(parsedInvite)
            val attachment = MessageAttachment.parse(parsedInvite)

            if (isAriesOutOfBandConnectionInvitation(invitationType!!) && attachment != null) {
                if (attachment.isCredentialAttachment) {
                    val preview = attachment.data.getJSONObject("credential_preview")
                    val action = Action(
                        invite = invite,
                        name = attachment.data.getString("comment"),
                        description = inviteObject.getString("goal"),
                        details = preview.getJSONArray("attributes").getString(0)!!,
                        icon = inviteObject.getString("profileUrl"),
                        status = PENDING.toString()
                    )
                    db.actionDao().insertAll(action)
                    liveData.postValue(ACTION_SUCCESS)
                    return@launch
                }
                if (attachment.isProofAttachment) {
                    val decodedProofAttach = decodeProofRequestAttach(attachment.data)
                    val action = Action(
                        invite = invite,
                        name = extractRequestedNameFromProofRequest(decodedProofAttach),
                        description = inviteObject.getString("goal"),
                        details = extractRequestedAttributesFromProofRequest(decodedProofAttach),
                        icon = inviteObject.getString("profileUrl"),
                        status = PENDING.toString()
                    )

                    db.actionDao().insertAll(action)
                    liveData.postValue(ACTION_SUCCESS)
                    return@launch
                }
            } else {
                val action = Action(
                    invite = invite,
                    name = inviteObject.getString("label"),
                    description = inviteObject.getString("goal"),
                    icon = inviteObject.getString("profileUrl"),
                    status = PENDING.toString()
                )

                db.actionDao().insertAll(action)
                liveData.postValue(ACTION_SUCCESS)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            liveData.postValue(ACTION_FAILURE)
        }
    }

    private suspend fun createActionWithOffer(
        type: String,
        name: String,
        icon: String,
        details: String,
        offerId: String,
        pwDid: String,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val action = Action(
                type = type,
                name = name,
                description = "To issue the credential",
                icon = icon,
                details = details,
                claimId = offerId,
                pwDid = pwDid,
                status = PENDING.toString()
            )

            db.actionDao().insertAll(action)
            liveData.postValue(SUCCESS)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }

    private suspend fun createActionWithProof(
        type: String,
        name: String,
        icon: String,
        threadId: String,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val action = Action(
                type = type,
                name = name,
                description = "Share the proof",
                icon = icon,
                threadId = threadId,
                status = PENDING.toString()
            )

            db.actionDao().insertAll(action)
            liveData.postValue(SUCCESS)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }

    private suspend fun createActionWithQuestion(
        type: String,
        name: String,
        details: String,
        pwDid: String,
        entryId: String,
        messageAnswers: List<msdk.kotlin.sample.messages.QuestionMessage.Response>,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val action = Action(
                invite = null,
                type = type,
                name = name,
                description = "Answer the questions",
                details = details,
                pwDid = pwDid,
                entryId = entryId,
                messageAnswers = messageAnswers,
                status = PENDING.toString()
            )

            db.actionDao().insertAll(action)
            liveData.postValue(SUCCESS)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }

    object HistoryActions {
        suspend fun addHistoryAction(
            db: Database,
            name: String,
            description: String,
            icon: String,
            liveData: SingleLiveData<Results>
        ) {
            try {
                val action = Action(
                    invite = null,
                    name = name,
                    description = description,
                    icon = icon,
                    status = HISTORIZED.toString()
                )

                db.actionDao().insertAll(action)
                liveData.postValue(ACTION_SUCCESS)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                liveData.postValue(ACTION_FAILURE)
            }
        }

        suspend fun addToHistory(
            actionId: Int,
            description: String,
            db: Database,
            liveData: SingleLiveData<Results>
        ) {
            try {
                val action = db.actionDao().getActionsById(actionId)
                action.status = HISTORIZED.toString()
                action.description = description
                db.actionDao().update(action)
                liveData.postValue(REJECT)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                liveData.postValue(FAILURE)
            }
        }
    }
}
