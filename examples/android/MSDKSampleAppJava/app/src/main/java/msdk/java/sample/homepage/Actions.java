package msdk.java.sample.homepage;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import msdk.java.messages.QuestionMessage;
import msdk.java.sample.db.entity.Action;

import static msdk.java.sample.db.ActionStatus.PENDING;

public class Actions {
    public static Action createActionWithConnectionInvitation(
            String type,
            String name,
            String description,
            String icon,
            String invite
    ) {
        Action action = new Action();
        action.type = type;
        action.invite = invite;
        action.name = name;
        action.description = description;
        action.icon = icon;
        action.status = PENDING.toString();
        return action;
    }

    public static Action createActionWithOffer(
            String type,
            String name,
            String icon,
            JSONObject attributes,
            String offerId,
            String pwDid,
            String invite
    ) {
        Action action = new Action();
        action.type = type;
        action.name = name;
        action.description = "To issue the credential";
        action.icon = icon;
        action.details = buildCredentialOfferAttributesDetailsString(attributes);
        action.claimId = offerId;
        action.pwDid = pwDid;
        action.invite = invite;
        action.status = PENDING.toString();
        return action;
    }

    public static Action createActionWithProof(
            String type,
            String name,
            String icon,
            String threadId,
            JSONObject attributes,
            String invite
    ) {
        Action action = new Action();
        action.type = type;
        action.name = name;
        action.description = "Share the proof";
        action.icon = icon;
        action.details = buildProofRequestDetailsString(attributes);
        action.threadId = threadId;
        action.invite = invite;
        action.status = PENDING.toString();
        return action;
    }

    public static Action createActionWithQuestion(
            String type,
            String name,
            String details,
            String pwDid,
            String entryId,
            List<QuestionMessage.Response> messageAnswers
    ) {
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
        return action;
    }

    public static String buildCredentialOfferAttributesDetailsString(JSONObject attributes) {
        StringBuilder attributesString = new StringBuilder();
        Iterator<String> keys = attributes.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = attributes.optString(key);
            attributesString.append(String.format("%s: %s\n", key, value));
        }
        return attributesString.toString();
    }

    public static String buildProofRequestDetailsString(JSONObject requestedAttributes) {
        StringBuilder attributesString = new StringBuilder();
        Iterator<String> keys = requestedAttributes.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject requestedAttribute = requestedAttributes.optJSONObject(key);
            attributesString.append(String.format("%s\n", requestedAttribute.optString("name")));
        }
        return attributesString.toString();
    }
}
