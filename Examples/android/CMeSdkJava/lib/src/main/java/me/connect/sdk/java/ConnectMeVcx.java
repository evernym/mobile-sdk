package me.connect.sdk.java;

import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.utils.UtilsApi;
import com.evernym.sdk.vcx.vcx.AlreadyInitializedException;
import com.evernym.sdk.vcx.vcx.VcxApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import java9.util.concurrent.CompletableFuture;

public class ConnectMeVcx {

    public static final String TAG = "ConnectMeVcx";
    public static final int LOG_MAX_SIZE_DEFAULT = 1_000_000;
    private static final int WALLET_KEY_LENGTH = 128;
    private static final String SECURE_PREF_VCXCONFIG = "me.connect.vcx.config";

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
        Log.i(TAG, "Initializing ConnectMeVcx");
        CompletableFuture<Void> result = new CompletableFuture<>();
        Exception error = validate(config);
        if (error != null) {
            result.completeExceptionally(error);
            return result;
        }
        try {
            Os.setenv("EXTERNAL_STORAGE", Utils.getRootDir(config.context), true);
        } catch (ErrnoException e) {
            Log.e(TAG, "Failed to set environment variable storage", e);
        }

        // NOTE: api.vcx_set_logger is already initialized by com.evernym.sdk.vcx.LibVcx
        String logFilePath = setVcxLogger("trace", config.logMaxSize, config.context);

        Log.d(TAG, "the log file path is: " + logFilePath);

