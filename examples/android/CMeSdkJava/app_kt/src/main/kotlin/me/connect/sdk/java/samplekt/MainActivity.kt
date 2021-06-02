package me.connect.sdk.java.samplekt

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import me.connect.sdk.java.ConnectMeVcx
import me.connect.sdk.java.samplekt.databinding.MainActivityBinding
import org.json.JSONException
import java.util.*
import java.util.concurrent.Executors

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
        Executors.newSingleThreadExecutor().execute {
            if (!ConnectMeVcx.configAlreadyExist(this)) {
                val constants = ConnectMeVcx.Constants.builder()
                    .withWalletName(Constants.WALLET_NAME)
                    .withPrefsName(Constants.PREFS_NAME)
                    .withSponseeId(Constants.SPONSEE_ID)
                    .withProvisionToken(Constants.PROVISION_TOKEN)
                    .withProvisionTokenRetrieved(Constants.PROVISION_TOKEN_RETRIEVED)
                    .withPlaceholderServerUrl(Constants.PLACEHOLDER_SERVER_URL)
                    .withServerUrl(Constants.SERVER_URL)
                    .build()

                try {
                    ConnectMeVcx.init(this, constants, R.raw.genesis)
                        .handleAsync<Any?> { res: Void?, err: Throwable? ->
                        val message: String
                        if (err == null) {
                            message = "SDK initialized successfully."
                            sendToken()
                        } else {
                            message = "SDK was not initialized!"
                            Log.e(TAG, "Sdk not initialized: ", err)
                        }
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        null
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                ConnectMeVcx.init(this)
                    .handleAsync<Any?> { res: Void?, err: Throwable? ->
                    val message: String
                    if (err == null) {
                        message = "SDK initialized successfully."
                        sendToken()
                    } else {
                        message = "SDK was not initialized!"
                        Log.e(TAG, "Sdk not initialized: ", err)
                    }
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    null
                }
            }
        }
    }

    private fun sendToken() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val tokenSent = prefs.getBoolean(Constants.FCM_TOKEN_SENT, false)
        if (tokenSent) {
            Log.d(TAG, "FCM token already sent")
            return
        }
        val token = prefs.getString(Constants.FCM_TOKEN, null)
        if (token != null) {
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