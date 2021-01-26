package me.connect.sdk.java.samplekt

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.evernym.sdk.vcx.StringUtils
import me.connect.sdk.java.AgencyConfig
import me.connect.sdk.java.ConnectMeVcx
import me.connect.sdk.java.samplekt.databinding.MainActivityBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import pl.brightinventions.slf4android.LogLevel
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
            val token = if (!provisionTokenRetrieved()) {
                try {
                    retrieveToken()
                } catch (ex: Exception) {
                    Log.e(TAG, "Failed to retrieve token, init cannot continue: ", ex)
                    return@execute
                }
            } else {
                getSavedToken()
            }
            val config = ConnectMeVcx.Config.builder()
                    .withAgency(AgencyConfig.DEFAULT)
                    .withGenesisPool(R.raw.genesis)
                    .withWalletName(Constants.WALLET_NAME)
                    .withLogLevel(LogLevel.DEBUG)
                    .withContext(this)
                    .withProvisionToken(token)
                    .build()

            // Todo progress bar could be added
            ConnectMeVcx.init(config).handleAsync { _, err ->
                val message: String
                if (err == null) {
                    message = "SDK initialized successfully."
                    sendToken()
                } else {
                    message = "SDK was not initialized!"
                    Log.e(TAG, "Sdk not initialized: ", err)
                }
                runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
                null
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

    private fun provisionTokenRetrieved() = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
            .getBoolean(Constants.PROVISION_TOKEN_RETRIEVED, false)


    private fun getSavedToken() = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
            .getString(Constants.PROVISION_TOKEN, null)


    private fun retrieveToken(): String {
        Log.d(TAG, "Retrieving token")
        if (StringUtils.isNullOrWhiteSpace(Constants.SERVER_URL) || Constants.SERVER_URL == Constants.PLACEHOLDER_SERVER_URL) {
            runOnUiThread { Toast.makeText(this, "Error: sponsor server URL is not set.", Toast.LENGTH_LONG).show() }
            throw Exception("Sponsor's server URL seems to be not set, please set your server URL in constants file to provision the app.")
        }
        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        var sponseeId = prefs.getString(Constants.SPONSEE_ID, null)
        if (sponseeId == null) {
            sponseeId = UUID.randomUUID().toString()
            prefs.edit()
                    .putString(Constants.SPONSEE_ID, sponseeId)
                    .apply()
        }
        val json = JSONObject()
        json.put("sponseeId", sponseeId)
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
                .url(Constants.SERVER_URL)
                .post(body)
                .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Response failed with code " + response.code)
        }
        val token = response.body!!.string()
        Log.d(TAG, "Retrieved token: $token")
        prefs.edit()
                .putString(Constants.PROVISION_TOKEN, token)
                .putBoolean(Constants.PROVISION_TOKEN_RETRIEVED, true)
                .apply()
        return token
    }
}