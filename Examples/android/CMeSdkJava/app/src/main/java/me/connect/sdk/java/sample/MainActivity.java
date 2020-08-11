package me.connect.sdk.java.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import java9.util.concurrent.CompletableFuture;

import me.connect.sdk.java.AgencyConfig;
import me.connect.sdk.java.ConnectMeVcx;
import me.connect.sdk.java.Connections;
import me.connect.sdk.java.Credentials;
import me.connect.sdk.java.Messages;
import me.connect.sdk.java.PoolTxnGenesis;
import me.connect.sdk.java.Proofs;
import me.connect.sdk.java.StructuredMessages;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.message.StructuredMessageHolder;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init the sdkApi
        ConnectMeVcx.Config config = ConnectMeVcx.Config.builder()
                .withContext(this)
                .withGenesisPool(PoolTxnGenesis.POOL_TXN_GENESIS_PROD)
                .withAgency(AgencyConfig.DEFAULT)
                .withWalletName("some-wallet-name")
                .build();

        ConnectMeVcx.init(config).handle((aVoid, throwable) -> {
            Log.i(TAG, "Init finished, res: " + aVoid + ", err: " + throwable);
            Toast.makeText(MainActivity.this, "Init finished", Toast.LENGTH_LONG).show();
            return null;
        });
    }

    public void acceptOnClick(View v) {
        EditText editTextConn = (EditText) findViewById(R.id.editTextConn);
        String serializedConn = editTextConn.getText().toString();
        try {
            List<String> offers = Credentials.getOffers(serializedConn).get();
            for (String offer : offers) {
                try {
                    String co = Credentials.createWithOffer(serializedConn, UUID.randomUUID().toString(), offer).get();
                    Log.i(TAG, "Credential offer: " + co);
                    String co2 = Credentials.acceptOffer(serializedConn, co).get();
                    Log.i(TAG, "Credential after accepting: " + co2);
                    Credentials.awaitStatusChange(co, MessageState.ACCEPTED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Accept failed with exception, " + e);
            e.printStackTrace();
        }
    }

    public void proofOnClick(View view) {
        EditText editTextConn = (EditText) findViewById(R.id.editTextConn);
        String serializedConn = editTextConn.getText().toString();
        try {
            List<String> proofReqs = Proofs.getRequests(serializedConn).get();
            for (String proof : proofReqs) {
                try {
                    String serializedProof = Proofs.createWithRequest(UUID.randomUUID().toString(), proof).get();
                    Log.i(TAG, "Proof request created: " + serializedProof);
                    String availableCreds = Proofs.retrieveAvailableCredentials(serializedProof).get();
                    Log.i(TAG, "Available creds retrieved: " + availableCreds);
                    String mappedCreds = Proofs.mapCredentials(availableCreds);
                    String res = Proofs.send(serializedConn, serializedProof, mappedCreds, "{}").get();
                    Log.i(TAG, "Proof request sent: " + res);
                    Proofs.awaitStatusChange(res, MessageState.ACCEPTED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Proof request failed with exception, " + e);
            e.printStackTrace();
        }
    }

    public void rejectProofOnClick(View view) {
        EditText editTextConn = (EditText) findViewById(R.id.editTextConn);
        String serializedConn = editTextConn.getText().toString();
        try {
            List<String> proofReqs = Proofs.getRequests(serializedConn).get();
            for (String proof : proofReqs) {
                try {
                    String res = Proofs.reject(serializedConn, proof).get();
                    Log.i(TAG, "Proof rejected: " + res);
                    Proofs.awaitStatusChange(res, MessageState.REJECTED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Reject failed with exception, " + e);
            e.printStackTrace();
        }
    }

    public void questionOnClick(View v) {
        EditText editTextConn = (EditText) findViewById(R.id.editTextConn);
        String serializedConn = editTextConn.getText().toString();
        try {
            List<Message> messages = Messages.getPendingMessages(serializedConn, MessageType.QUESTION).get();
            for (Message m : messages) {
                try {
                    Log.i(TAG, "Question received: " + m.getPayload());
                    StructuredMessageHolder sm = StructuredMessages.extract(m);
                    StructuredMessageHolder.Response resp = sm.getResponses().get(0);
                    String res = StructuredMessages.answer(serializedConn, m.getUid(), resp.getNonce()).get();
                    Log.i(TAG, "Structured message response: " + res);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Accept failed with exception, " + e);
            e.printStackTrace();
        }
    }

    public void addConnectionOnClick(View v) {
        EditText editText = (EditText) findViewById(R.id.editText2);
        EditText editTextConn = (EditText) findViewById(R.id.editTextConn);
        String invitationDetails = editText.getText().toString();
        Log.d(TAG, "connection invitation is set to: " + invitationDetails);

        CompletableFuture<String> result = Connections.create(invitationDetails, new QRConnection());
        try {
            String serializedConnection = result.get();
            Log.i(TAG, "Established connection: " + serializedConnection);
            editTextConn.setText(serializedConnection);

        } catch (Exception e) {
            Log.e("TAG", "Connection creation failed with exception, " + e);
            e.printStackTrace();
        }
    }
}
