package me.connect.sdk.java.sample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.util.UUID;
import java.util.concurrent.Executors;

import me.connect.sdk.java.ConnectMeVcx;
import me.connect.sdk.java.sample.databinding.MainActivityBinding;

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
        Executors.newSingleThreadExecutor().execute(() -> {

            if (!ConnectMeVcx.configAlreadyExist(this)) {
                ConnectMeVcx.Constants constants = ConnectMeVcx.Constants.builder()
                        .withWalletName(Constants.WALLET_NAME)
                        .withPrefsName(Constants.PREFS_NAME)
                        .withSponseeId(Constants.SPONSEE_ID)
                        .withProvisionToken(Constants.PROVISION_TOKEN)
                        .withProvisionTokenRetrieved(Constants.PROVISION_TOKEN_RETRIEVED)
                        .withPlaceholderServerUrl(Constants.PLACEHOLDER_SERVER_URL)
                        .withServerUrl(Constants.SERVER_URL)
                        .build();

                try {
                    ConnectMeVcx.init(this, constants, R.raw.genesis)
                            .handleAsync((res, err) -> {
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                ConnectMeVcx.init(this).handleAsync((res, err) -> {
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
        });
    }

    private void sendToken() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        boolean tokenSent = prefs.getBoolean(Constants.FCM_TOKEN_SENT, false);
        if (tokenSent) {
            Log.d(TAG, "FCM token already sent");
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
}
