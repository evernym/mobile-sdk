package msdk.kotlin.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import msdk.kotlin.sample.databinding.MainActivityBinding
import msdk.kotlin.sample.handlers.Initialization
import org.json.JSONException
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        initSdk()
    }

    private fun initViews() {
        viewBinding = MainActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        val adapter = MainPagerAdapter(supportFragmentManager)
        viewBinding.pager.adapter = adapter
        viewBinding.tabLayout.setupWithViewPager(viewBinding.pager)
    }

    private fun initSdk() {
        Toast.makeText(this, "Started SDK initialization", Toast.LENGTH_SHORT).show()
        Executors.newSingleThreadExecutor().execute {
            if (!Initialization.isCloudAgentProvisioned(this)) {
                firstTimeRun()
            } else {
                regularRun()
            }
        }
    }

    private fun firstTimeRun() {
        try {
            val constants = buildConstants()

            // 1. Start provisioning
            Initialization.provisionCloudAgentAndInitializeSdk(this, constants, R.raw.genesis)
                .whenComplete { res: Void?, ex: Throwable? ->
                    val message: String
                    if (ex != null) {
                        runOnUiThread {
                            Toast.makeText(this, "Sdk not initialized: ", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "SDK initialized successfully.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun regularRun() {
        // 1. Start initialization
        Initialization.initializeSdk(this, R.raw.genesis).handleAsync<Any?> { res: Void?, err: Throwable? ->
            val message: String
            if (err == null) {
                runOnUiThread {
                    Toast.makeText(this, "SDK initialized successfully.", Toast.LENGTH_SHORT).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "SDK was not initialized!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buildConstants(): Initialization.Constants {
        return Initialization.Constants.builder()
                .withAgencyEndpoint(Constants.AGENCY_ENDPOINT)
                .withAgencyDid(Constants.AGENCY_DID)
                .withAgencyVerkey(Constants.AGENCY_VERKEY)
                .withWalletName(Constants.WALLET_NAME)
                .withPrefsName(Constants.PREFS_NAME)
                .withSponseeId(Constants.SPONSEE_ID)
                .withServerUrl(Constants.SERVER_URL)
                .withLogo(Constants.LOGO)
                .withName(Constants.NAME)
                .build()
    }
}