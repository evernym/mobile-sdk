package me.connect.sdk.java;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.evernym.sdk.vcx.StringUtils;
import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.utils.UtilsApi;
import com.evernym.sdk.vcx.vcx.AlreadyInitializedException;
import com.evernym.sdk.vcx.vcx.VcxApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import java.util.UUID;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import java9.util.concurrent.CompletableFuture;
import pl.brightinventions.slf4android.LogLevel;

import static me.connect.sdk.java.Logger.TAG;

public class Initialization {

    private static final String SECURE_PREF_VCXCONFIG = "me.connect.vcx.config";

    private Initialization() {
    }

    public static boolean isCloudAgentProvisioned(Context context) {
        return SecurePreferencesHelper.containsLongStringValue(context, SECURE_PREF_VCXCONFIG);
    }

    /**
     * Provision Cloud Agent and initialize library
     *
     * @param context Main Activity context
     * @return {@link CompletableFuture}
     */
    public static CompletableFuture<Void> provisionCloudAgentAndInitializeSdk(
            Context context,
            Constants constants,
            @RawRes int genesisPool
    ) throws JSONException {
        Logger.getInstance().setLogLevel(LogLevel.DEBUG);

        Activity activity = (Activity) context;

        // 1. Prepare provisioning config
        String provisioningConfig = Initialization.Config.builder()
                .withAgencyEndpoint(constants.AGENCY_ENDPOINT)
                .withAgencyDid(constants.AGENCY_DID)
                .withAgencyVerkey(constants.AGENCY_VERKEY)
                .withWalletName(Utils.makeWalletName(constants.WALLET_NAME))
                .withWalletKey(Utils.createWalletKey())
                .withLogo(constants.LOGO)
                .withName(constants.NAME)
                .buildVcxConfig();

        CompletableFuture<Void> result = new CompletableFuture<>();

        try {
            // 2. Receive provisioning token from Sponsor Server
            String token = retrieveToken(activity, constants);

            // 3. Provision Cloud Agent with prepared config and received token
            UtilsApi.vcxAgentProvisionWithTokenAsync(provisioningConfig, token)
                .whenComplete((oneTimeInfo, err) -> {
                    try {
                        if (err != null) {
                            Logger.getInstance().e("createOneTimeInfo failed: ", err);
                            result.completeExceptionally(err);
                        } else if (oneTimeInfo == null) {
                            throw new Exception("oneTimeInfo is null");
                        } else {
                            Logger.getInstance().i("createOneTimeInfo called: " + oneTimeInfo);
                            try {
                                // 4. Store config with provisioned Cloud Agent data
                                SecurePreferencesHelper.setLongStringValue(context, SECURE_PREF_VCXCONFIG, oneTimeInfo);

                                // 5. Initialize library
                                initialize(context, genesisPool).whenComplete((returnCode, error) -> {
                                    if (error != null) {
                                        Logger.getInstance().e("Init failed", error);
                                        result.completeExceptionally(error);
                                    } else {
                                        Logger.getInstance().i("Init completed");
                                        result.complete(null);
                                    }
                                });
                            } catch (Exception e) {
                                result.completeExceptionally(e);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     * Initialize library
     *
     * @param context Main Activity context
     * @return {@link CompletableFuture}
     */
    public static @NonNull
    CompletableFuture<Void> initializeSdk(Context context, @RawRes int genesisPool) {
        Logger.getInstance().setLogLevel(LogLevel.DEBUG);
        Logger.getInstance().i("Initializing SDK");
        CompletableFuture<Void> result = new CompletableFuture<>();

        // 1. Initialize library
        initialize(context, genesisPool).whenComplete((returnCode, err) -> {
            if (err != null) {
                Logger.getInstance().e("Init failed", err);
                result.completeExceptionally(err);
            } else {
                Logger.getInstance().i("Init completed");
                result.complete(null);
            }
        });
        return result;
    }

    private static CompletableFuture<Void> initialize(Context context, @RawRes int genesisPool) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            // 1. Receive config from the storage
            String config = SecurePreferencesHelper.getLongStringValue(context, SECURE_PREF_VCXCONFIG, null);

            // 2. Initialize VCX library
            VcxApi.vcxInitWithConfig(config).whenComplete((integer, err) -> {
                if (err != null) {
                    result.completeExceptionally(err);
                } else {
                    // 3. Initialize pool
                    initPool(context, genesisPool);
                    result.complete(null);
                }
            });
        } catch (AlreadyInitializedException e) {
            // even if we get already initialized exception
            // then also we will resolve promise, because we don't care if vcx is already
            // initialized
            result.complete(null);
        } catch (VcxException e) {
            e.printStackTrace();
            result.completeExceptionally(e);
        }
        return result;
    }

    private static void initPool(Context context, @RawRes int genesisPool) {
        // 1. Run Pool initialization in a separate thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject poolConfig = new JSONObject();
                File genesisFile = Utils.writeGenesisFile(context, genesisPool);
                poolConfig.put("genesis_path", genesisFile.getAbsoluteFile());
                poolConfig.put("pool_name", "android-sample-pool");
                VcxApi.vcxInitPool(poolConfig.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String retrieveToken(Activity activity, Constants constants) throws Exception {
        Log.d(TAG, "Retrieving token");

        if (StringUtils.isNullOrWhiteSpace(constants.SERVER_URL) || constants.SERVER_URL.equals(constants.PLACEHOLDER_SERVER_URL)) {
            activity.runOnUiThread(() -> {
                Toast.makeText(activity, "Error: sponsor server URL is not set.", Toast.LENGTH_LONG).show();
            });
            throw new Exception("Sponsor's server URL seems to be not set, please set your server URL in constants file to provision the app.");
        }

        SharedPreferences prefs = activity.getSharedPreferences(constants.PREFS_NAME, Context.MODE_PRIVATE);
        String sponseeId = prefs.getString(constants.SPONSEE_ID, null);
        if (sponseeId == null) {
            sponseeId = UUID.randomUUID().toString();
            prefs.edit()
                    .putString(constants.SPONSEE_ID, sponseeId)
                    .apply();
        }
        JSONObject json = new JSONObject();
        json.put("sponseeId", sponseeId);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(constants.SERVER_URL)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new Exception("Response failed with code " + response.code());
        }
        String token = response.body().string();
        if (token == null) {
            throw new Exception("Token is not received ");
        }
        Log.d(TAG, "Retrieved token: " + token);
        prefs.edit()
                .putString(constants.PROVISION_TOKEN, token)
                .putBoolean(constants.PROVISION_TOKEN_RETRIEVED, true)
                .apply();

        return token;
    }

//    public static void sendToken(
//            Activity activity,
//            String PREFS_NAME,
//            String FCM_TOKEN,
//            String FCM_TOKEN_SENT) {
//        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        boolean tokenSent = prefs.getBoolean(FCM_TOKEN_SENT, false);
//        if (tokenSent) {
//            Log.d(TAG, "FCM token already sent");
//            return;
//        }
//        String token = prefs.getString(FCM_TOKEN, null);
//        if (token != null) {
//            ConnectMeVcx.updateAgentInfo(UUID.randomUUID().toString(), token).whenComplete((res, err) -> {
//                if (err == null) {
//                    Log.d(TAG, "FCM token updated successfully");
//                    prefs.edit()
//                            .putBoolean(FCM_TOKEN_SENT, true)
//                            .apply();
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

    public void shutdownVcx(Boolean deleteWallet) {
        Logger.getInstance().d(" ==> shutdownVcx() called with: deleteWallet = [" + deleteWallet);
        try {
            VcxApi.vcxShutdown(deleteWallet);
        } catch (VcxException e) {
            e.printStackTrace();
        }
    }

    public static final class ConstantsBuilder {
        private String AGENCY_ENDPOINT;
        private String AGENCY_DID;
        private String AGENCY_VERKEY;
        private String WALLET_NAME;
        private String PREFS_NAME;
        private String SPONSEE_ID;
        private String PROVISION_TOKEN;
        private String FCM_TOKEN;
        private String FCM_TOKEN_SENT;
        private String PROVISION_TOKEN_RETRIEVED;
        private String PLACEHOLDER_SERVER_URL;
        private String SERVER_URL;
        private String LOGO;
        private String NAME;

        private ConstantsBuilder() {
        }

        public @NonNull
        ConstantsBuilder withAgencyEndpoint(@NonNull String AGENCY_ENDPOINT) {
            this.AGENCY_ENDPOINT = AGENCY_ENDPOINT;
            return this;
        }

        public @NonNull
        ConstantsBuilder withAgencyDid(@NonNull String AGENCY_DID) {
            this.AGENCY_DID = AGENCY_DID;
            return this;
        }

        public @NonNull
        ConstantsBuilder withAgencyVerkey(@NonNull String AGENCY_VERKEY) {
            this.AGENCY_VERKEY = AGENCY_VERKEY;
            return this;
        }

        public @NonNull
        ConstantsBuilder withWalletName(@NonNull String WALLET_NAME) {
            this.WALLET_NAME = WALLET_NAME;
            return this;
        }

        public @NonNull
        ConstantsBuilder withPrefsName(@NonNull String PREFS_NAME) {
            this.PREFS_NAME = PREFS_NAME;
            return this;
        }

        public @NonNull
        ConstantsBuilder withSponseeId(@NonNull String SPONSEE_ID) {
            this.SPONSEE_ID = SPONSEE_ID;
            return this;
        }

        public @NonNull
        ConstantsBuilder withProvisionToken(@NonNull String PROVISION_TOKEN) {
            this.PROVISION_TOKEN = PROVISION_TOKEN;
            return this;
        }

        public @NonNull
        ConstantsBuilder withFcmToken(@NonNull String FCM_TOKEN) {
            this.FCM_TOKEN = FCM_TOKEN;
            return this;
        }

        public @NonNull
        ConstantsBuilder withFcmTokenSent(@NonNull String FCM_TOKEN_SENT) {
            this.FCM_TOKEN_SENT = FCM_TOKEN_SENT;
            return this;
        }

        public @NonNull
        ConstantsBuilder withProvisionTokenRetrieved(@NonNull String PROVISION_TOKEN_RETRIEVED) {
            this.PROVISION_TOKEN_RETRIEVED = PROVISION_TOKEN_RETRIEVED;
            return this;
        }

        public @NonNull
        ConstantsBuilder withPlaceholderServerUrl(@NonNull String PLACEHOLDER_SERVER_URL) {
            this.PLACEHOLDER_SERVER_URL = PLACEHOLDER_SERVER_URL;
            return this;
        }

        public @NonNull
        ConstantsBuilder withServerUrl(@NonNull String SERVER_URL) {
            this.SERVER_URL = SERVER_URL;
            return this;
        }

        public @NonNull
        ConstantsBuilder withLogo(@NonNull String LOGO) {
            this.LOGO = LOGO;
            return this;
        }

        public @NonNull
        ConstantsBuilder withName(@NonNull String NAME) {
            this.NAME = NAME;
            return this;
        }

        /**
         * Build {@link Config} instance.
         *
         * @return {@link Config} instance
         */
        public @NonNull
        Constants build() {
            return new Constants(
                    AGENCY_ENDPOINT,
                    AGENCY_DID,
                    AGENCY_VERKEY,
                    WALLET_NAME,
                    PREFS_NAME,
                    SPONSEE_ID,
                    PROVISION_TOKEN,
                    FCM_TOKEN,
                    FCM_TOKEN_SENT,
                    PROVISION_TOKEN_RETRIEVED,
                    PLACEHOLDER_SERVER_URL,
                    SERVER_URL,
                    LOGO,
                    NAME
            );
        }
    }

    /**
     * Config used during {@link Initialization} initialization.
     */
    public static class Constants {
        private String AGENCY_ENDPOINT;
        private String AGENCY_DID;
        private String AGENCY_VERKEY;
        private String WALLET_NAME;
        private String PREFS_NAME;
        private String SPONSEE_ID;
        private String PROVISION_TOKEN;
        private String FCM_TOKEN;
        private String FCM_TOKEN_SENT;
        private String PROVISION_TOKEN_RETRIEVED;
        private String PLACEHOLDER_SERVER_URL;
        private String SERVER_URL;
        private String LOGO;
        private String NAME;

        public Constants(String AGENCY_ENDPOINT,
                         String AGENCY_DID,
                         String AGENCY_VERKEY,
                         String WALLET_NAME,
                         String PREFS_NAME,
                         String SPONSEE_ID,
                         String PROVISION_TOKEN,
                         String FCM_TOKEN,
                         String FCM_TOKEN_SENT,
                         String PROVISION_TOKEN_RETRIEVED,
                         String PLACEHOLDER_SERVER_URL,
                         String SERVER_URL,
                         String LOGO,
                         String NAME
        ) {
            this.AGENCY_ENDPOINT = AGENCY_ENDPOINT;
            this.AGENCY_DID = AGENCY_DID;
            this.AGENCY_VERKEY = AGENCY_VERKEY;
            this.WALLET_NAME = WALLET_NAME;
            this.PREFS_NAME = PREFS_NAME;
            this.SPONSEE_ID = SPONSEE_ID;
            this.PROVISION_TOKEN = PROVISION_TOKEN;
            this.FCM_TOKEN = FCM_TOKEN;
            this.FCM_TOKEN_SENT = FCM_TOKEN_SENT;
            this.PROVISION_TOKEN_RETRIEVED = PROVISION_TOKEN_RETRIEVED;
            this.PLACEHOLDER_SERVER_URL = PLACEHOLDER_SERVER_URL;
            this.SERVER_URL = SERVER_URL;
            this.LOGO = LOGO;
            this.NAME = NAME;
        }

        /**
         * Creates builder for {@link Config}.
         *
         * @return {@link ConstantsBuilder} instance
         */
        public static ConstantsBuilder builder() {
            return new ConstantsBuilder();
        }

    }

    public static final class ConfigBuilder {
        private String genesisPool;
        private Integer genesisPoolResId;
        private String agencyEndpoint;
        private String agencyDid;
        private String agencyVerkey;
        private String walletName;
        private String genesisPath;
        private String walletKey;
        private String logo;
        private String name;

        private ConfigBuilder() {
        }

        /**
         * Set genesis pool string.
         *
         * @param genesisPool genesis pool string
         * @return {@link ConfigBuilder} instance
         */
        public @NonNull
        ConfigBuilder withGenesisPool(@NonNull String genesisPool) {
            this.genesisPool = genesisPool;
            return this;
        }

        /**
         * Set genesis pool raw resource id.
         *
         * @param genesisPoolResId raw resource ID of genesis pool
         * @return {@link ConfigBuilder} instance
         */
        public @NonNull
        ConfigBuilder withGenesisPool(@RawRes int genesisPoolResId) {
            this.genesisPoolResId = genesisPoolResId;
            return this;
        }

        /**
         * Set agency endpoint.
         *
         * @param agencyEndpoint agency endpoint
         * @return {@link ConfigBuilder} instance
         */
        public @NonNull
        ConfigBuilder withAgencyEndpoint(@NonNull String agencyEndpoint) {
            this.agencyEndpoint = agencyEndpoint;
            return this;
        }

        /**
         * Set agency did.
         *
         * @param agencyDid agency did
         * @return {@link ConfigBuilder} instance
         */
        public @NonNull
        ConfigBuilder withAgencyDid(@NonNull String agencyDid) {
            this.agencyDid = agencyDid;
            return this;
        }

        /**
         * Set agency verkey.
         *
         * @param agencyVerkey agency verkey
         * @return {@link ConfigBuilder} instance
         */
        public @NonNull
        ConfigBuilder withAgencyVerkey(@NonNull String agencyVerkey) {
            this.agencyVerkey = agencyVerkey;
            return this;
        }

        /**
         * Set wallet name.
         *
         * @param walletName wallet name
         * @return {@link ConfigBuilder} instance
         */
        public @NonNull
        ConfigBuilder withWalletName(@NonNull String walletName) {
            this.walletName = walletName;
            return this;
        }

        /**
         * Set logo
         *
         * @param logo logo
         * @return this
         */
        public @NonNull
        ConfigBuilder withLogo(String logo) {
            this.logo = logo;
            return this;
        }

        /**
         * Set app name
         *
         * @param name name
         * @return this
         */
        public @NonNull
        ConfigBuilder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set genesis path
         *
         * @param genesisPath genesisPath
         * @return this
         */
        public @NonNull
        ConfigBuilder withGenesisPath(String genesisPath) {
            this.genesisPath = genesisPath;
            return this;
        }

        /**
         * Set wallet key
         *
         * @param walletKey walletKey
         * @return this
         */
        public @NonNull
        ConfigBuilder withWalletKey(String walletKey) {
            this.walletKey = walletKey;
            return this;
        }


        /**
         * Build {@link Config} instance.
         *
         * @return {@link Config} instance
         */
        public @NonNull
        Config build() {
            return new Config(
                    agencyEndpoint,
                    agencyDid,
                    agencyVerkey,
                    genesisPool,
                    genesisPoolResId,
                    walletName,
                    logo,
                    name,
                    walletKey
            );
        }

        /**
         * Creates agency config from current config {@link Config} and make wallet dir.
         *
         * @return provisioning config
         */
        public @NonNull
        String buildVcxConfig() throws JSONException {
            JSONObject agencyConfig = new JSONObject();
            agencyConfig.put("agency_endpoint", this.agencyEndpoint);
            agencyConfig.put("agency_did", this.agencyDid);
            agencyConfig.put("agency_verkey", this.agencyVerkey);
            agencyConfig.put("wallet_name", this.walletName);
            agencyConfig.put("wallet_key", this.walletKey);
            agencyConfig.put("protocol_type", "3.0");
            agencyConfig.put("path", this.genesisPath);
            agencyConfig.put("logo", this.logo);
            agencyConfig.put("name", this.name);
            return agencyConfig.toString();
        }
    }

    /**
     * Config used during {@link Initialization} initialization.
     */
    public static class Config {
        private String agencyEndpoint;
        private String agencyDid;
        private String agencyVerkey;
        private String genesisPool;
        private Integer genesisPoolResId;
        private String walletName;
        private String walletKey;
        private String logo;
        private String name;

        public Config(
            String agencyEndpoint,
            String agencyDid,
            String agencyVerkey,
            String genesisPool,
            Integer genesisPoolResId,
            String walletName,
            String walletKey,
            String logo,
            String name
        ) {
            this.agencyEndpoint = agencyEndpoint;
            this.agencyDid = agencyDid;
            this.agencyVerkey = agencyVerkey;
            this.genesisPool = genesisPool;
            this.genesisPoolResId = genesisPoolResId;
            this.walletName = walletName;
            this.walletKey = walletKey;
            this.logo = logo;
            this.name = name;
        }

        /**
         * Creates builder for {@link Config}.
         *
         * @return {@link ConfigBuilder} instance
         */
        public static ConfigBuilder builder() {
            return new ConfigBuilder();
        }
    }
}
