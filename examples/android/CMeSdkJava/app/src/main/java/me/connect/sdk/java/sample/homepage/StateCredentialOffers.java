package me.connect.sdk.java.sample.homepage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.Executors;

import me.connect.sdk.java.Connections;
import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.OutOfBandHelper;
import me.connect.sdk.java.Proofs;
import me.connect.sdk.java.Utils;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Action;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;
import me.connect.sdk.java.sample.db.entity.ProofRequest;

import static me.connect.sdk.java.sample.homepage.Results.CONNECTION_FAILURE;
import static me.connect.sdk.java.sample.homepage.Results.CONNECTION_SUCCESS;
import static me.connect.sdk.java.sample.homepage.Results.OFFER_FAILURE;
import static me.connect.sdk.java.sample.homepage.Results.OFFER_SUCCESS;
import static me.connect.sdk.java.sample.homepage.Results.PROOF_FAILURE;
import static me.connect.sdk.java.sample.homepage.Results.PROOF_SUCCESS;

public class StateCredentialOffers {
    public static void createCredentialStateObjectForExistingConnection(
            Database db,
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        try {
            JSONObject connection = Utils.convertToJSONObject(outOfBandInvite.existingConnection);
            assert connection != null;
            JSONObject connectionData = connection.getJSONObject("data");
            me.connect.sdk.java.Credentials.createWithOffer(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((co, er) -> {
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

                        acceptCredentialOffer(offer, db, liveData, action);
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
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            SingleLiveData<Results> liveData,
            Action action
    ) {

        me.connect.sdk.java.Credentials.createWithOffer(UUID.randomUUID().toString(), outOfBandInvite.extractedAttachRequest).handle((co, er) -> {
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

                    acceptCredentialOffer(offer, db, liveData, action);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    public static void acceptCredentialOffer(
            CredentialOffer offer,
            Database db,
            SingleLiveData<Results> data,
            Action action
    ) {
        if (offer.attachConnection != null) {
            acceptCredentialOfferAndCreateConnection(offer, db, data, action);
            return;
        }
        Connection connection = db.connectionDao().getByPwDid(offer.pwDid);
        me.connect.sdk.java.Credentials.acceptOffer(connection.serialized, offer.serialized).handle((s, throwable) -> {
                if (s != null) {
                    offer.serialized = me.connect.sdk.java.Credentials.awaitStatusChange(s, MessageState.ACCEPTED);
                    offer.accepted = true;
                    db.credentialOffersDao().update(offer);
                }
                HomePageViewModel.addToHistory(
                    action.id,
                    "Credential accept",
                    db,
                    data
                );

                data.postValue(throwable == null ? OFFER_SUCCESS: OFFER_SUCCESS);
                return null;
            }
        );
    }

    private static void acceptCredentialOfferAndCreateConnection(
            CredentialOffer offer,
            Database db,
            SingleLiveData<Results> data,
            Action action
    ) {
        Connections.create(offer.attachConnection, new QRConnection())
                .handle((res, throwable) -> {
                    if (res != null) {
                        String serializedCon = Connections.awaitStatusChange(res, MessageState.ACCEPTED);

                        String pwDid = Connections.getPwDid(serializedCon);
                        Connection c = new Connection();
                        c.name = offer.attachConnectionName;
                        c.icon = offer.attachConnectionLogo;
                        c.pwDid = pwDid;
                        c.serialized = serializedCon;
                        db.connectionDao().insertAll(c);
                        data.postValue(throwable == null ? CONNECTION_SUCCESS : CONNECTION_FAILURE);

                        HomePageViewModel.addHistoryAction(
                            db,
                            offer.attachConnectionName,
                            "Connection created",
                            offer.attachConnectionLogo,
                            data
                        );

                        me.connect.sdk.java.Credentials.acceptOffer(serializedCon, offer.serialized).handle((s, thr) -> {
                                if (s != null) {
                                    offer.serialized = me.connect.sdk.java.Credentials.awaitStatusChange(s, MessageState.ACCEPTED);
                                    offer.pwDid = pwDid;
                                    offer.accepted = true;
                                    db.credentialOffersDao().update(offer);
                                }

                                HomePageViewModel.addToHistory(
                                    action.id,
                                    "Credential accept",
                                    db,
                                    data
                                );

                                data.postValue(thr == null ? OFFER_SUCCESS: OFFER_FAILURE);
                                return null;
                            }
                        );
                    }
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                    return null;
                });
    }

    public static void rejectCredentialOffer(CredentialOffer offer, Database db, SingleLiveData<Results> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (offer.pwDid == null) {
                offer.accepted = false;
                db.credentialOffersDao().update(offer);
                liveData.postValue(PROOF_SUCCESS);
                return;
            }
            Connection con = db.connectionDao().getByPwDid(offer.pwDid);
            Credentials.rejectOffer(con.serialized, offer.serialized).handle((s, err) -> {
                if (s != null) {
                    String serializedProof = Credentials.awaitStatusChangeForOffer(s, MessageState.REJECTED);
                    offer.serialized = serializedProof;
                    offer.accepted = false;
                    db.credentialOffersDao().update(offer);
                }
                liveData.postValue(err == null ? PROOF_SUCCESS : PROOF_FAILURE);
                return null;
            });
        });
    }
}
