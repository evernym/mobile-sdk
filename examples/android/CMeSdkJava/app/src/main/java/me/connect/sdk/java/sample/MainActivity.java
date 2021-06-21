package me.connect.sdk.java.sample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

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
                firstTimeRun();
            } else {
                usuallyTimeRun();
            }
        });
    }

    private void firstTimeRun() {
        try {
            ConnectMeVcx.Constants constants = buildConstants();
            ConnectMeVcx.createOneTimeInfo(this, constants, R.raw.genesis)
                .whenComplete((res, ex) -> {
                    String message;
                    if (ex != null) {
                        message = "SDK was not initialized!";
                        Log.e(TAG, "Sdk not initialized: ", ex);
                    } else {
                        message = "SDK initialized successfully.";
                        ConnectMeVcx.sendToken(
                                this,
                                Constants.PREFS_NAME,
                                Constants.FCM_TOKEN,
                                Constants.FCM_TOKEN_SENT
                        );
                    }
                    runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
                });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ConnectMeVcx.Constants buildConstants() {
        return ConnectMeVcx.Constants.builder()
            .withWalletName(Constants.WALLET_NAME)
            .withPrefsName(Constants.PREFS_NAME)
            .withSponseeId(Constants.SPONSEE_ID)
            .withProvisionToken(Constants.PROVISION_TOKEN)
            .withProvisionTokenRetrieved(Constants.PROVISION_TOKEN_RETRIEVED)
            .withPlaceholderServerUrl(Constants.PLACEHOLDER_SERVER_URL)
            .withServerUrl(Constants.SERVER_URL)
            .build();
    }

    private void usuallyTimeRun() {
        ConnectMeVcx.init(this, R.raw.genesis).handleAsync((res, err) -> {
            String message;
            if (err == null) {
                message = "SDK initialized successfully.";
                ConnectMeVcx.sendToken(
                        this,
                        Constants.PREFS_NAME,
                        Constants.FCM_TOKEN,
                        Constants.FCM_TOKEN_SENT
                );
            } else {
                message = "SDK was not initialized!";
                Log.e(TAG, "Sdk not initialized: ", err);
            }
            runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
            return null;
        });
    }
}
