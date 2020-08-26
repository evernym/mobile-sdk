package me.connect.sdk.java.samplekt

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import me.connect.sdk.java.AgencyConfig
import me.connect.sdk.java.ConnectMeVcx
import me.connect.sdk.java.samplekt.databinding.MainActivityBinding
import pl.brightinventions.slf4android.LogLevel

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
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
        val config: ConnectMeVcx.Config = ConnectMeVcx.Config.builder()
                .withAgency(AgencyConfig.DEFAULT)
                .withGenesisPool(Constants.POOL_TXN_GENESIS)
                .withWalletName(Constants.WALLET_NAME)
                .withLogLevel(LogLevel.DEBUG)
                .withContext(this)
                .build()

        ConnectMeVcx.init(config).handleAsync { _, err ->
            val message = if (err == null) {
                "SDK initialized successfully."
            } else {
                Log.e(TAG, "Sdk not initialized: ", err)
                "SDK was not initialized!"
            }
            runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
        }
    }
}