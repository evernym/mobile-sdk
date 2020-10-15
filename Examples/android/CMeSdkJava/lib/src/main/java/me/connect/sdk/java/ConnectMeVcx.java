package me.connect.sdk.java;

import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

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
     * @param config library config
     * @return {@link CompletableFuture}
     */
    public static @NonNull
    CompletableFuture<Void> init(Config config) {
        Logger.getInstance().setLogLevel(config.logLevel);
        Logger.getInstance().i("Initializing SDK");
        CompletableFuture<Void> result = new CompletableFuture<>();
        Exception error = validate(config);
        if (error != null) {
            result.completeExceptionally(error);
            return result;
        }
        configureLoggerAndFiles(config);

        CompletionStage<Void> first;
        if (!configAlreadyExist(config.context)) {
            first = createOneTimeInfo(config);
        } else {
            first = CompletableFuture.completedStage(null);
        }

        first.whenComplete((res, ex) -> {
            if (ex != null) {
                result.completeExceptionally(ex);
                return;
            }
            initialize(config.context).whenComplete((returnCode, err) -> {
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

    private static Exception validate(Config config) {
        if (config.context == null) {
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

    private static void configureLoggerAndFiles(Config config) {
        Logger.getInstance().i("Configuring logger and file storage");
        for (String name : VCX_LOGGER_NAMES) {
            LoggerFactory.getLogger(name);
            LoggerConfiguration.configuration().setLogLevel(name, config.logLevel);
        }
        Utils.makeRootDir(config.context);
        setVcxLogger(config.logMaxSize, config.context);
        try {
            Os.setenv("EXTERNAL_STORAGE", Utils.getRootDir(config.context), true);
        } catch (ErrnoException e) {
            Logger.getInstance().e("Failed to set environment variable storage", e);
        }
    }

    private static String prepareAgencyConfig(Config config) throws Exception {
        String walletName = Utils.makeWalletName(config.walletName);
        File walletDir = new File(Utils.getRootDir(config.context), "indy_client/wallet");
        walletDir.mkdirs();
        String walletPath = walletDir.getAbsolutePath();
        String walletKey = createWalletKey(WALLET_KEY_LENGTH);
        return AgencyConfig.setConfigParameters(config.agency, walletName, walletKey, walletPath, "3.0");
    }

    private static String populateConfig(String poolName, String oneTimeInfo, String genesisFilePath,
                                         String logoUrl, String name) throws JSONException {
        JSONObject json = new JSONObject(oneTimeInfo);
        json.put("genesis_path", genesisFilePath);
        json.put("institution_logo_url", logoUrl);
        json.put("institution_name", name);
        json.put("pool_name", poolName);
        json.put("protocol_version", "2");
        return json.toString();
    }

    private static File writeGenesisFile(Config config) {
        File genesisFile = new File(Utils.getRootDir(config.context), "pool_transactions_genesis");
        if (!genesisFile.exists()) {
            try (FileOutputStream stream = new FileOutputStream(genesisFile)) {
                Logger.getInstance().d("writing poolTxnGenesis to file: " + genesisFile.getAbsolutePath());
                if (config.genesisPool != null) {
                    stream.write(config.genesisPool.getBytes());
                } else if (config.genesisPoolResId != null) {
                    try (InputStream genesisStream = config.context.getResources().openRawResource(config.genesisPoolResId)) {
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

    private static boolean configAlreadyExist(Context context) {
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

    private static CompletableFuture<Void> createOneTimeInfo(Config config) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        // We have top create thew ca cert for the openssl to work properly on android
        Utils.writeCACert(config.context);
        try {
            String agencyConfig = prepareAgencyConfig(config);
            CompletionStage<String> provisioningStep;
            if (config.provisionToken != null) {
                String oneTimeInfo = UtilsApi.vcxAgentProvisionWithToken(agencyConfig, config.provisionToken);
                // Fixme workaround to handle exception not thrown on previous step
                //       Assume that `null` value is an error
                if (oneTimeInfo == null) {
                    throw new Exception("oneTimeInfo is null");
                }
                provisioningStep = CompletableFuture.completedStage(oneTimeInfo);
            } else {
                provisioningStep = UtilsApi.vcxAgentProvisionAsync(agencyConfig);
            }
            provisioningStep.whenComplete((oneTimeInfo, err) -> {
                if (err != null) {
                    Logger.getInstance().e("createOneTimeInfo failed: ", err);
                    result.completeExceptionally(err);
                } else {
                    Logger.getInstance().i("createOneTimeInfo called: " + oneTimeInfo);
                    try {
                        File genesisFile = writeGenesisFile(config);
                        String poolName = Utils.makePoolName(config.walletName);
                        String vcxConfig = populateConfig(poolName, oneTimeInfo, genesisFile.getAbsolutePath(),
                                "https://robothash.com/logo.png", "real institution name");
                        SecurePreferencesHelper.setLongStringValue(config.context, SECURE_PREF_VCXCONFIG, vcxConfig);
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
            int retCode = VcxApi.initSovToken();
            if (retCode != 0) {
                result.completeExceptionally(new Exception("Could not init: " + retCode));
            } else {
                String config = SecurePreferencesHelper.getLongStringValue(context, SECURE_PREF_VCXCONFIG, null);
                VcxApi.vcxInitWithConfig(config).whenComplete((integer, err) -> {
                    if (err != null) {
                        result.completeExceptionally(err);
                    } else {
                        result.complete(null);
                    }
                });
            }
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

    public static final class ConfigBuilder {
        private Context context;
        private String genesisPool;
        private Integer genesisPoolResId;
        private String agency;
        private String walletName;
        private Integer logMaxSize;
        private LogLevel logLevel;
        private String provisionToken;


        private ConfigBuilder() {
        }

        /**
         * Set {@link Context} that will be used by SDK.
         * Context is required to access internal file storage and shared preferences.
         *
         * @param context {@link Context} of current application
         * @return {@link ConfigBuilder} instance
         */
        public @NonNull
        ConfigBuilder withContext(@NonNull Context context) {
            this.context = context;
            return this;
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
         * Set log max size in bytes. Default value is {@link #LOG_MAX_SIZE_DEFAULT}.
         *
         * @param logMaxSize max log file size in bytes
         * @return {@link ConfigBuilder} instance
         */
        public @NonNull
        ConfigBuilder withLogMaxSize(int logMaxSize) {
            this.logMaxSize = logMaxSize;
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
         * Set provision token
         *
         * @param provisionToken provisionToken
         * @return
         */
        public @NonNull
        ConfigBuilder withProvisionToken(String provisionToken) {
            this.provisionToken = provisionToken;
            return this;
        }

        /**
         * Build {@link Config} instance.
         *
         * @return {@link Config} instance
         */
        public @NonNull
        Config build() {
            return new Config(context, genesisPool, genesisPoolResId, agency, walletName, logMaxSize, logLevel,
                    provisionToken);
        }
    }

    /**
     * Config used during {@link ConnectMeVcx} initialization.
     */
    public static class Config {
        private Context context;
        private String genesisPool;
        private Integer genesisPoolResId;
        private String agency;
        private String walletName;
        private Integer logMaxSize = LOG_MAX_SIZE_DEFAULT;
        private LogLevel logLevel = LogLevel.INFO;
        private String provisionToken;

        public Config(Context context, String genesisPool, Integer genesisPoolResId, String agency, String walletName,
                      Integer logMaxSize, LogLevel logLevel, String provisionToken) {
            this.context = context;
            this.genesisPool = genesisPool;
            this.genesisPoolResId = genesisPoolResId;
            this.agency = agency;
            this.walletName = walletName;
            if (logMaxSize != null) {
                this.logMaxSize = logMaxSize;
            }
            if (logLevel != null) {
                this.logLevel = logLevel;
            }
            this.provisionToken = provisionToken;
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
