package me.connect.sdk.java.sample.messages;

import android.content.Context;
import android.util.Log;

import java.util.UUID;

import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.Proofs;
import me.connect.sdk.java.StructuredMessages;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.MessageStatusType;
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.message.StructuredMessageHolder;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;
import me.connect.sdk.java.sample.db.entity.ProofRequest;
import me.connect.sdk.java.sample.db.entity.StructuredMessage;

public class MessageProcessor {
    public static final String TAG = "MessageProcessor";
    Database db;
    Connection conn;

    public MessageProcessor(Context context, String connPwDid) {
        db = Database.getInstance(context);
        conn = db.connectionDao().getByPwDid(connPwDid);
    }

    public void process(Message message) {
        if (!message.getStatus().equals(MessageStatusType.PENDING)) {
            Log.d(TAG, "Required message with state MS-103, got: " + message.getStatus());
            return;
        }
        String type = message.getType();
        if (MessageType.CREDENTIAL_OFFER.matches(type)) {
            handleCredOfferMessasge(message);
        } else if (MessageType.PROOF_REQUEST.matches(type)) {
            handleProofRequestMessasge(message);
        } else if (MessageType.QUESTION.matches(type)) {
            handleQuestionMessasge(message);
        } else {
            Log.w(TAG, "Unsupported message type: " + type);
        }
    }

    private void handleCredOfferMessasge(Message message) {
        CredDataHolder holder = CredDataHolder.extractDataFromCredentialsOfferMessage(message);
        if (!db.credentialOffersDao().checkOfferExists(holder.id, conn.id)) {
            Credentials.createWithOffer(conn.serialized, UUID.randomUUID().toString(), holder.offer).whenComplete((co, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    CredentialOffer offer = new CredentialOffer();
                    offer.claimId = holder.id;
                    offer.name = holder.name;
                    offer.connectionId = conn.id;
                    offer.attributes = holder.attributes;
                    offer.serialized = co;
                    offer.messageId = message.getUid();
                    db.credentialOffersDao().insertAll(offer);
                }
            });
        }
    }

    private void handleProofRequestMessasge(Message message) {
        ProofDataHolder holder = ProofDataHolder.extractRequestedFieldsFromProofMessage(message);
        if (!db.proofRequestDao().checkExists(holder.threadId)) {
            Proofs.createWithRequest(UUID.randomUUID().toString(), holder.proofReq).whenComplete((pr, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    ProofRequest proof = new ProofRequest();
                    proof.serialized = pr;
                    proof.name = holder.name;
                    proof.connectionId = conn.id;
                    proof.attributes = holder.attributes;
                    proof.threadId = holder.threadId;
                    proof.messageId = message.getUid();
                    db.proofRequestDao().insertAll(proof);
                }
            });
        }
    }

    private void handleQuestionMessasge(Message message) {
        StructuredMessageHolder holder = StructuredMessages.extract(message);
        if (!db.structuredMessageDao().checkMessageExists(holder.getId(), conn.id)) {
            StructuredMessage sm = new StructuredMessage();
            sm.connectionId = conn.id;
            sm.messageId = message.getUid();
            sm.entryId = holder.getId();
            sm.questionText = holder.getQuestionText();
            sm.questionDetail = holder.getQuestionDetail();
            sm.answers = holder.getResponses();
            sm.type = holder.getType();
            sm.serialized = message.getPayload();
            db.structuredMessageDao().insertAll(sm);
        }
    }
}
