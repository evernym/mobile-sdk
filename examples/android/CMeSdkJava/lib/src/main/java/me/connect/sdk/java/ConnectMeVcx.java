package me.connect.sdk.java;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.evernym.sdk.vcx.StringUtils;
import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.utils.UtilsApi;
import com.evernym.sdk.vcx.vcx.AlreadyInitializedException;
import com.evernym.sdk.vcx.vcx.InvalidAgencyResponseException;
import com.evernym.sdk.vcx.vcx.VcxApi;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import java9.util.concurrent.CompletableFuture;
import java9.util.concurrent.CompletionStage;
import pl.brightinventions.slf4android.FileLogHandlerConfiguration;
import pl.brightinventions.slf4android.LogLevel;
import pl.brightinventions.slf4android.LoggerConfiguration;

public class ConnectMeVcx {

    public static final String TAG = "ConnectMeVcx";
    public static final int LOG_MAX_SIZE_DEFAULT = 1_000_000;
    private static final int WALLET_KEY_LENGTH = 128;
    private static final String SECURE_PREF_VCXCONFIG = "me.connect.vcx.config";
    private static FileLogHandlerConfiguration fileHandler;
    private static final String[] VCX_LOGGER_NAMES = new String[]{
            "com.evernym.sdk.vcx.LibVcx.native",
            "VcxException",
            "ConnectionApi",
            "CredentialApi",
            "CredentialDefApi",
            "IssuerApi",
            "DisclosedProofApi",
            "ProofApi",
            "SchemaApi",
            "TokenApi",
            "UtilsApi",
            "VcxApi",
            "WalletApi"
    };

    private ConnectMeVcx() {
    }

    /**
     * Initialize library
     *
     * @param context Main Activity context
     *                     THIS NEEDS FOR BUILD CONFIG FOR ONE TIME INIT
     * @param constants Constants from app's constants
     * @return {@link CompletableFuture}
     */
    public static @NonNull
    CompletableFuture<Void> init(Context context, Constants constants, @RawRes int genesisPool) throws JSONException {

        Logger.getInstance().setLogLevel(LogLevel.DEBUG);
        Logger.getInstance().i("Initializing SDK");
        CompletableFuture<Void> result = new CompletableFuture<>();

        createOneTimeInfo(context, constants, genesisPool)
            .whenComplete((res, ex) -> {
                if (ex != null) {
                    result.completeExceptionally(ex);
                    return;
                }
                initialize(context).whenComplete((returnCode, err) -> {
                    if (err != null) {
                        Logger.getInstance().e("Init failed", err);
                        result.completeExceptionally(err);
                    } else {
                        Logger.getInstance().i("Init completed");
                        result.complete(null);
                    }
                });
        });
        return result;
    }

