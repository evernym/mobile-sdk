package me.connect.sdk.java.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

import me.connect.sdk.java.AgencyConfig;
import me.connect.sdk.java.ConnectMeVcx;
import me.connect.sdk.java.sample.databinding.MainActivityBinding;
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

        ConnectMeVcx.Config config = ConnectMeVcx.Config.builder()
                .withAgency(AgencyConfig.DEFAULT)
                .withGenesisPool(R.raw.genesis)
                .withWalletName(Constants.WALLET_NAME)
                .withLogLevel(LogLevel.DEBUG)
                .withContext(this)
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
}
