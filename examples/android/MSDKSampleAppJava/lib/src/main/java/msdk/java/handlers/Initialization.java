package msdk.java.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.utils.UtilsApi;
import com.evernym.sdk.vcx.vcx.AlreadyInitializedException;
import com.evernym.sdk.vcx.vcx.VcxApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import java.util.UUID;
import java.util.concurrent.Executors;

import msdk.java.logger.Logger;
import msdk.java.types.PoolConfig;
import msdk.java.types.ProvisioningConfig;
import msdk.java.utils.CommonUtils;
import msdk.java.utils.SecurePreferencesHelper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import java9.util.concurrent.CompletableFuture;
import pl.brightinventions.slf4android.LogLevel;

import static msdk.java.logger.Logger.TAG;
import static msdk.java.utils.StorageUtils.configureStoragePermissions;

public class Initialization {

    private static final String SECURE_PREF_VCX_CONFIG = "msdk.config";

    /*
     * Check if Cloud Agent already provisioned
     *
     * @param context                       {@link Context}
     * @return {@link CompletableFuture}
     */
    public static boolean isCloudAgentProvisioned(Context context) {
        return SecurePreferencesHelper.containsLongStringValue(context, SECURE_PREF_VCX_CONFIG);
    }

    /*
     * Initialize library and provision Cloud Agent if it's not done yet
     *
     * @param context                       {@link Context}
     * @param constants                     SDK settings
     * @param genesisPool                   Genesis transactions
     * @return {@link CompletableFuture}
     */
    public static @NonNull
    CompletableFuture<Void> initialize(
            Context context,
            Constants constants,
            @RawRes int genesisPool
    ) {
        Logger.getInstance().setLogLevel(LogLevel.DEBUG);

        CompletableFuture<Void> result = new CompletableFuture<>();

        try {
            // 1. Configure storage permissions
            configureStoragePermissions(context);
            // 2. Configure Logger
            Logger.configureLogger(context);

            // 3. Provision Cloud Agent if needed
            if (!Initialization.isCloudAgentProvisioned(context)) {
                Initialization.provisionCloudAgent(context, constants).get();
            }

            // 4. Receive config from the storage
            String config = SecurePreferencesHelper.getLongStringValue(context, SECURE_PREF_VCX_CONFIG, null);

            // 5. Initialize VCX library
            VcxApi.vcxInitWithConfig(config).whenComplete((integer, err) -> {
                if (err != null) {
                    result.completeExceptionally(err);
                } else {
                    // 3. Initialize pool
                    initializePool(context, genesisPool);
                    result.complete(null);
                }
            });
        }catch (AlreadyInitializedException e) {
            // even if we get already initialized exception
            // then also we will resolve promise, because we don't care if vcx is already
            // initialized
            result.complete(null);
        } catch (VcxException e) {
            e.printStackTrace();
            result.completeExceptionally(e);
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /*
     * Provision Cloud Agent and store populated config
     *
     * @param context                       {@link Context}
     * @param constants                     SDK settings
     * @return {@link CompletableFuture}
     */
    public static CompletableFuture<Void> provisionCloudAgent(
            Context context,
            Constants constants
    ) throws JSONException {
        Logger.getInstance().setLogLevel(LogLevel.DEBUG);

        Activity activity = (Activity) context;

        // 3. Prepare provisioning config
        String provisioningConfig = ProvisioningConfig.builder()
                .withAgencyEndpoint(constants.AGENCY_ENDPOINT)
                .withAgencyDid(constants.AGENCY_DID)
                .withAgencyVerkey(constants.AGENCY_VERKEY)
                .withWalletName(CommonUtils.makeWalletName(constants.WALLET_NAME))
                .withWalletKey(CommonUtils.createWalletKey())
                .withLogo(constants.LOGO)
                .withName(constants.NAME)
                .build();

        CompletableFuture<Void> result = new CompletableFuture<>();

        try {
            // 4. Receive provisioning token from Sponsor Server
            String token = retrieveToken(activity, constants);

            // 5. Provision Cloud Agent with prepared config and received token
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
                                // 6. Store config with provisioned Cloud Agent data
                                SecurePreferencesHelper.setLongStringValue(context, SECURE_PREF_VCX_CONFIG, oneTimeInfo);
                                result.complete(null);
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

    /*
     * Connect to Pool Ledger
     *
     * @param context                       {@link Context}
     * @param genesisPool                   Genesis transactions
     * @return {@link CompletableFuture}
     */
    private static void initializePool(Context context, @RawRes int genesisPool) {
        // 1. Run Pool initialization in a separate thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File genesisFile = PoolConfig.writeGenesisFile(context, genesisPool);
                String poolConfig = PoolConfig.builder()
                        .withgGenesisPath(genesisFile.getAbsolutePath())
                        .withgPoolName("android-sample-pool")
                        .build();
                VcxApi.vcxInitPool(poolConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Retrieve provisioning token from Sponsor Server
     */
    private static String retrieveToken(Activity activity, Constants constants) throws Exception {
        Log.d(TAG, "Retrieving token");

        if (constants.SERVER_URL == null || constants.SERVER_URL.length() == 0) {
            activity.runOnUiThread(() -> {
                Toast.makeText(activity, "Error: sponsor server URL is not set.", Toast.LENGTH_LONG).show();
            });
            throw new Exception("Sponsor's server URL seems to be not set, please set your server URL in constants file to provision the app.");
        }

        SharedPreferences prefs = activity.getSharedPreferences(constants.PREFERENCES_KEY, Context.MODE_PRIVATE);
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
        return token;
    }

    public static final class ConstantsBuilder {
        private String AGENCY_ENDPOINT;
        private String AGENCY_DID;
        private String AGENCY_VERKEY;
        private String WALLET_NAME;
        private String PREFERENCES_KEY;
        private String SPONSEE_ID;
        private String FCM_TOKEN;
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
        ConstantsBuilder withPrefsName(@NonNull String PREFERENCES_KEY) {
            this.PREFERENCES_KEY = PREFERENCES_KEY;
            return this;
        }

        public @NonNull
        ConstantsBuilder withSponseeId(@NonNull String SPONSEE_ID) {
            this.SPONSEE_ID = SPONSEE_ID;
            return this;
        }

        public @NonNull
        ConstantsBuilder withFcmToken(@NonNull String FCM_TOKEN) {
            this.FCM_TOKEN = FCM_TOKEN;
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
         * Build {@link Constants} instance.
         *
         * @return {@link Constants} instance
         */
        public @NonNull
        Constants build() {
            return new Constants(
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
        private String PREFERENCES_KEY;
        private String SPONSEE_ID;
        private String FCM_TOKEN;
        private String SERVER_URL;
        private String LOGO;
        private String NAME;

        public Constants(String AGENCY_ENDPOINT,
                         String AGENCY_DID,
                         String AGENCY_VERKEY,
                         String WALLET_NAME,
                         String PREFERENCES_KEY,
                         String SPONSEE_ID,
                         String FCM_TOKEN,
                         String SERVER_URL,
                         String LOGO,
                         String NAME
        ) {
            this.AGENCY_ENDPOINT = AGENCY_ENDPOINT;
            this.AGENCY_DID = AGENCY_DID;
            this.AGENCY_VERKEY = AGENCY_VERKEY;
            this.WALLET_NAME = WALLET_NAME;
            this.PREFERENCES_KEY = PREFERENCES_KEY;
            this.SPONSEE_ID = SPONSEE_ID;
            this.FCM_TOKEN = FCM_TOKEN;
            this.SERVER_URL = SERVER_URL;
            this.LOGO = LOGO;
            this.NAME = NAME;
        }

        /**
         * Creates builder for {@link Constants}.
         *
         * @return {@link ConstantsBuilder} instance
         */
        public static ConstantsBuilder builder() {
            return new ConstantsBuilder();
        }

    }
}
