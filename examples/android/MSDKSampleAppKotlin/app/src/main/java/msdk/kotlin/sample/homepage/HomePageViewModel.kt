package msdk.kotlin.sample.homepage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import msdk.kotlin.sample.SingleLiveData
import msdk.kotlin.sample.db.ActionStatus.PENDING
import msdk.kotlin.sample.db.Database
import msdk.kotlin.sample.db.entity.*
import msdk.kotlin.sample.handlers.Credentials
import msdk.kotlin.sample.handlers.Messages
import msdk.kotlin.sample.handlers.Proofs
import msdk.kotlin.sample.handlers.StructuredMessages
import msdk.kotlin.sample.history.HistoryHandler
import msdk.kotlin.sample.homepage.Results.*
import msdk.kotlin.sample.messages.ConnectionInvitation
import msdk.kotlin.sample.messages.ConnectionInvitation.getConnectionInvitationFromData
import msdk.kotlin.sample.messages.ConnectionInvitation.getInvitationType
import msdk.kotlin.sample.messages.CredentialOfferMessage
import msdk.kotlin.sample.messages.Message
import msdk.kotlin.sample.messages.ProofRequestMessage
import msdk.kotlin.sample.messages.ProofRequestMessage.Companion.decodeProofRequestAttach
import msdk.kotlin.sample.messages.ProofRequestMessage.Companion.extractRequestedAttributesFromProofRequest
import msdk.kotlin.sample.messages.ProofRequestMessage.Companion.extractRequestedNameFromProofRequest
import msdk.kotlin.sample.types.MessageAttachment
import msdk.kotlin.sample.types.MessageType
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

    fun createActionWithInvitation(invite: String): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        createActionWithInvitation(invite, data)
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
            Messages.updateMessageStatus(message.pwDid, message.uid)
        }
    }

    private suspend fun handleReceivedCredentialOffer(message: Message, liveData: SingleLiveData<Results>) {
        try {
            val credentialOffer = CredentialOfferMessage.parse(message)
            val connection: Connection = db.connectionDao().getByPwDid(message.pwDid)

            val co = Credentials.createWithOffer(UUID.randomUUID().toString(), credentialOffer!!.offer!!).wrap().await()
            val offer = CredentialOffer(
                claimId = credentialOffer.id,
                pwDid = message.pwDid,
                serialized = co,
                attachConnectionLogo = connection.icon,
                threadId = credentialOffer.threadId
            )
            db.credentialOffersDao().insertAll(offer)

            val action: Action = Actions.createActionWithOffer(
                MessageType.CREDENTIAL_OFFER.toString(),
                credentialOffer.name,
                connection.icon!!,
                credentialOffer.attributes,
                credentialOffer.id,
                connection.pwDid,
                null
            )
            db.actionDao().insertAll(action)
            liveData.postValue(SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun handleReceivedProofRequest(message: Message, liveData: SingleLiveData<Results>) {
        try {
            val proofRequest = ProofRequestMessage.parse(message)!!
            val connection: Connection = db.connectionDao().getByPwDid(message.pwDid)
            val pr = Proofs.createWithRequest(UUID.randomUUID().toString(), proofRequest.proofReq!!).wrap().await()
            val proof = ProofRequest(
                    serialized = pr,
                    pwDid = message.pwDid,
                    threadId = proofRequest.threadId,
                    attachConnectionLogo = connection.icon
            )
            db.proofRequestDao().insertAll(proof)

            val action: Action = Actions.createActionWithProof(
                MessageType.CREDENTIAL_OFFER.toString(),
                proofRequest.name,
                connection.icon!!,
                proofRequest.threadId,
                proofRequest.attributes,
                null
            )
            db.actionDao().insertAll(action)
            liveData.postValue(SUCCESS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun handleReceivedQuestion(
        message: Message,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val question = msdk.kotlin.sample.messages.QuestionMessage.parse(message)
            val sm = StructuredMessage(
                pwDid = message.pwDid,
                entryId = question.id,
                type = question.type!!,
                serialized = message.payload,
                answers = question.responses
            )

            db.structuredMessageDao().insertAll(sm)

            val action = Actions.createActionWithQuestion(
                MessageType.QUESTION.toString(),
                question.questionText,
                question.questionDetail,
                message.pwDid,
                question.id,
                question.responses
            )
            db.actionDao().insertAll(action)
            liveData.postValue(SUCCESS)
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
            if (action.type == MessageType.CONNECTION_INVITATION.toString()) {
                ConnectionsHandler.handleConnectionInvitation(action, db, liveData)
                return@launch
            }
            if (action.type == MessageType.CREDENTIAL_OFFER.toString()) {
                val offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid)
                CredentialOffersHandler.processCredentialOffer(offer!!, db, liveData, action)
                return@launch
            }
            if (action.type == MessageType.PROOF_REQUEST.toString()) {
                val proof = db.proofRequestDao().getByThreadId(action.threadId)
                ProofRequestsHandler.processProofRequest(proof!!, db, liveData, action)
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
            if (action.type == MessageType.CONNECTION_INVITATION.toString()) {
                HistoryHandler.addToHistory(actionId, "Rejected", db, liveData)
                return@launch
            }
            if (action.type == MessageType.CREDENTIAL_OFFER.toString()) {
                val offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid)
                CredentialOffersHandler.rejectCredentialOffer(offer!!, db, liveData)
                return@launch
            }
            if (action.type == MessageType.PROOF_REQUEST.toString()) {
                val proof = db.proofRequestDao().getByThreadId(action.threadId)
                ProofRequestsHandler.rejectProofReq(proof!!, db, liveData)
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
            HistoryHandler.addToHistory(actionId, "Ask to question", db, liveData)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(QUESTION_FAILURE)
        }
    }

    private fun createActionWithInvitation(
        invite: String,
        liveData: SingleLiveData<Results>
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val parsedInvite = getConnectionInvitationFromData(invite)
            val inviteObject = JSONObject(parsedInvite)
            val invitationType = getInvitationType(parsedInvite)
            val attachment = MessageAttachment.parse(parsedInvite)

            var action: Action? = null

            if (ConnectionInvitation.isAriesOutOfBandConnectionInvitation(invitationType!!) && attachment != null) {
                if (attachment.isCredentialAttachment) {
                    val attributes =
                        CredentialOfferMessage.extractAttributesFromCredentialOffer(attachment.data)
                    action = Actions.createActionWithOffer(
                        MessageType.CONNECTION_INVITATION.toString(),
                        attachment.data.getString("comment"),
                        inviteObject.getString("profileUrl"),
                        attributes!!,
                        null,
                        null,
                        invite
                    )
                }
                if (attachment.isProofAttachment) {
                    val decodedProofAttach =
                        decodeProofRequestAttach(attachment.data)
                    val requestedAttributes: JSONObject? =
                        extractRequestedAttributesFromProofRequest(
                            decodedProofAttach
                        )
                    action = Actions.createActionWithProof(
                        MessageType.CONNECTION_INVITATION.toString(),
                        extractRequestedNameFromProofRequest(decodedProofAttach),
                        inviteObject.getString("profileUrl"),
                        null,
                        requestedAttributes!!,
                        invite
                    )
                }
            } else {
                var goal: String? = "Connection request"
                if (inviteObject.has("goal")) {
                    goal = inviteObject.getString("goal")
                }
                action = Actions.createActionWithConnectionInvitation(
                    MessageType.CONNECTION_INVITATION.toString(),
                    inviteObject.getString("label"),
                    goal,
                    inviteObject.getString("profileUrl"),
                    invite
                )
            }
            liveData.postValue(ACTION_SUCCESS)
            db.actionDao().insertAll(action!!)
            return@launch
        } catch (e: JSONException) {
            e.printStackTrace()
            liveData.postValue(ACTION_FAILURE)
        }
    }
}
