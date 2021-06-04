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
                firstTimeRun()
            } else {
                usuallyTimeRun()
            }
        }
    }

    private fun firstTimeRun() {
        try {
            val constants = buildConstants()
            ConnectMeVcx.createOneTimeInfo(this, constants, R.raw.genesis)
                .whenComplete { res: Void?, ex: Throwable? ->
                    val message: String
                    if (ex != null) {
                        message = "SDK was not initialized!"
                        Log.e(TAG, "Sdk not initialized: ", ex)
                    } else {
                        message = "SDK initialized successfully."
                        ConnectMeVcx.sendToken(
                            this,
                            Constants.PREFS_NAME,
                            Constants.FCM_TOKEN,
                            Constants.FCM_TOKEN_SENT
                        )
                    }
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun buildConstants(): ConnectMeVcx.Constants {
        return ConnectMeVcx.Constants.builder()
            .withWalletName(Constants.WALLET_NAME)
            .withPrefsName(Constants.PREFS_NAME)
            .withSponseeId(Constants.SPONSEE_ID)
            .withProvisionToken(Constants.PROVISION_TOKEN)
            .withProvisionTokenRetrieved(Constants.PROVISION_TOKEN_RETRIEVED)
            .withPlaceholderServerUrl(Constants.PLACEHOLDER_SERVER_URL)
            .withServerUrl(Constants.SERVER_URL)
            .build()
    }

    private fun usuallyTimeRun() {
        ConnectMeVcx.init(this).handleAsync<Any?> { res: Void?, err: Throwable? ->
            val message: String
            if (err == null) {
                message = "SDK initialized successfully."
                ConnectMeVcx.sendToken(
                    this,
                    Constants.PREFS_NAME,
                    Constants.FCM_TOKEN,
                    Constants.FCM_TOKEN_SENT
                )
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