package msdk.java.sample.homepage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import msdk.java.messages.ConnectionInvitation;
import msdk.java.handlers.Credentials;
import msdk.java.handlers.Messages;
import msdk.java.messages.CredentialOfferMessage;
import msdk.java.messages.ProofRequestMessage;
import msdk.java.handlers.Proofs;
import msdk.java.handlers.StructuredMessages;
import msdk.java.messages.Message;
import msdk.java.sample.db.entity.ProofRequest;
import msdk.java.sample.db.entity.StructuredMessage;
import msdk.java.sample.history.HistoryHandler;
import msdk.java.types.MessageAttachment;
import msdk.java.types.MessageType;
import msdk.java.messages.QuestionMessage;
import msdk.java.sample.SingleLiveData;
import msdk.java.sample.db.Database;
import msdk.java.sample.db.entity.Action;
import msdk.java.sample.db.entity.Connection;
import msdk.java.sample.db.entity.CredentialOffer;

import static msdk.java.sample.homepage.Results.ACTION_FAILURE;
import static msdk.java.sample.homepage.Results.ACTION_SUCCESS;
import static msdk.java.sample.homepage.Results.FAILURE;
import static msdk.java.sample.db.ActionStatus.PENDING;
import static msdk.java.sample.homepage.Results.SUCCESS;
import static msdk.java.sample.homepage.Results.QUESTION_SUCCESS;
import static msdk.java.sample.homepage.Results.QUESTION_FAILURE;

public class HomePageViewModel extends AndroidViewModel {
    private final Database db;
    private LiveData<List<Action>> actions;

    public HomePageViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<List<Action>> getActions() {
        if (actions == null) {
            actions = db.actionDao().getActionsByStatus(PENDING.toString());
        }
        return actions;
    }

    public SingleLiveData<Results> accept(int actionId) {
        SingleLiveData<Results> data = new SingleLiveData<>();
        handleAcceptButton(actionId, data);
        return data;
    }

    public SingleLiveData<Results> reject(int actionId) {
        SingleLiveData<Results> data = new SingleLiveData<>();
        handleRejectButton(actionId, data);
        return data;
    }

    public SingleLiveData<Results> createActionWithInvitation(String invite) {
        SingleLiveData<Results> data = new SingleLiveData<>();
        createActionWithInvitation(invite, data);
        return data;
    }

    public SingleLiveData<Results> checkMessages() {
        SingleLiveData<Results> data = new SingleLiveData<>();
        checkForNewEntryMessages(data);
        return data;
    }

    public SingleLiveData<Results> answerMessage(int actionId, JSONObject answer) {
        SingleLiveData<Results> data = new SingleLiveData<>();
        answerQuestion(actionId, answer, data);
        return data;
    }

