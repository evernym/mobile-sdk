package me.connect.sdk.java.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.UUID;

import me.connect.sdk.java.AgencyConfig;
import me.connect.sdk.java.ConnectMeVcx;
import me.connect.sdk.java.SecurePreferencesHelper;
import me.connect.sdk.java.sample.databinding.MainActivityBinding;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.brightinventions.slf4android.LogLevel;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";


    private MainActivityBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewBinding.pager.setAdapter(adapter);
        viewBinding.tabLayout.setupWithViewPager(viewBinding.pager);

        initSdk();
    }

    private void initSdk() {
        Toast.makeText(this, "Started SDK initialization", Toast.LENGTH_SHORT).show();
        String token = null;
        if (!provisionTokenRetrieved()) {
            try {
                token = retrieveToken();
            } catch (Exception ex) {
                Log.e(TAG, "Failed to retrieve token: ", ex);
            }
        } else {
            token = getSavedToken();
        }
        // Todo retry policy if token not retrieved
        ConnectMeVcx.Config config = ConnectMeVcx.Config.builder()
                .withAgency(AgencyConfig.DEFAULT)
                .withGenesisPool(R.raw.genesis)
                .withWalletName(Constants.WALLET_NAME)
                .withLogLevel(LogLevel.DEBUG)
                .withContext(this)
                .withProvisionToken(token)
                .build();

        // Todo progress bar could be added
        ConnectMeVcx.init(config).handleAsync((res, err) -> {
            String message;
            if (err == null) {
                message = "SDK initialized successfully.";
                sendToken();
            } else {
                message = "SDK was not initialized!";
                Log.e(TAG, "Sdk not initialized: ", err);
            }
            runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
            return null;
        });
    }

    private void sendToken() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        boolean tokenSent = prefs.getBoolean(Constants.FCM_TOKEN_SENT, false);
        if (tokenSent) {
            return;
        }
        String token = prefs.getString(Constants.FCM_TOKEN, null);
        if (token != null) {
            ConnectMeVcx.updateAgentInfo(UUID.randomUUID().toString(), token).whenComplete((res, err) -> {
                if (err == null) {
                    Log.d(TAG, "FCM token updated successfully");
                    prefs.edit()
                            .putBoolean(Constants.FCM_TOKEN_SENT, true)
                            .apply();
                } else {
                    Log.e(TAG, "FCM token was not updated: ", err);
                }
            });
        }
    }

    private boolean provisionTokenRetrieved() {
        return getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .getBoolean(Constants.PROVISION_TOKEN_RETRIEVED, false);
    }

    private String getSavedToken() {
        return getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                .getString(Constants.PROVISION_TOKEN, null);
    }

    private String retrieveToken() throws Exception {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        String sponseeId = prefs.getString(Constants.SPONSEE_ID, null);
        if (sponseeId == null) {
            sponseeId = UUID.randomUUID().toString();
            prefs.edit()
                    .putString(Constants.SPONSEE_ID, sponseeId)
                    .apply();
        }
        JSONObject json = new JSONObject();
        json.put("sponseeId", sponseeId);

        //todo make standalone method
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(Constants.SERVER_URL)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new Exception("Response failed with code " + response.code());
        }
        String token = response.body().string();
        Log.d(TAG, "Retrieved token: "+ token);
        /* Todo uncomment this when server is registered
        prefs.edit()
                .putString(Constants.PROVISION_TOKEN, token)
                .putBoolean(Constants.PROVISION_TOKEN_RETRIEVED, true)
                .apply();

        return token;
        */
        return null;
    }

}
