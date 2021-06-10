package me.connect.sdk.java.sample.connections;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.Proofs;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;
import me.connect.sdk.java.sample.db.entity.ProofRequest;

import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.PROOF_ATTACH;
import static me.connect.sdk.java.sample.connections.ConnectionCreateResult.REQUEST_ATTACH;

public class OutOfBandHelper {

    public static String extractRequestAttach(String invite) {
        try {
            JSONObject json = Utils.convertToJSONObject(invite);
            if (json != null && json.has("request~attach")) {
                String requestAttachCode = json.getString("request~attach");
                JSONArray requestsAttachItems = new JSONArray(requestAttachCode);
                if (requestsAttachItems.length() == 0) {
                    return null;
                }
                JSONObject requestsAttachItem = requestsAttachItems.getJSONObject(0);
                JSONObject requestsAttachItemData = requestsAttachItem.getJSONObject("data");
                String requestsAttachItemBase = requestsAttachItemData.getString("base64");
                String requestAttachDecode = new String(Base64.decode(requestsAttachItemBase, Base64.NO_WRAP));
                JSONObject result = Utils.convertToJSONObject(requestAttachDecode);
                if (result != null) {
                    result.put("@id", requestsAttachItem.getString("@id"));
                    return result.toString();
                }
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String extractAttachmentFromProofAttach(JSONObject proofAttach) {
        try {
            if (proofAttach != null && proofAttach.has("request_presentations~attach")) {
                String requestAttachCode = proofAttach.getString("request_presentations~attach");
                JSONArray requestsAttachItems = new JSONArray(requestAttachCode);
                if (requestsAttachItems.length() == 0) {
                    return null;
                }
                JSONObject requestsAttachItem = requestsAttachItems.getJSONObject(0);
                JSONObject requestsAttachItemData = requestsAttachItem.getJSONObject("data");
                String requestsAttachItemBase = requestsAttachItemData.getString("base64");
                String requestAttachDecode = new String(Base64.decode(requestsAttachItemBase, Base64.NO_WRAP));
                JSONObject result = Utils.convertToJSONObject(requestAttachDecode);
                if (result != null) {
                    return result.getJSONObject("requested_attributes").toString();
                }
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createCredentialStateObjectForExistingConnection(
            Database db,
            String extractedAttachRequest,
            JSONObject offerAttach,
            String existingConnection,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        try {
            JSONObject connection = Utils.convertToJSONObject(existingConnection);
            assert connection != null;
            JSONObject connectionData = connection.getJSONObject("data");
            Credentials.createWithOffer(UUID.randomUUID().toString(), extractedAttachRequest).handle((co, er) -> {
                if (er != null) {
                    er.printStackTrace();
                } else {
                    CredentialOffer offer = new CredentialOffer();
                    try {
                        offer.claimId = offerAttach.getString("@id");
                        offer.name = offerAttach.getString("comment");
                        offer.pwDid = connectionData.getString("pw_did");
                        JSONObject preview = offerAttach.getJSONObject("credential_preview");
                        offer.attributes = preview.getString("attributes");
                        offer.serialized = co;
                        offer.messageId = null;
                        db.credentialOffersDao().insertAll(offer);
                        liveData.postValue(REQUEST_ATTACH);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void createCredentialStateObject(
            Database db,
            String parsedInvite,
            String extractedAttachRequest,
            JSONObject offerAttach,
            Utils.ConnDataHolder userMeta,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        Credentials.createWithOffer(UUID.randomUUID().toString(), extractedAttachRequest).handle((co, er) -> {
            if (er != null) {
                er.printStackTrace();
            } else {
                CredentialOffer offer = new CredentialOffer();
                try {
                    offer.claimId = offerAttach.getString("@id");
                    offer.name = offerAttach.getString("comment");
                    offer.pwDid = null;

                    JSONObject preview = offerAttach.getJSONObject("credential_preview");
                    offer.attributes = preview.getString("attributes");

                    offer.serialized = co;
                    offer.messageId = null;

                    offer.attachConnection = parsedInvite;
                    offer.attachConnectionName = userMeta.name;
                    offer.attachConnectionLogo = userMeta.logo;

                    db.credentialOffersDao().insertAll(offer);

                    liveData.postValue(REQUEST_ATTACH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    public static void createProofStateObjectForExistingConnection(
            Database db,
            String extractedAttachRequest,
            JSONObject proofAttach,
            String existingConnection,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        try {
            JSONObject connection = Utils.convertToJSONObject(existingConnection);
            assert connection != null;
            JSONObject connectionData = connection.getJSONObject("data");
            Proofs.createWithRequest(UUID.randomUUID().toString(), extractedAttachRequest).handle((pr, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    ProofRequest proof = new ProofRequest();
                    try {
                        proof.serialized = pr;
                        proof.name = proofAttach.getString("comment");
                        proof.pwDid = connectionData.getString("pw_did");
                        proof.attributes = extractAttachmentFromProofAttach(proofAttach);
                        JSONObject thread = proofAttach.getJSONObject("~thread");
                        proof.threadId = thread.getString("thid");

                        proof.messageId = null;
                        db.proofRequestDao().insertAll(proof);

                        liveData.postValue(PROOF_ATTACH);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void createProofStateObject(
            Database db,
            String parsedInvite,
            String extractedAttachRequest,
            JSONObject proofAttach,
            Utils.ConnDataHolder userMeta,
            SingleLiveData<ConnectionCreateResult> liveData
    ) {
        Proofs.createWithRequest(UUID.randomUUID().toString(), extractedAttachRequest).handle((pr, err) -> {
            if (err != null) {
                err.printStackTrace();
            } else {
                ProofRequest proof = new ProofRequest();
                try {
                    proof.serialized = pr;
                    proof.name = proofAttach.getString("comment");
                    proof.pwDid = null;
                    proof.attributes = extractAttachmentFromProofAttach(proofAttach);
                    JSONObject thread = proofAttach.getJSONObject("~thread");
                    proof.threadId = thread.getString("thid");

                    proof.attachConnection = parsedInvite;
                    proof.attachConnectionName = userMeta.name;
                    proof.attachConnectionLogo = userMeta.logo;

                    proof.messageId = null;
                    db.proofRequestDao().insertAll(proof);

                    liveData.postValue(PROOF_ATTACH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }
}
