package me.connect.sdk.java.sample.homepage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import me.connect.sdk.java.Connections;
import me.connect.sdk.java.ConnectionsUtils;
import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.Messages;
import me.connect.sdk.java.OutOfBandHelper;
import me.connect.sdk.java.ProofRequests;
import me.connect.sdk.java.Proofs;
import me.connect.sdk.java.StructuredMessages;
import me.connect.sdk.java.Utils;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.message.StructuredMessageHolder;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Action;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;
import me.connect.sdk.java.sample.db.entity.ProofRequest;
import me.connect.sdk.java.sample.db.entity.StructuredMessage;
import me.connect.sdk.java.sample.messages.CredDataHolder;
import me.connect.sdk.java.sample.messages.ProofDataHolder;

import static me.connect.sdk.java.sample.db.ActionStatus.HISTORIZED;
import static me.connect.sdk.java.sample.homepage.Results.ACTION_FAILURE;
import static me.connect.sdk.java.sample.homepage.Results.ACTION_SUCCESS;
import static me.connect.sdk.java.sample.homepage.Results.REJECT;
import static me.connect.sdk.java.sample.homepage.Results.FAILURE;
import static me.connect.sdk.java.sample.db.ActionStatus.PENDING;
import static me.connect.sdk.java.sample.homepage.Results.SUCCESS;
import static me.connect.sdk.java.sample.homepage.Results.QUESTION_SUCCESS;
import static me.connect.sdk.java.sample.homepage.Results.QUESTION_FAILURE;

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
            StructuredMessage sm = db.structuredMessageDao().getByEntryIdAndPwDid(action.entryId, action.pwDid);

            Connection connection = db.connectionDao().getByPwDid(sm.pwDid);
            StructuredMessages.answer(connection.serialized, sm.serialized, answer).handle((res, err) -> {
                if (err == null) {
                    try {
                        sm.selectedAnswer = answer.getString("text");
                        db.structuredMessageDao().update(sm);
                        liveData.postValue(QUESTION_FAILURE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                liveData.postValue(QUESTION_SUCCESS);
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
        CredDataHolder holder = CredDataHolder.extractDataFromCredentialsOfferMessage(message);
        String pwDid = message.getPwDid();
        Connection connection = db.connectionDao().getByPwDid(pwDid);
        try {
            Credentials.createWithOffer(UUID.randomUUID().toString(), holder.offer).handle((co, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    CredentialOffer offer = new CredentialOffer();
                    offer.threadId = holder.threadId;
                    offer.claimId = holder.id;
                    offer.pwDid = pwDid;
                    offer.serialized = co;
                    db.credentialOffersDao().insertAll(offer);

                    Messages.updateMessageStatus(pwDid, message.getUid());
                    createActionWithOffer(
                            MessageType.CREDENTIAL_OFFER.toString(),
                            holder.name,
                            connection.icon,
                            holder.id,
                            pwDid,
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
        ProofDataHolder holder = ProofDataHolder.extractRequestedFieldsFromProofMessage(message);
        String pwDid = message.getPwDid();
        Connection connection = db.connectionDao().getByPwDid(pwDid);
        try {
            Proofs.createWithRequest(UUID.randomUUID().toString(), holder.proofReq).handle((pr, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    ProofRequest proof = new ProofRequest();
                    proof.serialized = pr;
                    proof.pwDid = pwDid;
                    proof.threadId = holder.threadId;
                    db.proofRequestDao().insertAll(proof);

                    Messages.updateMessageStatus(pwDid, message.getUid());
                    createActionWithProof(
                            MessageType.CREDENTIAL_OFFER.toString(),
                            holder.name,
                            connection.icon,
                            holder.threadId,
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
        StructuredMessageHolder holder = StructuredMessages.extract(message);

        String pwDid = message.getPwDid();
        try {
            StructuredMessage sm = new StructuredMessage();
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
                    MessageType.CREDENTIAL_OFFER.toString(),
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
                    StateConnections.createConnection(action, db, liveData);
                    return;
                }
                if (action.type.equals(MessageType.CREDENTIAL_OFFER.toString())) {
                    CredentialOffer offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid);
                    StateCredentialOffers.acceptCredentialOffer(offer, db, liveData, action);
                    return;
                }
                if (action.type.equals(MessageType.PROOF_REQUEST.toString())) {
                    ProofRequest proof = db.proofRequestDao().getByThreadId(action.threadId);
                    StateProofRequests.acceptProofReq(proof, db, liveData, action);
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
                    ProofRequest proof = db.proofRequestDao().getByThreadId(action.threadId);
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
                String parsedInvite = ConnectionsUtils.parseInvite(invite);
                Connections.InvitationType invitationType = Connections.getInvitationType(parsedInvite);
                JSONObject inviteObject = new JSONObject(parsedInvite);
                JSONArray attach = inviteObject.getJSONArray("request~attach");

                if (ConnectionsUtils.isOutOfBandType(invitationType) && attach.length() == 0) {
                    Action action = new Action();
                    action.invite = invite;
                    action.name = inviteObject.getString("label");
                    action.description = inviteObject.getString("goal");
                    action.icon = inviteObject.getString("profileUrl");
                    action.status = HISTORIZED.toString();
                    db.actionDao().insertAll(action);
                    StateConnections.createConnection(action, db, liveData);
                } else if (ConnectionsUtils.isOutOfBandType(invitationType) && attach.length() != 0) {
                    String extractedAttachRequest = OutOfBandHelper.extractRequestAttach(parsedInvite);
                    JSONObject attachRequestObject = Utils.convertToJSONObject(extractedAttachRequest);

                    assert attachRequestObject != null;
                    String attachType = attachRequestObject.getString("@type");

                    if (ConnectionsUtils.isCredentialInviteType(attachType)) {
                        JSONObject preview = attachRequestObject.getJSONObject("credential_preview");
                        Action action = new Action();
                        action.invite = invite;
                        action.name = attachRequestObject.getString("comment");
                        action.description = inviteObject.getString("goal");
                        action.details = preview.getJSONArray("attributes").getString(0);
                        action.icon = inviteObject.getString("profileUrl");
                        action.status = PENDING.toString();
                        db.actionDao().insertAll(action);
                    }
                    if (ConnectionsUtils.isProofInviteType(attachType)) {
                        JSONObject decodedProofAttach = ProofRequests.decodeProofRequestAttach(attachRequestObject);

                        Action action = new Action();
                        action.invite = invite;
                        action.name = ProofRequests.extractRequestedNameFromProofRequest(decodedProofAttach);
                        action.description = inviteObject.getString("goal");
                        action.details = ProofRequests.extractRequestedAttributesFromProofRequest(decodedProofAttach);
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
            List<StructuredMessageHolder.Response> messageAnswers,
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
