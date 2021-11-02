package msdk.kotlin.sample.handlers

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.RawRes
import com.evernym.sdk.vcx.VcxException
import com.evernym.sdk.vcx.utils.UtilsApi
import com.evernym.sdk.vcx.vcx.AlreadyInitializedException
import com.evernym.sdk.vcx.vcx.VcxApi
import java9.util.concurrent.CompletableFuture
import msdk.kotlin.sample.logger.Logger
import msdk.kotlin.sample.types.PoolConfig
import msdk.kotlin.sample.types.ProvisioningConfig
import msdk.kotlin.sample.utils.CommonUtils
import msdk.kotlin.sample.utils.SecurePreferencesHelper
import msdk.kotlin.sample.utils.StorageUtils
import msdk.kotlin.sample.utils.StorageUtils.configureStoragePermissions
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import pl.brightinventions.slf4android.LogLevel
import java.io.File
import java.util.*
import java.util.concurrent.Executors

object Initialization {
    private const val SECURE_PREF_VCX_CONFIG = "me.connect.vcx.config"
    fun isCloudAgentProvisioned(context: Context?): Boolean {
        return SecurePreferencesHelper.containsLongStringValue(
            context,
            SECURE_PREF_VCX_CONFIG
        )
    }

    /**
     * Initialize library and provision Cloud Agent if it's not done yet
     */
    fun initialize(
        context: Context,
        constants: Constants,
        @RawRes genesisPool: Int
    ): CompletableFuture<Void?> {
        Logger.instance.setLogLevel(LogLevel.DEBUG)
        val result =
            CompletableFuture<Void?>()
        try {
            // 1. Configure storage permissions
            StorageUtils.configureStoragePermissions(context)

            // 2. Configure Logger
            Logger.configureLogger(context)

            // 3. Provision Cloud Agent if needed
            if (!isCloudAgentProvisioned(context)) {
                provisionCloudAgent(context, constants).get()
            }

            // 4. Receive config from the storage
            val config: String? = SecurePreferencesHelper.getLongStringValue(context, SECURE_PREF_VCX_CONFIG)

            // 5. Initialize VCX library
            VcxApi.vcxInitWithConfig(config)
                .whenComplete { integer: Int?, err: Throwable? ->
                    if (err != null) {
                        result.completeExceptionally(err)
                    } else {
                        // 3. Initialize pool
                        initializePool(context, genesisPool)
                        result.complete(null)
                    }
                }
        } catch (e: AlreadyInitializedException) {
            // even if we get already initialized exception
            // then also we will resolve promise, because we don't care if vcx is already
            // initialized
            result.complete(null)
        } catch (e: VcxException) {
            e.printStackTrace()
            result.completeExceptionally(e)
        } catch (e: Exception) {
            result.completeExceptionally(e)
        }
        return result
    }

