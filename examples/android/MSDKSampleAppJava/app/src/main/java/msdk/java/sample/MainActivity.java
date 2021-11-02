package msdk.java.sample;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.util.concurrent.Executors;

import msdk.java.sample.databinding.MainActivityBinding;
import msdk.java.handlers.Initialization;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivityBinding viewBinding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewBinding.pager.setAdapter(adapter);
        viewBinding.tabLayout.setupWithViewPager(viewBinding.pager);

        initialize();
    }

    private void initialize() {
        Toast.makeText(this, "Application initialization started", Toast.LENGTH_SHORT).show();
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Start initialization
            Initialization.Constants constants = buildConstants();
            Initialization.initialize(this, constants, R.raw.genesis).handleAsync((res, err) -> {
                if (err != null) {
                    runOnUiThread(() -> Toast.makeText(this, "Application was not initialized!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Application successfully initialized", Toast.LENGTH_SHORT).show());
                }
                return null;
            });
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
