package me.connect.sdk.java.sample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.util.concurrent.Executors;

import me.connect.sdk.java.Initialization;
import me.connect.sdk.java.Logger;
import me.connect.sdk.java.Utils;
import me.connect.sdk.java.sample.databinding.MainActivityBinding;

public class MainActivity extends AppCompatActivity {
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
            if (!Initialization.isCloudAgentProvisioned(this)) {
                firstTimeRun();
            } else {
                regularRun();
            }
        });
    }

    private void firstTimeRun() {
        try {
            Initialization.Constants constants = buildConstants();

            // 1. Configure storage permissions
            Utils.configureStoragePermissions(this);
            // 2. Configure Logger
            Logger.configureLogger(this);
            // 3. Start provisioning
            Initialization.provisionCloudAgentAndInitializeSdk(this, constants, R.raw.genesis)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        runOnUiThread(() -> Toast.makeText(this, "SDK was not initialized!", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "SDK initialized successfully.", Toast.LENGTH_SHORT).show());
                    }
                });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void regularRun() {
        // 1. Configure Logger
        Logger.configureLogger(this);
        // 2. Start initialization
        Initialization.initializeSdk(this, R.raw.genesis).handleAsync((res, err) -> {
            if (err != null) {
                runOnUiThread(() -> Toast.makeText(this, "SDK was not initialized!", Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() -> Toast.makeText(this, "SDK initialized successfully.", Toast.LENGTH_SHORT).show());
            }
            return null;
        });
    }

    private Initialization.Constants buildConstants() {
        return Initialization.Constants.builder()
                .withAgencyEndpoint(Constants.AGENCY_ENDPOINT)
                .withAgencyDid(Constants.AGENCY_DID)
                .withAgencyVerkey(Constants.AGENCY_VERKEY)
                .withWalletName(Constants.WALLET_NAME)
                .withPrefsName(Constants.PREFS_NAME)
                .withSponseeId(Constants.SPONSEE_ID)
                .withProvisionToken(Constants.PROVISION_TOKEN)
                .withProvisionTokenRetrieved(Constants.PROVISION_TOKEN_RETRIEVED)
                .withPlaceholderServerUrl(Constants.PLACEHOLDER_SERVER_URL)
                .withServerUrl(Constants.SERVER_URL)
                .withLogo(Constants.LOGO)
                .withName(Constants.NAME)
                .build();
    }
}
