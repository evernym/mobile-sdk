package me.connect.sdk.java.sample.connections;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.Proofs;
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

    private static JSONObject decodeProofAttach(JSONObject proofAttach) {
        try {
            String requestAttachCode = proofAttach.getString("request_presentations~attach");
            JSONArray requestsAttachItems = new JSONArray(requestAttachCode);
            if (requestsAttachItems.length() == 0) {
                return null;
            }
            JSONObject requestsAttachItem = requestsAttachItems.getJSONObject(0);
            JSONObject requestsAttachItemData = requestsAttachItem.getJSONObject("data");
            String requestsAttachItemBase = requestsAttachItemData.getString("base64");
            String requestAttachDecode = new String(Base64.decode(requestsAttachItemBase, Base64.NO_WRAP));
            return Utils.convertToJSONObject(requestAttachDecode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String extractAttrFromProofAttach(JSONObject decodedProofAttach) {
        try {
            if (decodedProofAttach != null) {
                return decodedProofAttach.getJSONObject("requested_attributes").toString();
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String extractNameFromProofAttach(JSONObject decodedProofAttach) {
        try {
            if (decodedProofAttach != null) {
                return decodedProofAttach.getString("name");
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createCredentialStateObjectForExistingConnection(
            Database db,
            ConnectionsViewModel.OutOfBandInvite outOfBandInvite
    ) {
        try {
            JSONObject connection = Utils.convertToJSONObject(outOfBandInvite.existingConnection);
            assert connection != null;
            JSONObject connectionData = connection.getJSONObject("data");
            Credentials.createWithOffer(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((co, er) -> {
                if (er != null) {
                    er.printStackTrace();
                } else {
                    CredentialOffer offer = new CredentialOffer();
                    try {

                        offer.claimId = outOfBandInvite.attach.getString("@id");
                        offer.name = outOfBandInvite.attach.getString("comment");
                        offer.pwDid = connectionData.getString("pw_did");
                        JSONObject preview = outOfBandInvite.attach.getJSONObject("credential_preview");
                        offer.attributes = preview.getJSONArray("attributes").getString(0);
                        offer.serialized = co;

                        offer.attachConnectionLogo = new JSONObject(outOfBandInvite.parsedInvite)
                                .getString("profileUrl");

                        offer.messageId = null;
                        db.credentialOffersDao().insertAll(offer);
                        outOfBandInvite.liveData.postValue(REQUEST_ATTACH);
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
            ConnectionsViewModel.OutOfBandInvite outOfBandInvite
    ) {
        Credentials.createWithOffer(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((co, er) -> {
            if (er != null) {
                er.printStackTrace();
            } else {
                CredentialOffer offer = new CredentialOffer();
                try {
                    offer.claimId = outOfBandInvite.attach.getString("@id");
                    offer.name = outOfBandInvite.attach.getString("comment");
                    offer.pwDid = null;

                    JSONObject preview = outOfBandInvite.attach.getJSONObject("credential_preview");
                    offer.attributes = preview.getJSONArray("attributes").getString(0);

                    offer.serialized = co;
                    offer.messageId = null;

                    offer.attachConnection = outOfBandInvite.parsedInvite;
                    offer.attachConnectionName = outOfBandInvite.userMeta.name;
                    offer.attachConnectionLogo = outOfBandInvite.userMeta.logo;

                    db.credentialOffersDao().insertAll(offer);

                    outOfBandInvite.liveData.postValue(REQUEST_ATTACH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    public static void createProofStateObjectForExistingConnection(
            Database db,
            ConnectionsViewModel.OutOfBandInvite outOfBandInvite
    ) {
        try {
            JSONObject connection = Utils.convertToJSONObject(outOfBandInvite.existingConnection);
            assert connection != null;
            JSONObject connectionData = connection.getJSONObject("data");
            Proofs.createWithRequest(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((pr, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    ProofRequest proof = new ProofRequest();
                    try {
                        JSONObject decodedProofAttach = decodeProofAttach(outOfBandInvite.attach);

                        proof.serialized = pr;
                        proof.name = extractNameFromProofAttach(decodedProofAttach);
                        proof.pwDid = connectionData.getString("pw_did");
                        proof.attributes = extractAttrFromProofAttach(decodedProofAttach);
                        JSONObject thread = outOfBandInvite.attach.getJSONObject("~thread");
                        proof.threadId = thread.getString("thid");

                        proof.attachConnectionLogo = new JSONObject(outOfBandInvite.parsedInvite)
                                .getString("profileUrl");

                        proof.messageId = null;
                        db.proofRequestDao().insertAll(proof);

                        outOfBandInvite.liveData.postValue(PROOF_ATTACH);
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
            ConnectionsViewModel.OutOfBandInvite outOfBandInvite
    ) {
        Proofs.createWithRequest(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((pr, err) -> {
            if (err != null) {
                err.printStackTrace();
            } else {
                ProofRequest proof = new ProofRequest();
                try {
                    JSONObject decodedProofAttach = decodeProofAttach(outOfBandInvite.attach);

                    proof.serialized = pr;
                    proof.name = extractNameFromProofAttach(decodedProofAttach);
                    proof.pwDid = null;
                    proof.attributes = extractAttrFromProofAttach(decodedProofAttach);
                    JSONObject thread = outOfBandInvite.attach.getJSONObject("~thread");
                    proof.threadId = thread.getString("thid");

                    proof.attachConnection = outOfBandInvite.parsedInvite;
                    proof.attachConnectionName = outOfBandInvite.userMeta.name;
                    proof.attachConnectionLogo = outOfBandInvite.userMeta.logo;

                    proof.messageId = null;
                    db.proofRequestDao().insertAll(proof);

                    outOfBandInvite.liveData.postValue(PROOF_ATTACH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }
}
