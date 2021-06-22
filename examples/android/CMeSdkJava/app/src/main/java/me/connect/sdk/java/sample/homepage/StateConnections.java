package me.connect.sdk.java.sample.homepage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import me.connect.sdk.java.Connections;
import me.connect.sdk.java.ConnectionsUtils;
import me.connect.sdk.java.OutOfBandHelper;
import me.connect.sdk.java.Utils;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Action;
import me.connect.sdk.java.sample.db.entity.Connection;

import static me.connect.sdk.java.sample.homepage.Results.CONNECTION_FAILURE;
import static me.connect.sdk.java.sample.homepage.Results.CONNECTION_REDIRECT;
import static me.connect.sdk.java.sample.homepage.Results.CONNECTION_SUCCESS;

public class StateConnections {
    public static void createConnection(Action action, Database db, SingleLiveData<Results> liveData) {
        String parsedInvite = ConnectionsUtils.parseInvite(action.invite);
        Connections.InvitationType invitationType = Connections.getInvitationType(parsedInvite);
        ConnectionsUtils.ConnDataHolder userMeta = ConnectionsUtils.extractUserMetaFromInvite(parsedInvite);
        List<String> serializedConns = db.connectionDao().getAllSerializedConnections();
        String existingConnection = Connections.verifyConnectionExists(parsedInvite, serializedConns);
        if (ConnectionsUtils.isProprietaryType(invitationType)) {
            if (existingConnection != null) {
                Connections.connectionRedirectProprietary(action.invite, existingConnection);
                liveData.postValue(CONNECTION_SUCCESS);
            } else {
                connectionCreate(parsedInvite, db, userMeta, liveData);
            }
            return;
        }
        if (ConnectionsUtils.isAriesConnection(invitationType)) {
            if (existingConnection != null) {
                liveData.postValue(CONNECTION_REDIRECT);
                return;
            } else {
                connectionCreate(parsedInvite, db, userMeta, liveData);
            }
            return;
        }
        if (ConnectionsUtils.isOutOfBandType(invitationType)) {
            String extractedAttachRequest = OutOfBandHelper.extractRequestAttach(parsedInvite);
            JSONObject attachRequestObject = Utils.convertToJSONObject(extractedAttachRequest);
            if (attachRequestObject == null) {
                if (existingConnection != null) {
                    Connections.connectionRedirectAriesOutOfBand(parsedInvite, existingConnection);
                    liveData.postValue(CONNECTION_REDIRECT);
                } else {
                    connectionCreate(parsedInvite, db, userMeta, liveData);
                }
                return;
            }
            processAttachment(
                    db,
                    parsedInvite,
                    extractedAttachRequest,
                    attachRequestObject,
                    existingConnection,
                    userMeta,
                    action,
                    liveData
            );
        }
    }

    private static void processAttachment(
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
                processCredentialAttachment(outOfBandInvite, db, liveData, action);
                return;
            }
            if (ConnectionsUtils.isProofInviteType(attachType)) {
                processProofAttachment(outOfBandInvite, db, liveData, action);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void processCredentialAttachment(
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            Connections.connectionRedirectAriesOutOfBand(
                    outOfBandInvite.parsedInvite,
                    outOfBandInvite.existingConnection
            );
            StateCredentialOffers.createCredentialStateObjectForExistingConnection(
                    db,
                    outOfBandInvite,
                    liveData,
                    action
            );
            liveData.postValue(CONNECTION_REDIRECT);
            return;
        }
        StateCredentialOffers.createCredentialStateObject(
                db,
                outOfBandInvite,
                liveData,
                action
        );
    }

    private static void processProofAttachment(
            OutOfBandHelper.OutOfBandInvite outOfBandInvite,
            Database db,
            SingleLiveData<Results> liveData,
            Action action
    ) {
        if (outOfBandInvite.existingConnection != null) {
            StateProofRequests.createProofStateObjectForExistingConnection(
                    db,
                    outOfBandInvite,
                    liveData,
                    action
            );
            liveData.postValue(CONNECTION_REDIRECT);
            return;
        }
        StateProofRequests.createProofStateObject(db, outOfBandInvite, liveData, action);
    }


    public static void connectionCreate(
            String parsedInvite,
            Database db,
            ConnectionsUtils.ConnDataHolder data,
            SingleLiveData<Results> liveData
    ) {
        Connections.create(parsedInvite, new QRConnection())
                .handle((res, throwable) -> {
                    if (res != null) {
                        String pwDid = Connections.getPwDid(res);

                        String serializedCon = Connections.awaitConnectionReceived(res, pwDid);

                        pwDid = Connections.getPwDid(serializedCon);
                        Connection c = new Connection();
                        c.name = data.name;
                        c.icon = data.logo;
                        c.pwDid = pwDid;
                        c.serialized = serializedCon;
                        db.connectionDao().insertAll(c);
                    }
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                    liveData.postValue(throwable == null ? CONNECTION_SUCCESS : CONNECTION_FAILURE);
                    return res;
                });
    }
}
