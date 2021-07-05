package me.connect.sdk.java.sample.homepage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import me.connect.sdk.java.Connections;
import me.connect.sdk.java.ConnectionsUtils;
import me.connect.sdk.java.Logger;
import me.connect.sdk.java.OutOfBandHelper;
import me.connect.sdk.java.Utils;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Action;
import me.connect.sdk.java.sample.db.entity.Connection;

import static me.connect.sdk.java.sample.homepage.Results.CONNECTION_FAILURE;
import static me.connect.sdk.java.sample.homepage.Results.CONNECTION_REDIRECT;
import static me.connect.sdk.java.sample.homepage.Results.CONNECTION_SUCCESS;

public class StateConnections {
    public static void handleConnectionInvitation(Action action, Database db, SingleLiveData<Results> liveData) {
        // get invitation data, type, and meta to show
        String invitation = ConnectionsUtils.getInvitation(action.invite);
        Connections.InvitationType invitationType = Connections.getInvitationType(invitation);
        ConnectionsUtils.ConnDataHolder userMeta = ConnectionsUtils.extractUserMetaFromInvitation(invitation);

        // get existing invitations and check if we already has correspondent connection
        List<String> serializedConnections = db.connectionDao().getAllSerializedConnections();
        String existingConnection = Connections.verifyConnectionExists(invitation, serializedConnections);

        if (ConnectionsUtils.isAriesConnection(invitationType)) {
            // aries connection
            if (existingConnection != null) {
                // duplicates - nothing to do
                liveData.postValue(CONNECTION_REDIRECT);
            } else {
                // create a new connection
                connectionCreate(action.id, invitation, invitationType, db, userMeta, liveData);
            }
            return;
        }
        if (ConnectionsUtils.isOutOfBandType(invitationType)) {
            // aries out-of-band connection
            String extractedAttachRequest = OutOfBandHelper.extractRequestAttach(invitation);
            JSONObject attachRequestObject = Utils.convertToJSONObject(extractedAttachRequest);
            if (attachRequestObject == null) {
                // no attachment in the invitation
                if (existingConnection != null) {
                    // reuse existing connection
                    Connections.connectionRedirectAriesOutOfBand(invitation, existingConnection)
                            .whenComplete((sc, err) -> {
                                liveData.postValue(CONNECTION_REDIRECT);
                            });
                } else {
                    // create a new connection
                    connectionCreate(action.id, invitation, invitationType, db, userMeta, liveData);
                }
            } else {
                // handle invitation with attachment
                processInvitationWithAttachment(
                        db,
                        invitation,
                        extractedAttachRequest,
                        attachRequestObject,
                        existingConnection,
                        userMeta,
                        action,
                        liveData
                );
            }
        }
    }

    private static void processInvitationWithAttachment(
            Database db,
            String parsedInvite,
            String extractedAttachRequest,
            JSONObject attachRequestObject,
            String existingConnection,
            ConnectionsUtils.ConnDataHolder userMeta,
            Action action,
            SingleLiveData<Results> liveData
    ) {
        try {
            OutOfBandHelper.OutOfBandInvite outOfBandInvite = OutOfBandHelper.OutOfBandInvite.builder()
                    .withParsedInvite(parsedInvite)
                    .withExtractedAttachRequest(extractedAttachRequest)
                    .withAttach(attachRequestObject)
                    .withExistingConnection(existingConnection)
                    .withUserMeta(userMeta)
                    .build();

            String attachType = attachRequestObject.getString("@type");
            if (ConnectionsUtils.isCredentialInviteType(attachType)) {
                // handle invitation with attached credential offer
                processInvitationWithCredentialAttachment(outOfBandInvite, db, liveData, action);
                return;
            }
            if (ConnectionsUtils.isProofInviteType(attachType)) {
                // handle invitation with attached proof request
                processInvitationWithProofAttachment(outOfBandInvite, db, liveData, action);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void processInvitationWithCredentialAttachment(
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            // connection exists - redirect and create credential state object
            Connections.connectionRedirectAriesOutOfBand(
                    outOfBandInvite.parsedInvite,
                    outOfBandInvite.existingConnection
            ).whenComplete((sc, err) -> {
                liveData.postValue(CONNECTION_REDIRECT);
                StateCredentialOffers.createCredentialStateObject(
                        db,
                        outOfBandInvite,
                        liveData,
                        action
                );
            });
        } else {
            // create credential state object
            StateCredentialOffers.createCredentialStateObject(
                    db,
                    outOfBandInvite,
                    liveData,
                    action
            );
        }
    }

    private static void processInvitationWithProofAttachment(
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            // connection exists - redirect and create proof state object
            Connections.connectionRedirectAriesOutOfBand(
                    outOfBandInvite.parsedInvite,
                    outOfBandInvite.existingConnection
            ).whenComplete((sc, err) -> {
                StateProofRequests.createProofStateObject(
                        db,
                        outOfBandInvite,
                        liveData,
                        action
                );
                liveData.postValue(CONNECTION_REDIRECT);
            });
        } else {
            // create proof state object
            StateProofRequests.createProofStateObject(db, outOfBandInvite, liveData, action);
        }
    }

    public static void connectionCreate(
            int actionId,
            String parsedInvite,
            Connections.InvitationType invitationType,
            Database db,
            ConnectionsUtils.ConnDataHolder data,
            SingleLiveData<Results> liveData
    ) {
        Connections.create(parsedInvite, invitationType)
                .handle((serialized, throwable) -> {
                    if (serialized != null) {
                        String pwDid = Connections.getPwDid(serialized);
                        serialized = Connections.awaitConnectionCompleted(serialized, pwDid);

                        Connection c = new Connection();
                        c.pwDid = pwDid;
                        c.icon = data.logo;
                        c.name = data.name;
                        c.serialized = serialized;
                        c.invitation = parsedInvite;
                        db.connectionDao().insertAll(c);
                    }

                    HomePageViewModel.addToHistory(
                            actionId,
                            "Connection created",
                            db,
                            liveData
                    );

                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                    liveData.postValue(throwable == null ? CONNECTION_SUCCESS : CONNECTION_FAILURE);
                    return serialized;
                });
    }
}