    /**
     * Initialize library
     *
     * @param context Main Activity context
     * @return {@link CompletableFuture}
     */
    public static @NonNull
    CompletableFuture<Void> init(Context context) {
        Logger.getInstance().setLogLevel(LogLevel.DEBUG);
        Logger.getInstance().i("Initializing SDK");
        CompletableFuture<Void> result = new CompletableFuture<>();

        CompletionStage<Void> first;
        first = CompletableFuture.completedStage(null);
        configureLoggerAndFiles(context);
        first.whenComplete((res, ex) -> {
            if (ex != null) {
                result.completeExceptionally(ex);
                return;
            }
            initialize(context).whenComplete((returnCode, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Init failed", err);
                    result.completeExceptionally(err);
                } else {
                    Logger.getInstance().i("Init completed");
                    result.complete(null);
                }
            });
        });
        return result;
    }

    private static Exception validate(Config config, Context context) {
        if (context == null) {
            return new IllegalStateException("Context must not be null");
        } else if (config.genesisPoolResId == null && config.genesisPool == null) {
            return new IllegalStateException("Genesis pool must not be null");
        } else if (config.agency == null) {
            return new IllegalStateException("Agency must not be null");
        } else if (config.walletName == null) {
            return new IllegalStateException("Wallet name must not be null");
        }
        return null;
    }

    private static void configureLoggerAndFiles(Context context) {
        Logger.getInstance().i("Configuring logger and file storage");
        for (String name : VCX_LOGGER_NAMES) {
            LoggerFactory.getLogger(name);
            LoggerConfiguration.configuration().setLogLevel(name, LogLevel.DEBUG);
        }
        Utils.makeRootDir(context);
        setVcxLogger(LOG_MAX_SIZE_DEFAULT, context);
        try {
            Os.setenv("EXTERNAL_STORAGE", Utils.getRootDir(context), true);
        } catch (ErrnoException e) {
            Logger.getInstance().e("Failed to set environment variable storage", e);
        }
    }

    public static File writeGenesisFile(Context context, Integer genesisPoolResId) {
        File genesisFile = new File(Utils.getRootDir(context), "pool_transactions_genesis");
        if (!genesisFile.exists()) {
            try (FileOutputStream stream = new FileOutputStream(genesisFile)) {
                Logger.getInstance().d("writing poolTxnGenesis to file: " + genesisFile.getAbsolutePath());
                if (genesisPoolResId != null) {
                    try (InputStream genesisStream = context.getResources().openRawResource(genesisPoolResId)) {
                        byte[] buffer = new byte[8 * 1024];
                        int bytesRead;
                        while ((bytesRead = genesisStream.read(buffer)) != -1) {
                            stream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return genesisFile;
    }

    public static File writeGenesisFile(Context context, String genesisPool) {
        File genesisFile = new File(Utils.getRootDir(context), "pool_transactions_genesis");
        if (!genesisFile.exists()) {
            try (FileOutputStream stream = new FileOutputStream(genesisFile)) {
                Logger.getInstance().d("writing poolTxnGenesis to file: " + genesisFile.getAbsolutePath());
                if (genesisPool != null) {
                    stream.write(genesisPool.getBytes());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return genesisFile;
    }

    public static boolean configAlreadyExist(Context context) {
        return SecurePreferencesHelper.containsLongStringValue(context, SECURE_PREF_VCXCONFIG);
    }

    public void shutdownVcx(Boolean deleteWallet) {
        Logger.getInstance().d(" ==> shutdownVcx() called with: deleteWallet = [" + deleteWallet);
        try {
            VcxApi.vcxShutdown(deleteWallet);
        } catch (VcxException e) {
            e.printStackTrace();
        }
    }

    private static String createWalletKey(int lengthOfKey) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[lengthOfKey];
        random.nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static boolean provisionTokenRetrieved(Activity activity, Constants constants) {
        return activity.getSharedPreferences(constants.PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(constants.PROVISION_TOKEN_RETRIEVED, false);
    }

    private static String getSavedToken(Activity activity, Constants constants) {
        return activity.getSharedPreferences(constants.PREFS_NAME, Context.MODE_PRIVATE)
                .getString(constants.PROVISION_TOKEN, null);
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

    private static void makeDir(File dir) {
        dir.mkdirs();
    }

    private static CompletableFuture<Void> createOneTimeInfo(
            Context context,
            Constants constants,
            @RawRes int genesisPool
    ) throws JSONException {
        Activity activity = (Activity) context;

        String walletName = Utils.makeWalletName(constants.WALLET_NAME);

        String walletKey = createWalletKey(WALLET_KEY_LENGTH);

        File walletDir = new File(Utils.getRootDir(context), "indy_client/wallet");
        makeDir(walletDir);
        Log.d(TAG, "Wallet dir was made");

        String walletPath = walletDir.getAbsolutePath();
        JSONObject storageConfig = new JSONObject().put("path", walletPath);

        File genesisFile = writeGenesisFile(context, genesisPool);
        String poolName = Utils.makePoolName(constants.WALLET_NAME);

        ConnectMeVcx.Config config = ConnectMeVcx.Config.builder()
                .withGenesisPool(genesisPool)
                .withWalletName(walletName)
                .withLogoUrl("https://robothash.com/logo.png")
                .withInstitutionName("real institution name")
                .withPoolName(poolName)
                .withGenesisPath(genesisFile.getAbsolutePath())
                .withWalletKey(walletKey)
                .withWalletPath(walletPath)
                .withStorageConfig(storageConfig.toString())
                .build();

        CompletableFuture<Void> result = new CompletableFuture<>();

        configureLoggerAndFiles(context);

        // We have top create thew ca cert for the openssl to work properly on android
        Utils.writeCACert(context);
        try {
            String token = retrieveToken(activity, constants);
            String agencyConfig = config.convertToVcxConfigAndMakeWalletDir();

            CompletionStage<String> provisioningStep;
            if (token != null) {
                String oneTimeInfo = UtilsApi.vcxAgentProvisionWithToken(agencyConfig, token);
                // Fixme workaround to handle exception not thrown on previous step
                //       Assume that `null` value is an error
                if (oneTimeInfo == null) {
                    throw new Exception("oneTimeInfo is null");
                }
                provisioningStep = CompletableFuture.completedStage(oneTimeInfo);
            } else {
                throw new Exception("ProvisionToken is not received ");
            }
            provisioningStep.whenComplete((oneTimeInfo, err) -> {
                if (err != null) {
                    Logger.getInstance().e("createOneTimeInfo failed: ", err);
                    result.completeExceptionally(err);
                } else {
                    Logger.getInstance().i("createOneTimeInfo called: " + oneTimeInfo);
                    try {
                        SecurePreferencesHelper.setLongStringValue(context, SECURE_PREF_VCXCONFIG, oneTimeInfo);
                        result.complete(null);
                    } catch (Exception e) {
                        result.completeExceptionally(e);
                    }
                }
            });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<Void> initialize(Context context) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        // When we restore data, then we are not calling createOneTimeInfo
        // and hence ca-crt is not written within app directory
        // since the logic to write ca cert checks for file existence
        // we won't have to pay too much cost for calling this function inside init
        Utils.writeCACert(context);
        try {
            String config = SecurePreferencesHelper.getLongStringValue(context, SECURE_PREF_VCXCONFIG, null);
            VcxApi.vcxInitWithConfig(config).whenComplete((integer, err) -> {
                if (err != null) {
                    result.completeExceptionally(err);
                } else {
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

    private static void setVcxLogger(int maxFileSizeBytes, Context context) {
        File logFile = new File(Utils.getRootDir(context), "me.connect.rotating.log");
        String logFilePath = logFile.getAbsolutePath();
        Logger.getInstance().d("Setting vcx logger to: " + logFilePath);
        initLoggerFile(context, logFilePath, maxFileSizeBytes);
    }

    private static void initLoggerFile(final Context context, String logFilePath, int maxFileSizeBytes) {
        // create the log file if it does not exist
        try {
            File file = new File(logFilePath);

            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception ex) {
            Logger.getInstance().e("Failed to create log file", ex);
            return;
        }

        // Now monitor the logFile and empty it out when it's size is
        // larger than MAX_ALLOWED_FILE_BYTES
        LogFileObserver logFileObserver = new LogFileObserver(logFilePath, maxFileSizeBytes);
        logFileObserver.startWatching();

        fileHandler = LoggerConfiguration.fileLogHandler(context);
        fileHandler.setFullFilePathPattern(logFilePath);
        fileHandler.setRotateFilesCountLimit(1);
        // Prevent slf4android from rotating the log file as we will handle that. The
        // way that we prevent slf4android from rotating the log file is to set the log
        // file size limit to 1 million bytes higher that our MAX_ALLOWED_FILE_BYTES
        fileHandler.setLogFileSizeLimitInBytes(maxFileSizeBytes + 1000000);

        for (String name : VCX_LOGGER_NAMES) {
            LoggerConfiguration.configuration().addHandlerToLogger(name, fileHandler);
        }
    }

    public static CompletableFuture<Void> updateAgentInfo(String id, String token) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            JSONObject config = new JSONObject();
            config.put("type", 3);
            config.put("id", id);
            config.put("value", "FCM:" + token);
            UtilsApi.vcxUpdateAgentInfo(config.toString()).whenComplete((v, err) -> {
                if (err != null) {
                    // Fixme workaround due to issues on agency side
                    if (err instanceof InvalidAgencyResponseException
                            && ((InvalidAgencyResponseException) err).getSdkCause().contains("data did not match any variant of untagged enum MessageTypes")) {
                        result.complete(null);
                    } else {
                        Logger.getInstance().e("Failed to update agent info", err);
                        result.completeExceptionally(err);
                    }
                } else {
                    result.complete(null);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;

    }

    public static final class ConstantsBuilder {
        private String WALLET_NAME;
        private String PREFS_NAME;
        private String SPONSEE_ID;
        private String PROVISION_TOKEN;
        private String FCM_TOKEN;
        private String FCM_TOKEN_SENT;
        private String PROVISION_TOKEN_RETRIEVED;
        private String PLACEHOLDER_SERVER_URL;
        private String SERVER_URL;

        private ConstantsBuilder() {
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

        /**
         * Build {@link Config} instance.
         *
         * @return {@link Config} instance
         */
        public @NonNull
        Constants build() {
            return new Constants(
                    WALLET_NAME,
                    PREFS_NAME,
                    SPONSEE_ID,
                    PROVISION_TOKEN,
                    FCM_TOKEN,
                    FCM_TOKEN_SENT,
                    PROVISION_TOKEN_RETRIEVED,
                    PLACEHOLDER_SERVER_URL,
                    SERVER_URL
            );
        }
    }

    /**
     * Config used during {@link ConnectMeVcx} initialization.
     */
    public static class Constants {
        private String WALLET_NAME;
        private String PREFS_NAME;
        private String SPONSEE_ID;
        private String PROVISION_TOKEN;
        private String FCM_TOKEN;
        private String FCM_TOKEN_SENT;
        private String PROVISION_TOKEN_RETRIEVED;
        private String PLACEHOLDER_SERVER_URL;
        private String SERVER_URL;

        public Constants(String WALLET_NAME,
                         String PREFS_NAME,
                         String SPONSEE_ID,
                         String PROVISION_TOKEN,
                         String FCM_TOKEN,
                         String FCM_TOKEN_SENT,
                         String PROVISION_TOKEN_RETRIEVED,
                         String PLACEHOLDER_SERVER_URL,
                         String SERVER_URL
        ) {
            this.WALLET_NAME = WALLET_NAME;
            this.PREFS_NAME = PREFS_NAME;
            this.SPONSEE_ID = SPONSEE_ID;
            this.PROVISION_TOKEN = PROVISION_TOKEN;
            this.FCM_TOKEN = FCM_TOKEN;
            this.FCM_TOKEN_SENT = FCM_TOKEN_SENT;
            this.PROVISION_TOKEN_RETRIEVED = PROVISION_TOKEN_RETRIEVED;
            this.PLACEHOLDER_SERVER_URL = PLACEHOLDER_SERVER_URL;
            this.SERVER_URL = SERVER_URL;
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
        private String agency;
        private String walletName;
        private LogLevel logLevel;
        private String logoUrl;
        private String institutionName;
        private String poolName;
        private String genesisPath;
        private String protocolVersion;
        private String walletKey;
        private String walletPath;
        private String storageConfig;

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
         * Set agency config.
         *
         * @param agency agency config
         * @return {@link ConfigBuilder} instance
         */
        public @NonNull
        ConfigBuilder withAgency(@NonNull String agency) {
            this.agency = agency;
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
         * Set log level. Default value is {@link LogLevel#INFO}
         * Please note that log level is set globally for slf4j logs.
         *
         * @param logLevel
         * @return
         */
        public @NonNull
        ConfigBuilder withLogLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        /**
         * Set logo url
         *
         * @param logoUrl logoUrl
         * @return
         */
        public @NonNull
        ConfigBuilder withLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
            return this;
        }

        /**
         * Set institution name
         *
         * @param institutionName institutionName
         * @return
         */
        public @NonNull
        ConfigBuilder withInstitutionName(String institutionName) {
            this.institutionName = institutionName;
            return this;
        }

        /**
         * Set pool name
         *
         * @param poolName poolName
         *
         * @return
         */
        public @NonNull
        ConfigBuilder withPoolName(String poolName) {
            this.poolName = poolName;
            return this;
        }

        /**
         * Set genesis path
         *
         * @param genesisPath genesisPath
         *
         * @return
         */
        public @NonNull
        ConfigBuilder withGenesisPath(String genesisPath) {
            this.genesisPath = genesisPath;
            return this;
        }

        /**
         * Set protocol version
         *
         * @param protocolVersion protocolVersion
         *
         * @return
         */
        public @NonNull
        ConfigBuilder withProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        /**
         * Set wallet key
         *
         * @param walletKey walletKey
         *
         * @return
         */
        public @NonNull
        ConfigBuilder withWalletKey(String walletKey) {
            this.walletKey = walletKey;
            return this;
        }

        /**
         * Set wallet path
         *
         * @param walletPath walletPath
         *
         * @return
         */
        public @NonNull
        ConfigBuilder withWalletPath(String walletPath) {
            this.walletPath = walletPath;
            return this;
        }

        /**
         * Set storage config
         *
         * @param storageConfig storageConfig
         *
         * @return
         */
        public @NonNull
        ConfigBuilder withStorageConfig(String storageConfig) {
            this.storageConfig = storageConfig;
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
                    genesisPool,
                    genesisPoolResId,
                    agency,
                    walletName,
                    logLevel,
                    logoUrl,
                    institutionName,
                    poolName,
                    genesisPath,
                    protocolVersion,
                    walletKey,
                    walletPath,
                    storageConfig
            );
        }
    }

    /**
     * Config used during {@link ConnectMeVcx} initialization.
     */
    public static class Config {
        private String genesisPool;
        private Integer genesisPoolResId;
        private String agency = AgencyConfig.DEFAULT;
        private String walletName;
        private LogLevel logLevel = LogLevel.INFO;
        private String logoUrl;
        private String institutionName;
        private String poolName;
        private String genesisPath;
        private String protocolVersion = "2";
        private String walletKey;
        private String walletPath;
        private String storageConfig;

        public Config(String genesisPool,
                      Integer genesisPoolResId,
                      String agency,
                      String walletName,
                      LogLevel logLevel,
                      String logoUrl,
                      String institutionName,
                      String poolName,
                      String genesisPath,
                      String protocolVersion,
                      String walletKey,
                      String walletPath,
                      String storageConfig
        ) {
            this.genesisPool = genesisPool;
            this.genesisPoolResId = genesisPoolResId;
            this.walletName = walletName;
            if (logLevel != null) {
                this.logLevel = logLevel;
            }
            this.logoUrl = logoUrl;
            this.institutionName = institutionName;
            this.poolName = poolName;
            this.genesisPath = genesisPath;
            if (protocolVersion != null) {
                this.protocolVersion = protocolVersion;
            }
            this.walletKey = walletKey;
            this.walletPath = walletPath;
            this.storageConfig = storageConfig;
        }

        /**
         * Creates builder for {@link Config}.
         *
         * @return {@link ConfigBuilder} instance
         */
        public static ConfigBuilder builder() {
            return new ConfigBuilder();
        }

        /**
         * Creates agency config from current config {@link Config} and make wallet dir.
         *
         * @return {@link AgencyConfig} with advanced fields
         */
        public String convertToVcxConfigAndMakeWalletDir() throws JSONException {
            JSONObject agencyConfig = new JSONObject(this.agency);
            agencyConfig.put("wallet_name", this.walletName);
            agencyConfig.put("wallet_key", this.walletKey);
            agencyConfig.put("storage_config", this.storageConfig);
            agencyConfig.put("protocol_type", "3.0");
            agencyConfig.put("path", this.genesisPath);
            agencyConfig.put("logo", this.logoUrl);
            agencyConfig.put("name", this.institutionName);
            return agencyConfig.toString();
        }

    }
}
