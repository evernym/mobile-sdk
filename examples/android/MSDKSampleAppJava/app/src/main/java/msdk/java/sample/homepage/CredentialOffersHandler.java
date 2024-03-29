package msdk.java.sample.homepage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.Executors;

import msdk.java.messages.ConnectionInvitation;
import msdk.java.handlers.Connections;
import msdk.java.handlers.Credentials;
import msdk.java.messages.CredentialOfferMessage;
import msdk.java.messages.OutOfBandInvitation;
import msdk.java.sample.SingleLiveData;
import msdk.java.sample.db.Database;
import msdk.java.sample.db.entity.Action;
import msdk.java.sample.db.entity.Connection;
import msdk.java.sample.db.entity.CredentialOffer;
import msdk.java.sample.history.HistoryHandler;
import msdk.java.sample.history.HistoryViewModel;

import static msdk.java.sample.homepage.Results.CONNECTION_FAILURE;
import static msdk.java.sample.homepage.Results.CONNECTION_SUCCESS;
import static msdk.java.sample.homepage.Results.OFFER_FAILURE;
import static msdk.java.sample.homepage.Results.OFFER_SUCCESS;
import static msdk.java.sample.homepage.Results.PROOF_FAILURE;
import static msdk.java.sample.homepage.Results.PROOF_SUCCESS;

public class CredentialOffersHandler {
    public static void createCredentialStateObject(
            Database db,
            OutOfBandInvitation outOfBandInvite,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        CredentialOfferMessage credentialOffer = CredentialOfferMessage.parse(outOfBandInvite.attachment.toString());

        String pwDid = null;
        if (outOfBandInvite.existingConnection != null) {
            pwDid = Connections.getPwDid(outOfBandInvite.existingConnection);
        }
        String finalPwDid = pwDid;

        Credentials.createWithOffer(UUID.randomUUID().toString(), outOfBandInvite.attachment.toString()).handle((serialized, er) -> {
            if (er != null) {
                er.printStackTrace();
            } else {
                CredentialOffer offer = new CredentialOffer();
                offer.threadId = credentialOffer.threadId;
                offer.pwDid = finalPwDid;
                offer.serialized = serialized;
                offer.attachConnection = outOfBandInvite.invitation;
                offer.attachConnectionLogo = outOfBandInvite.userMeta.logo;
                offer.attachConnectionName = outOfBandInvite.userMeta.name;
                db.credentialOffersDao().insertAll(offer);

                acceptCredentialOffer(offer, db, liveData, action);
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
        Executors.newSingleThreadExecutor().execute(() -> {
            if (offer.pwDid == null) {
                acceptCredentialOfferAndCreateConnection(offer, db, data, action);
            } else {
                Connection connection = db.connectionDao().getByPwDid(offer.pwDid);
                acceptCredentialOffer(offer, connection, db, data, action);
            }
        });
    }

    public static void acceptCredentialOffer(
            CredentialOffer offer,
            Connection connection,
            Database db,
            SingleLiveData<Results> data,
            Action action
    ) {
        Credentials.acceptOffer(connection.serialized, offer.serialized).handle((s, throwable) -> {
            if (s != null) {
                offer.serialized = Credentials.awaitCredentialReceived(s, offer.threadId, offer.pwDid);
                db.credentialOffersDao().update(offer);
                HistoryHandler.addToHistory(
                        action.id,
                        "Credential accept",
                        db,
                        data
                );
            } else {
                HistoryHandler.addToHistory(
                        action.id,
                        "Credential accept failure",
                        db,
                        data
                );
            }
            data.postValue(throwable == null ? OFFER_SUCCESS: OFFER_FAILURE);
            return null;
        });
    }

    private static void acceptCredentialOfferAndCreateConnection(
            CredentialOffer offer,
            Database db,
            SingleLiveData<Results> data,
            Action action
    ) {
        Connections.create(offer.attachConnection, ConnectionInvitation.InvitationType.OutOfBand)
        .handle((res, throwable) -> {
            if (res != null) {
                String pwDid = Connections.getPwDid(res);
                String serializedCon = Connections.awaitConnectionCompleted(res, pwDid);

                Connection connection = new Connection();
                connection.icon = offer.attachConnectionLogo;
                connection.pwDid = pwDid;
                connection.serialized = serializedCon;
                db.connectionDao().insertAll(connection);
                data.postValue(throwable == null ? CONNECTION_SUCCESS : CONNECTION_FAILURE);

                offer.pwDid = pwDid;
                db.credentialOffersDao().update(offer);

                HistoryHandler.addHistoryAction(
                    db,
                    offer.attachConnectionName,
                    "Connection created",
                    offer.attachConnectionLogo,
                    data
                );
                acceptCredentialOffer(offer, connection, db, data, action);
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
                liveData.postValue(PROOF_SUCCESS);
                return;
            }
            Connection con = db.connectionDao().getByPwDid(offer.pwDid);
            Credentials.rejectOffer(con.serialized, offer.serialized).handle((s, err) -> {
                if (s != null) {
                    offer.serialized = s;
                    db.credentialOffersDao().update(offer);
                }
                liveData.postValue(err == null ? PROOF_SUCCESS : PROOF_FAILURE);
                return null;
            });
        });
    }
}
