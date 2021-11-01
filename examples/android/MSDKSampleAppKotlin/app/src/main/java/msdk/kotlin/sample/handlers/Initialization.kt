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
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import pl.brightinventions.slf4android.LogLevel
import java.util.*
import java.util.concurrent.Executors

class Initialization {
    //    public static void sendToken(
    //            Activity activity,
    //            String PREFERENCES_KEY,
    //            String FCM_TOKEN) {
    //        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
    //        String token = prefs.getString(FCM_TOKEN, null);
    //        if (token != null) {
    //            ConnectMeVcx.updateAgentInfo(UUID.randomUUID().toString(), token).whenComplete((res, err) -> {
    //                if (err == null) {
    //                    Log.d(TAG, "FCM token updated successfully");
    //                } else {
    //                    Log.e(TAG, "FCM token was not updated: ", err);
    //                }
    //            });
    //        }
    //    }
    //
    //    public static CompletableFuture<Void> updateAgentInfo(String id, String token) {
    //        CompletableFuture<Void> result = new CompletableFuture<>();
    //        try {
    //            JSONObject config = new JSONObject();
    //            config.put("type", 3);
    //            config.put("id", id);
    //            config.put("value", "FCM:" + token);
    //            UtilsApi.vcxUpdateAgentInfo(config.toString()).whenComplete((v, err) -> {
    //                if (err != null) {
    //                    // Fixme workaround due to issues on agency side
    //                    if (err instanceof InvalidAgencyResponseException
    //                            && ((InvalidAgencyResponseException) err).getSdkCause().contains("data did not match any variant of untagged enum MessageTypes")) {
    //                        result.complete(null);
    //                    } else {
    //                        Logger.getInstance().e("Failed to update agent info", err);
    //                        result.completeExceptionally(err);
    //                    }
    //                } else {
    //                    result.complete(null);
    //                }
    //            });
    //        } catch (Exception ex) {
    //            result.completeExceptionally(ex);
    //        }
    //        return result;
    //    }
    fun shutdownVcx(deleteWallet: Boolean) {
        Logger.instance
            .d(" ==> shutdownVcx() called with: deleteWallet = [$deleteWallet")
        try {
            VcxApi.vcxShutdown(deleteWallet)
        } catch (e: VcxException) {
            e.printStackTrace()
        }
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

    companion object {
        private const val SECURE_PREF_VCX_CONFIG = "me.connect.vcx.config"
        fun isCloudAgentProvisioned(context: Context?): Boolean {
            return SecurePreferencesHelper.containsLongStringValue(
                context,
                SECURE_PREF_VCX_CONFIG
            )
        }

        /**
         * Provision Cloud Agent and initialize library
         */
        @Throws(JSONException::class)
        fun provisionCloudAgentAndInitializeSdk(
            context: Context,
            constants: Constants,
            @RawRes genesisPool: Int
        ): CompletableFuture<Void?> {
            Logger.instance.setLogLevel(LogLevel.DEBUG)
            val activity = context as Activity

            // 1. Configure storage permissions
            StorageUtils.configureStoragePermissions(context)
            // 2. Configure Logger
            Logger.configureLogger(context)
            // 3. Prepare provisioning config
            val provisioningConfig = ProvisioningConfig.builder()
                .withAgencyEndpoint(constants.AGENCY_ENDPOINT!!)
                .withAgencyDid(constants.AGENCY_DID!!)
                .withAgencyVerkey(constants.AGENCY_VERKEY!!)
                .withWalletName(CommonUtils.makeWalletName(constants.WALLET_NAME))
                .withWalletKey(CommonUtils.createWalletKey())
                .withLogo(constants.LOGO)
                .withName(constants.NAME)
                .build()
            val result =
                CompletableFuture<Void?>()
            try {
                // 4. Receive provisioning token from Sponsor Server
                val token =
                    retrieveToken(activity, constants)

                // 5. Provision Cloud Agent with prepared config and received token
                UtilsApi.vcxAgentProvisionWithTokenAsync(provisioningConfig, token)
                    .whenComplete { oneTimeInfo: String?, err: Throwable? ->
                        try {
                            if (err != null) {
                                Logger.instance
                                    .e("createOneTimeInfo failed: ", err)
                                result.completeExceptionally(err)
                            } else if (oneTimeInfo == null) {
                                throw Exception("oneTimeInfo is null")
                            } else {
                                Logger.instance
                                    .i("createOneTimeInfo called: $oneTimeInfo")
                                try {
                                    // 6. Store config with provisioned Cloud Agent data
                                    SecurePreferencesHelper.setLongStringValue(
                                        context,
                                        SECURE_PREF_VCX_CONFIG,
                                        oneTimeInfo
                                    )

                                    // 7. Initialize library
                                    initialize(context, genesisPool)
                                        .whenComplete { returnCode: Void?, error: Throwable? ->
                                            if (error != null) {
                                                Logger.instance
                                                    .e("Init failed", error)
                                                result.completeExceptionally(error)
                                            } else {
                                                Logger.instance
                                                    .i("Init completed")
                                                result.complete(null)
                                            }
                                        }
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
         * Initialize library without Cloud Agent provisioning
         */
        fun initializeSdk(
            context: Context,
            @RawRes genesisPool: Int
        ): CompletableFuture<Void?> {
            Logger.instance.setLogLevel(LogLevel.DEBUG)
            val result =
                CompletableFuture<Void?>()

            // 1. Configure storage permissions
            StorageUtils.configureStoragePermissions(context)
            // 2. Configure Logger
            Logger.configureLogger(context)
            // 3. Initialize library
            initialize(context, genesisPool)
                .whenComplete { returnCode: Void?, err: Throwable? ->
                    if (err != null) {
                        Logger.instance.e("Init failed", err)
                        result.completeExceptionally(err)
                    } else {
                        Logger.instance.i("Init completed")
                        result.complete(null)
                    }
                }
            return result
        }

        private fun initialize(
            context: Context,
            @RawRes genesisPool: Int
        ): CompletableFuture<Void?> {
            val result =
                CompletableFuture<Void?>()
            try {
                // 1. Receive config from the storage
                val config = SecurePreferencesHelper.getLongStringValue(
                    context,
                    SECURE_PREF_VCX_CONFIG
                )

                // 2. Initialize VCX library
                VcxApi.vcxInitWithConfig(config)
                    .whenComplete { integer: Int?, err: Throwable? ->
                        if (err != null) {
                            result.completeExceptionally(err)
                        } else {
                            // 3. Initialize pool
                            initPool(context, genesisPool)
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
            }
            return result
        }

        private fun initPool(
            context: Context,
            @RawRes genesisPool: Int
        ) {
            // 1. Run Pool initialization in a separate thread
            Executors.newSingleThreadExecutor().execute {
                try {
                    val genesisFile =
                        PoolConfig.writeGenesisFile(context, genesisPool)
                    val poolConfig = PoolConfig.builder()
                        .withgGenesisPath(genesisFile.absolutePath)
                        .withgPoolName("android-sample-pool")
                        .build()
                    VcxApi.vcxInitPool(poolConfig)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

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
            val client =
                OkHttpClient.Builder().addInterceptor(logging).build()
            val body: RequestBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(constants.SERVER_URL)
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("Response failed with code " + response.code)
            }
            return response.body!!.string() ?: throw Exception("Token is not received ")
        }
    }
}