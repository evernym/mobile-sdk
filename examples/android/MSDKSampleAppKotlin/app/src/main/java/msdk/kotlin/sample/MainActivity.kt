package msdk.kotlin.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import msdk.kotlin.sample.databinding.MainActivityBinding
import msdk.kotlin.sample.handlers.Initialization
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        initialize()
    }

    private fun initViews() {
        viewBinding = MainActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        val adapter = MainPagerAdapter(supportFragmentManager)
        viewBinding.pager.adapter = adapter
        viewBinding.tabLayout.setupWithViewPager(viewBinding.pager)
    }

    private fun initialize() {
        Toast.makeText(this, "Application initialization started", Toast.LENGTH_SHORT).show()
        Executors.newSingleThreadExecutor().execute {
            // 1. Start initialization
            val constants = buildConstants()
            Initialization.initialize(this, constants, R.raw.genesis)
                .whenComplete { res: Void?, ex: Throwable? ->
                    if (ex != null) {
                        runOnUiThread {
                            Toast.makeText(this, "Application initialization failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Application successfully initialized", Toast.LENGTH_SHORT).show()
                        }
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