    private void checkForNewEntryMessages(SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Messages.getAllPendingMessages().handle((messages, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                liveData.postValue(SUCCESS);
                try {
                    for (Message message : messages) {
                        if (MessageType.CREDENTIAL_OFFER.matches(message.getType())) {
                            handleReceivedCredentialOffer(message, liveData);
                        }
                        if (MessageType.PROOF_REQUEST.matches(message.getType())) {
                            handleReceivedProofRequest(message, liveData);
                        }
                        if (MessageType.QUESTION.matches(message.getType())) {
                            handleReceivedQuestion(message, liveData);
                        }
                        Messages.updateMessageStatus(message.getPwDid(), message.getUid()).get();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    liveData.postValue(FAILURE);
                }
                return null;
            });
        });
    }

    private void handleReceivedCredentialOffer(Message message, SingleLiveData<Results> liveData) {
        try {
            Connection connection = db.connectionDao().getByPwDid(message.getPwDid());
            CredentialOfferMessage credentialOffer = CredentialOfferMessage.parse(message);
            Credentials.createWithOffer(UUID.randomUUID().toString(), credentialOffer.offer).handle((serialized, err) -> {
                if (err != null) {
                    err.printStackTrace();
                    liveData.postValue(FAILURE);
                } else {
                    CredentialOffer offer = new CredentialOffer();
                    offer.threadId = credentialOffer.threadId;
                    offer.claimId = credentialOffer.id;
                    offer.pwDid = message.getPwDid();
                    offer.serialized = serialized;
                    db.credentialOffersDao().insertAll(offer);

                    Action action = Actions.createActionWithOffer(
                            MessageType.CREDENTIAL_OFFER.toString(),
                            credentialOffer.name,
                            connection.icon,
                            credentialOffer.attributes,
                            credentialOffer.id,
                            connection.pwDid,
                            null
                    );
                    db.actionDao().insertAll(action);
                    liveData.postValue(SUCCESS);
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            liveData.postValue(FAILURE);
        }
    }

    private void handleReceivedProofRequest(Message message, SingleLiveData<Results> liveData) {
        try {
            Connection connection = db.connectionDao().getByPwDid(message.getPwDid());
            ProofRequestMessage proofRequest = ProofRequestMessage.parse(message);
            Proofs.createWithRequest(UUID.randomUUID().toString(), proofRequest.proofReq).handle((pr, err) -> {
                if (err != null) {
                    err.printStackTrace();
                    liveData.postValue(FAILURE);
                } else {
                    ProofRequest proof = new ProofRequest();
                    proof.serialized = pr;
                    proof.pwDid = message.getPwDid();
                    proof.threadId = proofRequest.threadId;
                    db.proofRequestDao().insertAll(proof);

                    Action action = Actions.createActionWithProof(
                            MessageType.CREDENTIAL_OFFER.toString(),
                            proofRequest.name,
                            connection.icon,
                            proofRequest.threadId,
                            proofRequest.attributes,
                            null
                    );
                    db.actionDao().insertAll(action);
                    liveData.postValue(SUCCESS);
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            liveData.postValue(FAILURE);
        }
    }

    private void handleReceivedQuestion(Message message, SingleLiveData<Results> liveData) {
        try {
            QuestionMessage question = QuestionMessage.parse(message);

            StructuredMessage sm = new StructuredMessage();
            sm.pwDid = message.getPwDid();
            sm.entryId = question.getId();
            sm.type = question.getType();
            sm.serialized = message.getPayload();
            sm.answers = question.getResponses();
            db.structuredMessageDao().insertAll(sm);

            Action action = Actions.createActionWithQuestion(
                    MessageType.QUESTION.toString(),
                    question.getQuestionText(),
                    question.getQuestionDetail(),
                    message.getPwDid(),
                    question.getId(),
                    question.getResponses()
            );
            db.actionDao().insertAll(action);
            liveData.postValue(SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            liveData.postValue(FAILURE);
        }
    }

    private void handleAcceptButton(int actionId, SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Action action = db.actionDao().getActionsById(actionId);
                if (action.type.equals(MessageType.CONNECTION_INVITATION.toString())) {
                    ConnectionsHandler.handleConnectionInvitation(action, db, liveData);
                    return;
                }
                if (action.type.equals(MessageType.CREDENTIAL_OFFER.toString())) {
                    CredentialOffer offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid);
                    CredentialOffersHandler.acceptCredentialOffer(offer, db, liveData, action);
                    return;
                }
                if (action.type.equals(MessageType.PROOF_REQUEST.toString())) {
                    ProofRequest proof = db.proofRequestDao().getByThreadId(action.threadId);
                    ProofRequestsHandler.acceptProofRequest(proof, db, liveData, action);
                }
            } catch (Exception e) {
                e.printStackTrace();
                liveData.postValue(FAILURE);
            }
        });
    }

    private void handleRejectButton(int actionId, SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Action action = db.actionDao().getActionsById(actionId);
                if (action.type.equals(MessageType.CONNECTION_INVITATION.toString())) {
                    HistoryHandler.addToHistory(actionId, "Connection Rejected", db, liveData);
                    return;
                }
                if (action.type.equals(MessageType.CREDENTIAL_OFFER.toString())) {
                    CredentialOffer offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid);
                    CredentialOffersHandler.rejectCredentialOffer(offer, db, liveData);
                    return;
                }
                if (action.type.equals(MessageType.PROOF_REQUEST.toString())) {
                    ProofRequest proof = db.proofRequestDao().getByThreadId(action.threadId);
                    ProofRequestsHandler.rejectProofReq(proof, db, liveData);
                }
            } catch (Exception e) {
                e.printStackTrace();
                liveData.postValue(FAILURE);
            }
        });
    }

    private void answerQuestion(int actionId, JSONObject answer, SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Action action = db.actionDao().getActionsById(actionId);
            StructuredMessage sm = db.structuredMessageDao().getByEntryIdAndPwDid(action.entryId, action.pwDid);

            Connection connection = db.connectionDao().getByPwDid(sm.pwDid);
            StructuredMessages.answer(connection.serialized, sm.serialized, answer).handle((res, err) -> {
                if (err == null) {
                    try {
                        sm.selectedAnswer = answer.getString("text");
                        db.structuredMessageDao().update(sm);
                        liveData.postValue(QUESTION_SUCCESS);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    liveData.postValue(QUESTION_FAILURE);
                }
                return null;
            });
            HistoryHandler.addToHistory(actionId, "Ask to question", db, liveData);
        });
    }

    private void createActionWithInvitation(String invite, SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String parsedInvite = ConnectionInvitation.getConnectionInvitationFromData(invite);
                JSONObject inviteObject = new JSONObject(parsedInvite);
                ConnectionInvitation.InvitationType invitationType = ConnectionInvitation.getInvitationType(parsedInvite);
                MessageAttachment attachment = MessageAttachment.parse(parsedInvite);

                Action action = null;

                if (ConnectionInvitation.isAriesOutOfBandConnectionInvitation(invitationType) && attachment != null) {
                    if (attachment.isCredentialAttachment()) {
                        JSONObject attributes = CredentialOfferMessage.extractAttributesFromCredentialOffer(attachment.data);
                        action = Actions.createActionWithOffer(
                                MessageType.CONNECTION_INVITATION.toString(),
                                attachment.data.getString("comment"),
                                inviteObject.getString("profileUrl"),
                                attributes,
                                null,
                                null,
                                invite
                        );
                    }
                    if (attachment.isProofAttachment()) {
                        JSONObject decodedProofAttach = ProofRequestMessage.decodeProofRequestAttach(attachment.data);
                        JSONObject requestedAttributes = ProofRequestMessage.extractRequestedAttributesFromProofRequest(decodedProofAttach);
                        action = Actions.createActionWithProof(
                                MessageType.CONNECTION_INVITATION.toString(),
                                ProofRequestMessage.extractRequestedNameFromProofRequest(decodedProofAttach),
                                inviteObject.getString("profileUrl"),
                                null,
                                requestedAttributes,
                                invite
                        );
                    }
                } else {
                    action = Actions.createActionWithConnectionInvitation(
                            MessageType.CONNECTION_INVITATION.toString(),
                            inviteObject.getString("label"),
                            inviteObject.getString("goal"),
                            inviteObject.getString("profileUrl"),
                            invite
                    );
                }
                liveData.postValue(ACTION_SUCCESS);
                db.actionDao().insertAll(action);
            } catch (JSONException e) {
                e.printStackTrace();
                liveData.postValue(ACTION_FAILURE);
            }
        });
    }
}
