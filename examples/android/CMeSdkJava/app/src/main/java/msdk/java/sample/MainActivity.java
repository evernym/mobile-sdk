package msdk.java.sample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.util.concurrent.Executors;

import msdk.java.sample.databinding.MainActivityBinding;
import msdk.java.handlers.Initialization;
import msdk.java.logger.Logger;

import static msdk.java.utils.StorageUtils.configureStoragePermissions;

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

        initialize();
    }

    private void initialize() {
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
            // 1. Start provisioning
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
        // 1. Start initialization
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
                .withPrefsName(Constants.PREFERENCES_KEY)
                .withSponseeId(Constants.SPONSEE_ID_KEY)
                .withServerUrl(Constants.SERVER_URL)
                .withLogo(Constants.LOGO)
                .withName(Constants.NAME)
                .build();
    }
}
