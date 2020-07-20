package me.connect.sdk.java.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.List;
import java.util.UUID;

import java9.util.concurrent.CompletableFuture;

import me.connect.sdk.java.AgencyConfig;
import me.connect.sdk.java.ConnectMeVcx;
import me.connect.sdk.java.ConnectMeVcxUpdated;
import me.connect.sdk.java.PoolTxnGenesis;
import me.connect.sdk.java.connection.QRConnection;
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.message.StructuredMessage;
import me.connect.sdk.java.proof.ProofHolder;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";

    private boolean sdkInited = false;
    private ConnectMeVcx sdkApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init the sdkApi
        sdkApi = ConnectMeVcx.builder()
                .withContext(this)
                .withGenesisPool(PoolTxnGenesis.POOL_TXN_GENESIS_PROD)
                .withAgency(AgencyConfig.DEFAULT)
                .build();

        sdkApi.init();
    }

    public void acceptOnClick(View v) {
        EditText editTextConn = (EditText) findViewById(R.id.editTextConn);
        String serializedConn = editTextConn.getText().toString();
        try {
            List<String> offers = ConnectMeVcxUpdated.getCredentialOffers(serializedConn).get();
            for (String offer : offers) {
                try {
                    String co = ConnectMeVcxUpdated.createCredentialWithOffer(serializedConn, UUID.randomUUID().toString(), offer).get();
                    Log.i(TAG, "Credential offer: " + co);
                    String co2 = ConnectMeVcxUpdated.acceptCredentialOffer(serializedConn, co).get();
                    Log.i(TAG, "Credential after accepting: " + co2);
                    ConnectMeVcxUpdated.awaitCredentialStatusChange(co);
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
            List<String> proofReqs = ConnectMeVcxUpdated.getProofRequests(serializedConn).get();
            for (String proof : proofReqs) {
                try {
                    String serializedProof = ConnectMeVcxUpdated.createProofRequest(UUID.randomUUID().toString(), proof).get();
                    Log.i(TAG, "Proof request created: " + serializedProof);
                    String availableCreds = ConnectMeVcxUpdated.retrieveCredentialsForProof(serializedProof).get();
                    Log.i(TAG, "Available creds retrieved: " + availableCreds);
                    String mappedCreds = ConnectMeVcxUpdated.mapCredentials(availableCreds);
                    String res = ConnectMeVcxUpdated.sendProof(serializedConn, serializedProof, mappedCreds, "{}").get();
                    Log.i(TAG, "Proof request sent: " + res);
                    ConnectMeVcxUpdated.awaitProofStatusChange(res);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Proof request failed with exception, " + e);
            e.printStackTrace();
        }
    }

    public void questionOnClick(View v) {
        EditText editTextConn = (EditText) findViewById(R.id.editTextConn);
        String serializedConn = editTextConn.getText().toString();
        try {
            List<String> questions = ConnectMeVcxUpdated.getPendingMessages(serializedConn, MessageType.QUESTION).get();
            for (String question : questions) {
                try {
                    Log.i(TAG, "Question received: " + question);
                    StructuredMessage sm = ConnectMeVcxUpdated.extractStructuredMessage(question);
                    StructuredMessage.Response resp = sm.getResponses().get(0);
                    String res = ConnectMeVcxUpdated.answerStructuredMessage(serializedConn, sm.getMessageId(), resp.getNonce()).get();
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

        CompletableFuture<String> result = ConnectMeVcxUpdated.createConnection(invitationDetails, new QRConnection());
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
