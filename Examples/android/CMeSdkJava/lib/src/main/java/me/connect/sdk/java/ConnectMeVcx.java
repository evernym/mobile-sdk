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
import java.io.InputStream;
import java.security.SecureRandom;

import java9.util.concurrent.CompletableFuture;

public class ConnectMeVcx {

    public static final String TAG = "ConnectMeVcx";
    public static final int LOG_MAX_SIZE_DEFAULT = 1_000_000;
    private static final String SECURE_PREF_VCXCONFIG = "me.connect.vcx.config";

    private Context context;
    private String genesisPool;
    private Integer genesisPoolResId;
    private String agency;
    private String walletName;
    private Integer logMaxSize = LOG_MAX_SIZE_DEFAULT;

    public ConnectMeVcx(Context context, String genesisPool, Integer genesisPoolResId, String agency, String walletName, Integer logMaxSize) {
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
     * Initializes library
     *
     * @return {@link CompletableFuture} containing nothing
     */
    public CompletableFuture<Void> init() {
        Log.i(TAG, "Initializing ConnectMeVcx");
        CompletableFuture<Void> result = new CompletableFuture<>();
        Exception error = validate();
        if (error != null) {
            result.completeExceptionally(error);
            return result;
        }
        try {
            Os.setenv("EXTERNAL_STORAGE", Utils.getRootDir(context), true);
        } catch (ErrnoException e) {
            Log.e(TAG, "Failed to set environment variable storage", e);
        }

        // NOTE: api.vcx_set_logger is already initialized by com.evernym.sdk.vcx.LibVcx
        String logFilePath = setVcxLogger("trace", logMaxSize); // todo could be moved into config param

        Log.d(TAG, "the log file path is: " + logFilePath);

        String wName = walletName + "-wallet";
        String poolName = walletName + "-pool";
        createWalletKey(128).handle((walletKey, err) -> {
            if (err != null) {
                result.completeExceptionally(err);
                return null;
            }
            Log.d(TAG, "wallet key value is: " + walletKey);

            initWithConfig(wName, walletKey, poolName).handle((aVoid, t) -> {
                if (t != null) {
                    result.completeExceptionally(t);
                    return null;
                }
                result.complete(null);
                return null;
            });
            return null;

        });

        return result;
    }

    private Exception validate() {
        if (context == null) {
            return new IllegalStateException("Context must not be null");
        } else if (genesisPoolResId == null && genesisPool == null) {
            return new IllegalStateException("Genesis pool must not be null");
        } else if (agency == null) {
            return new IllegalStateException("Agency must not be null");
        } else if (walletName == null) {
            return new IllegalStateException("Wallet name must not be null");
        }
        return null;
    }

//    public void deleteWallet(String walletName, String walletKey, String poolName, int attempts) {
//        Log.d(TAG, "trying to delete the old wallet: ");
//        init("{\"wallet_name\":\"" + walletName + "\",\"wallet_key\":\"" + walletKey + "\"}", new CompletableFuturePromise<>(returnCode -> {
//            Log.e(TAG, "simple init for deleting the wallet return code is: " + returnCode);
//            if(returnCode != -1) {
//                Log.e(TAG, "deleting the wallet: " + walletName);
//                shutdownVcx(true);
//                initWithConfig(walletName, walletKey, poolName);
//            } else if(attempts < 5) {
//                Log.e(TAG, "AGAIN trying to delete wallet... " + walletName);
//                deleteWallet(walletName, walletKey, poolName, attempts + 1);
//            } else {
//                Log.e(TAG, "Deleting wallet failed... Trying to init anyways: " + walletName);
//                shutdownVcx(true);
//                initWithConfig(walletName, walletKey, poolName);
//            }
//        }, (t) -> {
//            Log.e(TAG, "init with config error is: ", t);
//            return -1;
//        }));
//    }

    public CompletableFuture<Void> initWithConfig(String walletName, String walletKey, String poolName) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        File walletDir = new File(Utils.getRootDir(context), "indy_client/wallet");
        walletDir.mkdirs();

        String agencyConfig = null;
        String walletPath = walletDir.getAbsolutePath();
        try {
            agencyConfig = AgencyConfig.setConfigParameters(agency, walletName, walletKey, walletPath);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to populate agency config", e);
            result.completeExceptionally(e);
        }
        Log.d(TAG, "agencyConfig is set to: " + agencyConfig);

        // create the one time info
        createOneTimeInfo(agencyConfig).handle((oneTimeInfo, throwable) -> {
            if (throwable != null) {
                result.completeExceptionally(throwable);
                return null;
            }
            Log.d(TAG, "oneTimeInfo is set to: " + oneTimeInfo);

            String vcxConfig = null;
            if (oneTimeInfo == null) {
                if (SecurePreferencesHelper.containsLongStringValue(context, SECURE_PREF_VCXCONFIG)) {
                    Log.d(TAG, "found vcxConfig at key me.connect.vcxConfig");
                    vcxConfig = SecurePreferencesHelper.getLongStringValue(context, SECURE_PREF_VCXCONFIG, null);
                } else {
                    throw new RuntimeException("oneTimeInfo is null AND the key me.connect.vcxConfig is empty!!");
                }
            } else {
                File genesisFilePath = new File(Utils.getRootDir(context), "pool_transactions_genesis_DEMO");
                if (!genesisFilePath.exists()) {
                    try (FileOutputStream stream = new FileOutputStream(genesisFilePath)) {
                        Log.d(TAG, "writing poolTxnGenesis to file: " + genesisFilePath.getAbsolutePath());
                        if (genesisPool != null) {
                            stream.write(genesisPool.getBytes());
                        } else if (genesisPoolResId != null) {
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
                try {
                    JSONObject json = new JSONObject(oneTimeInfo);
                    json.put("genesis_path", genesisFilePath.getAbsolutePath());
                    json.put("institution_logo_url", "https://robothash.com/logo.png");
                    json.put("institution_name", "real institution name");
                    json.put("pool_name", poolName);
                    json.put("protocol_version", "2");
                    json.put("protocol_type", "3.0");
                    //json.put("storage_config", "{\"path\":\"" + context.getFilesDir().getAbsolutePath() + "/.indy_client/wallet\"}");
                    vcxConfig = json.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "stored vcxConfig to key me.connect.vcxConfig: " + vcxConfig);
                SecurePreferencesHelper.setLongStringValue(context, SECURE_PREF_VCXCONFIG, vcxConfig);
            }

            Log.d(TAG, "vcxConfig is set to: " + vcxConfig);
            if (vcxConfig == null) {
                throw new RuntimeException("vcxConfig is null and this is  not allowed!");
                //todo complete exceptionally
            }

            // invoke initWithConfig

            init(vcxConfig).handle((returnCode, err) -> {
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

    public CompletableFuture<String> createWalletKey(int lengthOfKey) {
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            SecureRandom random = new SecureRandom();
            byte bytes[] = new byte[lengthOfKey];
            random.nextBytes(bytes);
            String key = Base64.encodeToString(bytes, Base64.NO_WRAP);
            result.complete(key);
        } catch (Exception e) {
            Log.e(TAG, "createWalletKey: ", e);
            result.completeExceptionally(e);
        }
        return result;
    }

    public CompletableFuture<String> createOneTimeInfo(String agencyConfig) {
        CompletableFuture<String> result = new CompletableFuture<>();
        Log.d(TAG, "createOneTimeInfo() called with: agencyConfig = [" + agencyConfig + "]");
        // We have top create thew ca cert for the openssl to work properly on android
        Utils.writeCACert(context);

        try {
            UtilsApi.vcxAgentProvisionAsync(agencyConfig)
                    .handle((res, err) -> {
                        if (err != null) {
                            Log.e(TAG, "createOneTimeInfo: ", err);
                            result.completeExceptionally(err);
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

    public CompletableFuture<Integer> init(String config) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
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
                VcxApi.vcxInitWithConfig(config).exceptionally((t) -> {
                    Log.e(TAG, "init: ", t);
                    result.completeExceptionally(t);
                    return -1;
                }).thenAccept(res -> {
                    // Need to put this logic in every accept because that is how ugly Java's
                    // promise API is
                    // even if exceptionally is called, then also thenAccept block will be called
                    // we either need to switch to complete method and pass two callbacks as
                    // parameter
                    // till we change to that API, we have to live with this IF condition
                    // also reason to add this if condition is because we already rejected promise
                    // in
                    // exceptionally block, if we call promise.resolve now, then it `thenAccept`
                    // block
                    // would throw an exception that would not be caught here, because this is an
                    // async
                    // block and above try catch would not catch this exception
                    if (res != -1) {
                        result.complete(0);
                    }
                });
            }

        } catch (AlreadyInitializedException e) {
            // even if we get already initialized exception
            // then also we will resolve promise, because we don't care if vcx is already
            // initialized
            result.complete(0);
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


    public String setVcxLogger(String logLevel, int maxFileSizeBytes) {
        CompletableFuture<String> result = new CompletableFuture<>();
        File logFile = new File(Utils.getRootDir(context), "me.connect.rotating.log");
        String logFilePath = logFile.getAbsolutePath();
        /*VcxStaticData.ENCRYPTED_LOG_FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/connectme.rotating." + uniqueIdentifier + ".log.enc";*/
        //get the documents directory:
        Log.d(TAG, "Setting vcx logger to: " + logFilePath);

        VcxStaticData.initLoggerFile(context, logFilePath, maxFileSizeBytes);
        return logFilePath;
        //promise.resolve(VcxStaticData.LOG_FILE_PATH);
    }

    /**
     * Creates builder for ConnectMeVcx wrapper
     *
     * @return {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Context context;
        private String genesisPool;
        private Integer genesisPoolResId;
        private String agency;
        private String walletName;
        private Integer logMaxSize;

        private Builder() {
        }

        /**
         * Set {@link Context} that will be used by SDK.
         * Context is required to access internal file storage and shared preferences.
         *
         * @param context {@link Context} of current application
         * @return {@link Builder} instance
         */
        public @NonNull
        Builder withContext(@NonNull Context context) {
            this.context = context;
            return this;
        }

        /**
         * Set genesis pool string.
         * In case string exceeds 65,536 symbols limit, {@link #withGenesisPool(int)}} method should be used;
         *
         * @param genesisPool genesis pool string
         * @return {@link Builder} instance
         */
        public @NonNull
        Builder withGenesisPool(@NonNull String genesisPool) {

            this.genesisPool = genesisPool;
            return this;
        }

        /**
         * Set genesis pool raw resource id.
         *
         * @param genesisPoolResId raw resource ID of genesis pool
         * @return {@link Builder} instance
         */
        public @NonNull
        Builder withGenesisPool(@RawRes int genesisPoolResId) {
            this.genesisPoolResId = genesisPoolResId;
            return this;
        }

        /**
         * Set agency config.
         *
         * @param agency agency config
         * @return {@link Builder} instance
         */
        public @NonNull
        Builder withAgency(@NonNull String agency) {
            this.agency = agency;
            return this;
        }

        /**
         * Set wallet name.
         *
         * @param walletName wallet name
         * @return {@link Builder} instance
         */
        public @NonNull
        Builder withWalletName(@NonNull String walletName) {
            this.walletName = walletName;
            return this;
        }

        /**
         * Set log max size in bytes. Default value is {@link #LOG_MAX_SIZE_DEFAULT}.
         *
         * @param logMaxSize max log file size in bytes
         * @return {@link Builder} instance
         */
        public @NonNull
        Builder withLogMaxSize(int logMaxSize) {
            this.logMaxSize = logMaxSize;
            return this;
        }

        /**
         * Build {@link ConnectMeVcx} instance.
         * Call {@link #init()} to initialize library.
         *
         * @return {@link ConnectMeVcx} instance
         */
        public ConnectMeVcx build() {
            return new ConnectMeVcx(context, genesisPool, genesisPoolResId, agency, walletName, logMaxSize);
        }
    }
}
