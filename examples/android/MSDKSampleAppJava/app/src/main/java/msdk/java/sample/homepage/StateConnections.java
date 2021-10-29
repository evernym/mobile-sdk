package msdk.java.sample.homepage;

import java.util.List;

import msdk.java.messages.ConnectionInvitation;
import msdk.java.handlers.Connections;
import msdk.java.messages.OutOfBandInvitation;
import msdk.java.sample.SingleLiveData;
import msdk.java.sample.db.Database;
import msdk.java.sample.db.entity.Action;
import msdk.java.sample.db.entity.Connection;
import msdk.java.types.MessageAttachment;

import static msdk.java.sample.homepage.Results.CONNECTION_FAILURE;
import static msdk.java.sample.homepage.Results.CONNECTION_REDIRECT;
import static msdk.java.sample.homepage.Results.CONNECTION_SUCCESS;

public class StateConnections {
    public static void handleConnectionInvitation(Action action, Database db, SingleLiveData<Results> liveData) {
        // 1. Get invitation data, type, and metadata to show on UI
        String invitation = ConnectionInvitation.getConnectionInvitationFromData(action.invite);
        ConnectionInvitation.InvitationType invitationType = ConnectionInvitation.getInvitationType(invitation);
        Connections.ConnectionMetadata userMeta = ConnectionInvitation.extractUserMetaFromInvitation(invitation);

        // 2. Get existing invitations and check if we already has correspondent connection
        List<String> serializedConnections = db.connectionDao().getAllSerializedConnections();
        String existingConnection = Connections.verifyConnectionExists(invitation, serializedConnections);

        // 3. Handle Aries Connection Invitation
        if (ConnectionInvitation.isAriesConnectionInvitation(invitationType)) {
            if (existingConnection != null) {
                // duplicates - nothing to do
                liveData.postValue(CONNECTION_REDIRECT);
            } else {
                // create a new connection
                connectionCreate(action.id, invitation, invitationType, db, userMeta, liveData);
            }
            return;
        }

        // 4. Handle Aries Out-Of-Band Connection Invitation
        if (ConnectionInvitation.isAriesOutOfBandConnectionInvitation(invitationType)) {
            MessageAttachment attachment = MessageAttachment.parse(invitation);

            if (attachment == null) {
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
                handleOutOfBandConnectionInvitationWithAttachment(
                        db,
                        invitation,
                        attachment,
                        existingConnection,
                        userMeta,
                        action,
                        liveData
                );
            }
        }
    }

    private static void handleOutOfBandConnectionInvitationWithAttachment(
            Database db,
            String invitation,
            MessageAttachment attachment,
            String existingConnection,
            Connections.ConnectionMetadata userMeta,
            Action action,
            SingleLiveData<Results> liveData
    ) {
        OutOfBandInvitation outOfBandInvite = OutOfBandInvitation.builder()
                .withInvitation(invitation)
                .withAttachment(attachment.data)
                .withExistingConnection(existingConnection)
                .withUserMeta(userMeta)
                .build();

        if (attachment.isCredentialAttachment()) {
            // handle invitation with attached credential offer
            processInvitationWithCredentialAttachment(outOfBandInvite, db, liveData, action);
        }
        if (attachment.isProofAttachment()) {
            // handle invitation with attached proof request
            processInvitationWithProofAttachment(outOfBandInvite, db, liveData, action);
        }
    }

    private static void processInvitationWithCredentialAttachment(
            OutOfBandInvitation outOfBandInvite,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            // connection exists - redirect and create credential state object
            Connections.connectionRedirectAriesOutOfBand(
                    outOfBandInvite.invitation,
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
            OutOfBandInvitation outOfBandInvite,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            // connection exists - redirect and create proof state object
            Connections.connectionRedirectAriesOutOfBand(
                    outOfBandInvite.invitation,
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
            ConnectionInvitation.InvitationType invitationType,
            Database db,
            Connections.ConnectionMetadata data,
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