    /**
     * Provision Cloud Agent and store populated config
     */
    @Throws(JSONException::class)
    fun provisionCloudAgent(
        context: Context,
        constants: Constants
    ): CompletableFuture<Void?> {
        Logger.instance.setLogLevel(LogLevel.DEBUG)
        val activity = context as Activity

        // 3. Prepare provisioning config
        val provisioningConfig: String = ProvisioningConfig.builder()
            .withAgencyEndpoint(constants.AGENCY_ENDPOINT!!)
            .withAgencyDid(constants.AGENCY_DID!!)
            .withAgencyVerkey(constants.AGENCY_VERKEY!!)
            .withWalletName(CommonUtils.makeWalletName(constants.WALLET_NAME!!))
            .withWalletKey(CommonUtils.createWalletKey())
            .withLogo(constants.LOGO!!)
            .withName(constants.NAME!!)
            .build()
        val result =
            CompletableFuture<Void?>()
        try {
            // 4. Receive provisioning token from Sponsor Server
            val token = retrieveToken(activity, constants)

            // 5. Provision Cloud Agent with prepared config and received token
            UtilsApi.vcxAgentProvisionWithTokenAsync(provisioningConfig, token)
                .whenComplete { oneTimeInfo: String?, err: Throwable? ->
                    try {
                        if (err != null) {
                            Logger.instance.e("createOneTimeInfo failed: ", err)
                            result.completeExceptionally(err)
                        } else if (oneTimeInfo == null) {
                            throw Exception("oneTimeInfo is null")
                        } else {
                            Logger.instance.i("createOneTimeInfo called: $oneTimeInfo")
                            try {
                                // 6. Store config with provisioned Cloud Agent data
                                SecurePreferencesHelper.setLongStringValue(context, SECURE_PREF_VCX_CONFIG, oneTimeInfo)
                                result.complete(null)
                            } catch (e: Exception) {
                                result.completeExceptionally(e)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        } catch (e: Exception) {
            result.completeExceptionally(e)
        }
        return result
    }

    /**
     * Connect to Pool Ledger in a separate thread
     */
    private fun initializePool(
        context: Context,
        @RawRes genesisPool: Int
    ) {
        // 1. Run Pool initialization in a separate thread
        Executors.newSingleThreadExecutor().execute {
            try {
                val genesisFile: File = PoolConfig.writeGenesisFile(context, genesisPool)
                val poolConfig: String = PoolConfig.builder()
                    .withgGenesisPath(genesisFile.absolutePath)
                    .withgPoolName("android-sample-pool")
                    .build()
                VcxApi.vcxInitPool(poolConfig)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Retrieve provisioning token from Sponsor Server
     */
    @Throws(Exception::class)
    private fun retrieveToken(
        activity: Activity,
        constants: Constants
    ): String {
        if (constants.SERVER_URL == null || constants.SERVER_URL.length == 0) {
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    "Error: sponsor server URL is not set.",
                    Toast.LENGTH_LONG
                ).show()
            }
            throw Exception("Sponsor's server URL seems to be not set, please set your server URL in constants file to provision the app.")
        }
        val prefs = activity.getSharedPreferences(
            constants.PREFERENCES_KEY,
            Context.MODE_PRIVATE
        )
        var sponseeId = prefs.getString(constants.SPONSEE_ID, null)
        if (sponseeId == null) {
            sponseeId = UUID.randomUUID().toString()
            prefs.edit()
                .putString(constants.SPONSEE_ID, sponseeId)
                .apply()
        }
        val json = JSONObject()
        json.put("sponseeId", sponseeId)
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val body: RequestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(constants.SERVER_URL)
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Response failed with code " + response.code)
        }
        val token = response.body!!.string() ?: throw Exception("Token is not received ")
        return token
    }

    class ConstantsBuilder {
        private var AGENCY_ENDPOINT: String? = null
        private var AGENCY_DID: String? = null
        private var AGENCY_VERKEY: String? = null
        private var WALLET_NAME: String? = null
        private var PREFERENCES_KEY: String? = null
        private var SPONSEE_ID: String? = null
        private var FCM_TOKEN: String? = null
        private var SERVER_URL: String? = null
        private var LOGO: String? = null
        private var NAME: String? = null
        fun withAgencyEndpoint(AGENCY_ENDPOINT: String): ConstantsBuilder {
            this.AGENCY_ENDPOINT = AGENCY_ENDPOINT
            return this
        }

        fun withAgencyDid(AGENCY_DID: String): ConstantsBuilder {
            this.AGENCY_DID = AGENCY_DID
            return this
        }

        fun withAgencyVerkey(AGENCY_VERKEY: String): ConstantsBuilder {
            this.AGENCY_VERKEY = AGENCY_VERKEY
            return this
        }

        fun withWalletName(WALLET_NAME: String): ConstantsBuilder {
            this.WALLET_NAME = WALLET_NAME
            return this
        }

        fun withPrefsName(PREFERENCES_KEY: String): ConstantsBuilder {
            this.PREFERENCES_KEY = PREFERENCES_KEY
            return this
        }

        fun withSponseeId(SPONSEE_ID: String): ConstantsBuilder {
            this.SPONSEE_ID = SPONSEE_ID
            return this
        }

        fun withFcmToken(FCM_TOKEN: String): ConstantsBuilder {
            this.FCM_TOKEN = FCM_TOKEN
            return this
        }

        fun withServerUrl(SERVER_URL: String): ConstantsBuilder {
            this.SERVER_URL = SERVER_URL
            return this
        }

        fun withLogo(LOGO: String): ConstantsBuilder {
            this.LOGO = LOGO
            return this
        }

        fun withName(NAME: String): ConstantsBuilder {
            this.NAME = NAME
            return this
        }

        /**
         * Build [Constants] instance.
         *
         * @return [Constants] instance
         */
        fun build(): Constants {
            return Constants(
                AGENCY_ENDPOINT,
                AGENCY_DID,
                AGENCY_VERKEY,
                WALLET_NAME,
                PREFERENCES_KEY,
                SPONSEE_ID,
                FCM_TOKEN,
                SERVER_URL,
                LOGO,
                NAME
            )
        }
    }

    /**
     * Config used during [Initialization] initialization.
     */
    class Constants(
        val AGENCY_ENDPOINT: String?,
        val AGENCY_DID: String?,
        val AGENCY_VERKEY: String?,
        val WALLET_NAME: String?,
        val PREFERENCES_KEY: String?,
        val SPONSEE_ID: String?,
        private val FCM_TOKEN: String?,
        val SERVER_URL: String?,
        val LOGO: String?,
        val NAME: String?
    ) {
        companion object {
            /**
             * Creates builder for [Constants].
             *
             * @return [ConstantsBuilder] instance
             */
            fun builder(): ConstantsBuilder {
                return ConstantsBuilder()
            }
        }

    }
}