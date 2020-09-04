package me.connect.sdk.java.samplekt

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import me.connect.sdk.java.AgencyConfig
import me.connect.sdk.java.ConnectMeVcx
import me.connect.sdk.java.samplekt.databinding.MainActivityBinding
import pl.brightinventions.slf4android.LogLevel
import java.util.*

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
                .withGenesisPool(R.raw.genesis)
                .withWalletName(Constants.WALLET_NAME)
                .withLogLevel(LogLevel.DEBUG)
                .withContext(this)
                .build()

        ConnectMeVcx.init(config).handleAsync { _, err ->
            val message = if (err == null) {
                sendToken();
                "SDK initialized successfully."
            } else {
                Log.e(TAG, "Sdk not initialized: ", err)
                "SDK was not initialized!"
            }
            runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun sendToken() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val tokenSent = prefs.getBoolean(Constants.FCM_TOKEN_SENT, false)
        if (tokenSent) {
            return
        }
        prefs.getString(Constants.FCM_TOKEN, null)?.let { token ->
            ConnectMeVcx.updateAgentInfo(UUID.randomUUID().toString(), token).whenComplete { _, err ->
                if (err == null) {
                    Log.d(TAG, "FCM token updated successfully")
                    prefs.edit()
                            .putBoolean(Constants.FCM_TOKEN_SENT, true)
                            .apply()
                } else {
                    Log.e(TAG, "FCM token was not updated: ", err)
                }
            }
        }
    }
}