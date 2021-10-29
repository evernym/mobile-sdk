package msdk.java.sample.homepage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import msdk.java.messages.ConnectionInvitation;
import msdk.java.handlers.Credentials;
import msdk.java.handlers.Messages;
import msdk.java.messages.ProofRequest;
import msdk.java.handlers.Proofs;
import msdk.java.handlers.StructuredMessages;
import msdk.java.messages.Message;
import msdk.java.types.MessageAttachment;
import msdk.java.types.MessageType;
import msdk.java.messages.StructuredMessage;
import msdk.java.sample.SingleLiveData;
import msdk.java.sample.db.Database;
import msdk.java.sample.db.entity.Action;
import msdk.java.sample.db.entity.Connection;
import msdk.java.sample.db.entity.CredentialOffer;

import static msdk.java.sample.db.ActionStatus.HISTORIZED;
import static msdk.java.sample.homepage.Results.ACTION_FAILURE;
import static msdk.java.sample.homepage.Results.ACTION_SUCCESS;
import static msdk.java.sample.homepage.Results.REJECT;
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
        acceptProcess(actionId, data);
        return data;
    }

    public SingleLiveData<Results> reject(int actionId) {
        SingleLiveData<Results> data = new SingleLiveData<>();
        rejectProcess(actionId, data);
        return data;
    }

    public SingleLiveData<Results> newAction(String invite) {
        SingleLiveData<Results> data = new SingleLiveData<>();
        createAction(invite, data);
        return data;
    }

    public SingleLiveData<Results> checkMessages() {
        SingleLiveData<Results> data = new SingleLiveData<>();
        checkAllMessages(data);
        return data;
    }

    public SingleLiveData<Results> answerMessage(int actionId, JSONObject answer) {
        SingleLiveData<Results> data = new SingleLiveData<>();
        answerStructMessage(actionId, answer, data);
        return data;
    }

    private void answerStructMessage(int actionId, JSONObject answer, SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Action action = db.actionDao().getActionsById(actionId);
            msdk.java.sample.db.entity.StructuredMessage sm = db.structuredMessageDao().getByEntryIdAndPwDid(action.entryId, action.pwDid);

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
            addToHistory(actionId, "Ask to question", db, liveData);
        });
    }

    private void checkAllMessages(SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Messages.getAllPendingMessages().handle((messages, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                liveData.postValue(SUCCESS);
                for (Message message : messages) {
                    if (MessageType.CREDENTIAL_OFFER.matches(message.getType())) {
                        credentialOffersProcess(message, liveData);
                    }
                    if (MessageType.PROOF_REQUEST.matches(message.getType())) {
                        proofRequestProcess(message, liveData);
                    }
                    if (MessageType.QUESTION.matches(message.getType())) {
                        questionsProcess(message, liveData);
                    }
                }
                return null;
            });
        });
    }

    private void credentialOffersProcess(Message message, SingleLiveData<Results> liveData) {
        msdk.java.messages.CredentialOffer credentialOffer = msdk.java.messages.CredentialOffer.parseCredentialOfferMessage(message);
        String pwDid = message.getPwDid();
        Connection connection = db.connectionDao().getByPwDid(pwDid);
        try {
            Credentials.createWithOffer(UUID.randomUUID().toString(), credentialOffer.offer).handle((co, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    CredentialOffer offer = new CredentialOffer();
                    offer.threadId = credentialOffer.threadId;
                    offer.claimId = credentialOffer.id;
                    offer.pwDid = pwDid;
                    offer.serialized = co;
                    db.credentialOffersDao().insertAll(offer);

                    Messages.updateMessageStatus(pwDid, message.getUid());
                    createActionWithOffer(
                            MessageType.CREDENTIAL_OFFER.toString(),
                            credentialOffer.name,
                            connection.icon,
                            credentialOffer.attributes,
                            credentialOffer.id,
                            connection.pwDid,
                            liveData
                    );
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void proofRequestProcess(Message message, SingleLiveData<Results> liveData) {
        ProofRequest proofRequest = ProofRequest.parseProofRequestMessage(message);
        String pwDid = message.getPwDid();
        Connection connection = db.connectionDao().getByPwDid(pwDid);
        try {
            Proofs.createWithRequest(UUID.randomUUID().toString(), proofRequest.proofReq).handle((pr, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    msdk.java.sample.db.entity.ProofRequest proof = new msdk.java.sample.db.entity.ProofRequest();
                    proof.serialized = pr;
                    proof.pwDid = pwDid;
                    proof.threadId = proofRequest.threadId;
                    db.proofRequestDao().insertAll(proof);

                    Messages.updateMessageStatus(pwDid, message.getUid());
                    createActionWithProof(
                            MessageType.CREDENTIAL_OFFER.toString(),
                            proofRequest.name,
                            connection.icon,
                            proofRequest.threadId,
                            liveData
                    );
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void questionsProcess(Message message, SingleLiveData<Results> liveData) {
        StructuredMessage holder = StructuredMessage.extract(message);

        String pwDid = message.getPwDid();
        try {
            msdk.java.sample.db.entity.StructuredMessage sm = new msdk.java.sample.db.entity.StructuredMessage();
            sm.pwDid = pwDid;
            sm.entryId = holder.getId();
            sm.type = holder.getType();
            sm.serialized = message.getPayload();
            sm.answers = holder.getResponses();
            db.structuredMessageDao().insertAll(sm);

            if ("question".equals(holder.getType())) {
                Messages.updateMessageStatus(pwDid, message.getUid());
            }
            createActionWithQuestion(
                    MessageType.QUESTION.toString(),
                    holder.getQuestionText(),
                    holder.getQuestionDetail(),
                    pwDid,
                    holder.getId(),
                    holder.getResponses(),
                    liveData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void acceptProcess(int actionId, SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Action action = db.actionDao().getActionsById(actionId);
                if (action.type == null) {
                    StateConnections.handleConnectionInvitation(action, db, liveData);
                    return;
                }
                if (action.type.equals(MessageType.CREDENTIAL_OFFER.toString())) {
                    CredentialOffer offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid);
                    StateCredentialOffers.processCredentialOffer(offer, db, liveData, action);
                    return;
                }
                if (action.type.equals(MessageType.PROOF_REQUEST.toString())) {
                    msdk.java.sample.db.entity.ProofRequest proof = db.proofRequestDao().getByThreadId(action.threadId);
                    StateProofRequests.processProofRequest(proof, db, liveData, action);
                }
            } catch (Exception e) {
                e.printStackTrace();
                liveData.postValue(FAILURE);
            }
        });
    }

    private void rejectProcess(int actionId, SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Action action = db.actionDao().getActionsById(actionId);
                if (action.type == null) {
                    addToHistory(actionId, "Rejected", db, liveData);
                    return;
                }
                if (action.type.equals(MessageType.CREDENTIAL_OFFER.toString())) {
                    CredentialOffer offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid);
                    StateCredentialOffers.rejectCredentialOffer(offer, db, liveData);
                    return;
                }
                if (action.type.equals(MessageType.PROOF_REQUEST.toString())) {
                    msdk.java.sample.db.entity.ProofRequest proof = db.proofRequestDao().getByThreadId(action.threadId);
                    StateProofRequests.rejectProofReq(proof, db, liveData);
                }
            } catch (Exception e) {
                e.printStackTrace();
                liveData.postValue(FAILURE);
            }
        });
    }

    private void createAction(String invite, SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String parsedInvite = ConnectionInvitation.getConnectionInvitationFromData(invite);
                JSONObject inviteObject = new JSONObject(parsedInvite);
                ConnectionInvitation.InvitationType invitationType = ConnectionInvitation.getInvitationType(parsedInvite);
                MessageAttachment attachment = MessageAttachment.parse(parsedInvite);

                if (ConnectionInvitation.isAriesOutOfBandConnectionInvitation(invitationType) && attachment != null) {
                    if (attachment.isCredentialAttachment()) {
                        JSONObject preview = attachment.data.getJSONObject("credential_preview");

                        Action action = new Action();
                        action.invite = invite;
                        action.name = attachment.data.getString("comment");
                        action.description = inviteObject.getString("goal");
                        action.details = preview.getJSONArray("attributes").getString(0);
                        action.icon = inviteObject.getString("profileUrl");
                        action.status = PENDING.toString();
                        db.actionDao().insertAll(action);
                    }
                    if (attachment.isProofAttachment()) {
                        JSONObject decodedProofAttach = ProofRequest.decodeProofRequestAttach(attachment.data);

                        Action action = new Action();
                        action.invite = invite;
                        action.name = ProofRequest.extractRequestedNameFromProofRequest(decodedProofAttach);
                        action.description = inviteObject.getString("goal");
                        action.details = ProofRequest.extractRequestedAttributesFromProofRequest(decodedProofAttach);
                        action.icon = inviteObject.getString("profileUrl");
                        action.status = PENDING.toString();
                        db.actionDao().insertAll(action);
                    }
                } else {
                    Action action = new Action();
                    action.invite = invite;
                    action.name = inviteObject.getString("label");
                    action.description = inviteObject.getString("goal");
                    action.icon = inviteObject.getString("profileUrl");
                    action.status = PENDING.toString();
                    db.actionDao().insertAll(action);
                }
                liveData.postValue(ACTION_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
                liveData.postValue(ACTION_FAILURE);
            }
        });
    }

    private void createActionWithOffer(
            String type,
            String name,
            String icon,
            String details,
            String offerId,
            String pwDid,
            SingleLiveData<Results> liveData
    ) {
        try {
            Action action = new Action();
            action.type = type;
            action.name = name;
            action.description = "To issue the credential";
            action.icon = icon;
            action.details = details;
            action.claimId = offerId;
            action.pwDid = pwDid;
            action.status = PENDING.toString();
            db.actionDao().insertAll(action);

            liveData.postValue(SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            liveData.postValue(FAILURE);
        }
    }

    private void createActionWithProof(
            String type,
            String name,
            String icon,
            String threadId,
            SingleLiveData<Results> liveData
    ) {
        try {
            Action action = new Action();
            action.type = type;
            action.name = name;
            action.description = "Share the proof";
            action.icon = icon;
            action.threadId = threadId;
            action.status = PENDING.toString();
            db.actionDao().insertAll(action);

            liveData.postValue(SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            liveData.postValue(FAILURE);
        }
    }

    private void createActionWithQuestion(
            String type,
            String name,
            String details,
            String pwDid,
            String entryId,
            List<StructuredMessage.Response> messageAnswers,
            SingleLiveData<Results> liveData
    ) {
        try {
            Action action = new Action();
            action.invite = null;
            action.type = type;
            action.name = name;
            action.description = "Answer the questions";
            action.details = details;
            action.pwDid = pwDid;
            action.entryId = entryId;
            action.messageAnswers = messageAnswers;
            action.status = PENDING.toString();
            db.actionDao().insertAll(action);

            liveData.postValue(SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            liveData.postValue(FAILURE);
        }
    }

    public static void addHistoryAction(
            Database db,
            String name,
            String description,
            String icon,
            SingleLiveData<Results> liveData
    ) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Action action = new Action();
                action.invite = null;
                action.name = name;
                action.description = description;
                action.icon = icon;
                action.status = HISTORIZED.toString();
                db.actionDao().insertAll(action);

                liveData.postValue(ACTION_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                liveData.postValue(ACTION_FAILURE);
            }
        });
    }

    public static void addToHistory(int actionId, String description, Database db, SingleLiveData<Results> liveData) {
        try {
            Action action = db.actionDao().getActionsById(actionId);
            action.status = HISTORIZED.toString();
            action.description = description;
            db.actionDao().update(action);
            liveData.postValue(REJECT);
        } catch (Exception e) {
            e.printStackTrace();
            liveData.postValue(FAILURE);
        }
    }
}