        String wName = config.walletName + "-wallet";
        String poolName = config.walletName + "-pool";
        createWalletKey(WALLET_KEY_LENGTH).handle((walletKey, err) -> {
            if (err != null) {
                result.completeExceptionally(err);
                return null;
            }
            Log.d(TAG, "wallet key value is: " + walletKey);
            initWithConfig(wName, walletKey, poolName, config).handle((aVoid, t) -> {
                if (t != null) {
                    result.completeExceptionally(t);
                } else {
                    result.complete(null);
                }
                return null;
            });
            return null;
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

    private static CompletableFuture<Void> initWithConfig(String walletName, String walletKey, String poolName, Config config) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        File walletDir = new File(Utils.getRootDir(config.context), "indy_client/wallet");
        walletDir.mkdirs();

        String agencyConfig = null;
        String walletPath = walletDir.getAbsolutePath();
        try {
            agencyConfig = AgencyConfig.setConfigParameters(config.agency, walletName, walletKey, walletPath);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to populate agency config", e);
            result.completeExceptionally(e);
        }
        Log.d(TAG, "agencyConfig is set to: " + agencyConfig);

        createOneTimeInfo(agencyConfig, config.context).handle((oneTimeInfo, throwable) -> {
            if (throwable != null) {
                result.completeExceptionally(throwable);
                return null;
            }
            Log.d(TAG, "oneTimeInfo is set to: " + oneTimeInfo);

            String vcxConfig = null;
            if (oneTimeInfo == null) {
                if (SecurePreferencesHelper.containsLongStringValue(config.context, SECURE_PREF_VCXCONFIG)) {
                    Log.d(TAG, "found vcxConfig at key me.connect.vcxConfig");
                    vcxConfig = SecurePreferencesHelper.getLongStringValue(config.context, SECURE_PREF_VCXCONFIG, null);
                } else {
                    throw new RuntimeException("oneTimeInfo is null AND the key me.connect.vcxConfig is empty!!");
                }
            } else {
                File genesisFilePath = new File(Utils.getRootDir(config.context), "pool_transactions_genesis");
                if (!genesisFilePath.exists()) {
                    try (FileOutputStream stream = new FileOutputStream(genesisFilePath)) {
                        Log.d(TAG, "writing poolTxnGenesis to file: " + genesisFilePath.getAbsolutePath());
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
                try {
                    JSONObject json = new JSONObject(oneTimeInfo);
                    json.put("genesis_path", genesisFilePath.getAbsolutePath());
                    json.put("institution_logo_url", "https://robothash.com/logo.png");
                    json.put("institution_name", "real institution name");
                    json.put("pool_name", poolName);
                    json.put("protocol_version", "2");
                    json.put("protocol_type", "3.0");
                    vcxConfig = json.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "stored vcxConfig to key me.connect.vcxConfig: " + vcxConfig);
                SecurePreferencesHelper.setLongStringValue(config.context, SECURE_PREF_VCXCONFIG, vcxConfig);
            }

            Log.d(TAG, "vcxConfig is set to: " + vcxConfig);
            if (vcxConfig == null) {
                throw new RuntimeException("vcxConfig is null and this is  not allowed!");
            }

            // invoke initWithConfig
            init(vcxConfig, config.context).handle((returnCode, err) -> {
                if (err != null) {
                    result.completeExceptionally(err);
                    return null;
                }
                Log.e(TAG, "init with config return code is: " + returnCode);
                result.complete(null);
                return null;
            });
            return null;
        });
        return result;
    }


    public void shutdownVcx(Boolean deleteWallet) {
        Log.d(TAG, " ==> shutdownVcx() called with: deleteWallet = [" + deleteWallet);
        try {
            VcxApi.vcxShutdown(deleteWallet);
        } catch (VcxException e) {
            e.printStackTrace();
        }
    }

    private static CompletableFuture<String> createWalletKey(int lengthOfKey) {
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[lengthOfKey];
            random.nextBytes(bytes);
            String key = Base64.encodeToString(bytes, Base64.NO_WRAP);
            result.complete(key);
        } catch (Exception e) {
            Log.e(TAG, "createWalletKey: ", e);
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<String> createOneTimeInfo(String agencyConfig, Context context) {
        CompletableFuture<String> result = new CompletableFuture<>();
        Log.d(TAG, "createOneTimeInfo() called with: agencyConfig = [" + agencyConfig + "]");
        // We have top create thew ca cert for the openssl to work properly on android
        Utils.writeCACert(context);

        try {
            UtilsApi.vcxAgentProvisionAsync(agencyConfig)
                    .handle((res, err) -> {
                        if (err != null) {
                            Log.e(TAG, "createOneTimeInfo: ", err);
                            result.complete(null); // todo check
                        } else {
                            Log.i(TAG, "createOneTimeInfo: " + res);
                            result.complete(res);
                        }
                        return null;
                    });
        } catch (VcxException e) {
            e.printStackTrace();
            result.completeExceptionally(e);
        }
        return result;
    }

    private static CompletableFuture<Void> init(String config, Context context) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Log.d(TAG, " ==> init() called with: config = [" + config + "]");
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
                VcxApi.vcxInitWithConfig(config)
                        .handle((integer, err) -> {
                            if (err != null) {
                                result.completeExceptionally(err);
                            } else {
                                result.complete(null);
                            }
                            return null;
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

    private static int getLogLevel(String levelName) {
        if ("Error".equalsIgnoreCase(levelName)) {
            return 1;
        } else if ("Warning".equalsIgnoreCase(levelName) || levelName.toLowerCase().contains("warn")) {
            return 2;
        } else if ("Info".equalsIgnoreCase(levelName)) {
            return 3;
        } else if ("Debug".equalsIgnoreCase(levelName)) {
            return 4;
        } else if ("Trace".equalsIgnoreCase(levelName)) {
            return 5;
        } else {
            return 3;
        }
    }

    private static String setVcxLogger(String logLevel, int maxFileSizeBytes, Context context) {
        CompletableFuture<String> result = new CompletableFuture<>();
        File logFile = new File(Utils.getRootDir(context), "me.connect.rotating.log");
        String logFilePath = logFile.getAbsolutePath();
        Log.d(TAG, "Setting vcx logger to: " + logFilePath);
        initLoggerFile(context, logFilePath, maxFileSizeBytes);
        return logFilePath;
    }

    private static void initLoggerFile(final Context context, String logFilePath, int maxFileSizeBytes) {
        // create the log file if it does not exist
        try {
            if (!new File(logFilePath).exists()) {
                new FileWriter(logFilePath).close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        // Now monitor the logFile and empty it out when it's size is
        // larger than MAX_ALLOWED_FILE_BYTES
        LogFileObserver logFileObserver = new LogFileObserver(logFilePath, maxFileSizeBytes);
        logFileObserver.startWatching();

        pl.brightinventions.slf4android.FileLogHandlerConfiguration fileHandler = pl.brightinventions.slf4android.LoggerConfiguration.fileLogHandler(context);
        fileHandler.setFullFilePathPattern(logFilePath);
        fileHandler.setRotateFilesCountLimit(1);
        // Prevent slf4android from rotating the log file as we will handle that. The
        // way that we prevent slf4android from rotating the log file is to set the log
        // file size limit to 1 million bytes higher that our MAX_ALLOWED_FILE_BYTES
        fileHandler.setLogFileSizeLimitInBytes(maxFileSizeBytes + 1000000);
        pl.brightinventions.slf4android.LoggerConfiguration.configuration().addHandlerToRootLogger(fileHandler);

        // !!TODO: Remove the pl.brightinventions.slf4android.LoggerConfiguration.configuration() console logger
    }

    // todo add log level parameters
    public static final class ConfigBuilder {
        private Context context;
        private String genesisPool;
        private Integer genesisPoolResId;
        private String agency;
        private String walletName;
        private Integer logMaxSize;

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
         * Build {@link Config} instance.
         *
         * @return {@link Config} instance
         */
        public @NonNull
        Config build() {
            return new Config(context, genesisPool, genesisPoolResId, agency, walletName, logMaxSize);
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

        public Config(Context context, String genesisPool, Integer genesisPoolResId, String agency, String walletName, Integer logMaxSize) {
            this.context = context;
            this.genesisPool = genesisPool;
            this.genesisPoolResId = genesisPoolResId;
            this.agency = agency;
            this.walletName = walletName;
            if (logMaxSize != null) {
                this.logMaxSize = logMaxSize;
            }
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
