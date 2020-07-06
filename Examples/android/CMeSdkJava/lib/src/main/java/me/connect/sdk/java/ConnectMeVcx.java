package me.connect.sdk.java;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RawRes;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;
import com.evernym.sdk.vcx.utils.UtilsApi;
import com.evernym.sdk.vcx.vcx.AlreadyInitializedException;
import com.evernym.sdk.vcx.vcx.VcxApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;

public class ConnectMeVcx {

    public static final String TAG = "ConnectMeVcx";

    private Context context;
    private String genesisPool;
    private Integer genesisPoolResId;
    private String agency;

    private ConnectMeVcx() {
    }

    // fixme init should notify user of initialization process finished
    public void init() {
        Log.i(TAG, "Initializing ConnectMeVcx");
        try {
            Os.setenv("EXTERNAL_STORAGE", Utils.getRootDir(context), true);
        } catch (ErrnoException e) {
            Log.e(TAG, "Failed to set environment variable storage", e);
        }
        VcxStaticData.uniqueAndroidID = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        // NOTE: api.vcx_set_logger is already initialized by com.evernym.sdk.vcx.LibVcx
        setVcxLogger("trace", VcxStaticData.uniqueAndroidID, 10000000, new CompletableFuturePromise<>(logFilePath -> {
            Log.d(TAG, "the log file path is: " + logFilePath);

            String walletName = VcxStaticData.uniqueAndroidID + "-cm-wallet";
            String poolName = VcxStaticData.uniqueAndroidID + "cmpool";

            createWalletKey(128, new CompletableFuturePromise<>(walletKey -> {
                Log.d(TAG, "wallet key value is: " + walletKey);

                //if(!SecurePreferencesHelper.containsLongStringValue(context, VcxStaticData.SECURE_PREF_VCXCONFIG)) {
                //    deleteWallet(walletName, walletKey, poolName, 0);
                //} else {
                initWithConfig(walletName, walletKey, poolName);
                //}
            }, (t) -> {
                Log.e(TAG, "wallet key error is: ", t);
                return null;
            }));
        }, (t) -> {
            Log.e(TAG, "setVcxLogger error is: ", t);
            return null;
        }));
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

    public void initWithConfig(String walletName, String walletKey, String poolName) {

        File walletDir = new File(Utils.getRootDir(context), "indy_client/wallet");
        walletDir.mkdirs();

        String agencyConfig = null;
        String walletPath = walletDir.getAbsolutePath();
        try {
            agencyConfig = AgencyConfig.setConfigParameters(agency, walletName, walletKey, walletPath);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to populate agency config", e);
        }
        Log.d(TAG, "agencyConfig is set to: " + agencyConfig);

        // create the one time info
        createOneTimeInfo(agencyConfig, new CompletableFuturePromise<>(oneTimeInfo -> {

            Log.d(TAG, "oneTimeInfo is set to: " + oneTimeInfo);

            String vcxConfig = null;
            if (oneTimeInfo == null) {
                if (SecurePreferencesHelper.containsLongStringValue(context, VcxStaticData.SECURE_PREF_VCXCONFIG)) {
                    Log.d(TAG, "found vcxConfig at key me.connect.vcxConfig");
                    vcxConfig = SecurePreferencesHelper.getLongStringValue(context, VcxStaticData.SECURE_PREF_VCXCONFIG, null);
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
                SecurePreferencesHelper.setLongStringValue(context, VcxStaticData.SECURE_PREF_VCXCONFIG, vcxConfig);
            }

            Log.d(TAG, "vcxConfig is set to: " + vcxConfig);
            if (vcxConfig == null) {
                throw new RuntimeException("vcxConfig is null and this is  not allowed!");
            }

            // invoke initWithConfig
            init(vcxConfig, new CompletableFuturePromise<>(returnCode -> {
                Log.e(TAG, "init with config return code is: " + returnCode);
            }, (t) -> {
                Log.e(TAG, "init with config error is: ", t);
                return -1;
            }));
        }, (t) -> {
            Log.e(TAG, "create one time info error is: ", t);
            return null;
        }));

    }


    public void shutdownVcx(Boolean deleteWallet) {
        Log.d(TAG, " ==> shutdownVcx() called with: deleteWallet = [" + deleteWallet);
        try {
            VcxApi.vcxShutdown(deleteWallet);
        } catch (VcxException e) {
            e.printStackTrace();
        }
    }

    public void createWalletKey(int lengthOfKey, Promise<String> promise) {
        try {
            SecureRandom random = new SecureRandom();
            byte bytes[] = new byte[lengthOfKey];
            random.nextBytes(bytes);
            promise.resolve(Base64.encodeToString(bytes, Base64.NO_WRAP));
        } catch (Exception e) {
            Log.e(TAG, "createWalletKey: ", e);
            promise.reject("Exception", e.getMessage());
        }
    }

    public void createOneTimeInfo(String agencyConfig, Promise<String> promise) {
        Log.d(TAG, "createOneTimeInfo() called with: agencyConfig = [" + agencyConfig + "]");
        // We have top create thew ca cert for the openssl to work properly on android
        Utils.writeCACert(context);

        try {
            UtilsApi.vcxAgentProvisionAsync(agencyConfig).exceptionally((t) -> {
                Log.e(TAG, "createOneTimeInfo: ", t);
                promise.reject("FutureException", t.getMessage());
                return null;
            }).thenAccept(result -> {
                Log.d(TAG, "vcx::APP::async result Prov: " + result);
                Utils.resolveIfValid(promise, result);
            });
        } catch (VcxException e) {
            promise.reject("VCXException", e.getMessage());
            e.printStackTrace();
        }
    }

    public void init(String config, Promise<Integer> promise) {
        Log.d(TAG, " ==> init() called with: config = [" + config + "], promise = [" + promise + "]");
        // When we restore data, then we are not calling createOneTimeInfo
        // and hence ca-crt is not written within app directory
        // since the logic to write ca cert checks for file existence
        // we won't have to pay too much cost for calling this function inside init
        Utils.writeCACert(context);

        try {
            int retCode = VcxApi.initSovToken();
            if (retCode != 0) {
                promise.reject("Could not init nullpay", String.valueOf(retCode));
            } else {
                VcxApi.vcxInitWithConfig(config).exceptionally((t) -> {
                    Log.e(TAG, "init: ", t);
                    promise.reject("FutureException", t.getMessage());
                    return -1;
                }).thenAccept(result -> {
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
                    if (result != -1) {
                        promise.resolve(0);
                    }
                });
            }

        } catch (AlreadyInitializedException e) {
            // even if we get already initialized exception
            // then also we will resolve promise, because we don't care if vcx is already
            // initialized
            promise.resolve(0);
        } catch (VcxException e) {
            e.printStackTrace();
            promise.reject(e);
        }
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


    public String setVcxLogger(String logLevel, String uniqueIdentifier, int MAX_ALLOWED_FILE_BYTES, Promise<String> promise) {

        VcxStaticData.MAX_ALLOWED_FILE_BYTES = MAX_ALLOWED_FILE_BYTES;
        File logFile = new File(Utils.getRootDir(context), "connectme.rotating." + uniqueIdentifier + ".log");
        VcxStaticData.LOG_FILE_PATH = logFile.getAbsolutePath();
        VcxStaticData.ENCRYPTED_LOG_FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/connectme.rotating." + uniqueIdentifier + ".log.enc";
        //get the documents directory:
        Log.d(TAG, "Setting vcx logger to: " + VcxStaticData.LOG_FILE_PATH);

        VcxStaticData.initLoggerFile(context);
        promise.resolve(VcxStaticData.LOG_FILE_PATH);

        return VcxStaticData.LOG_FILE_PATH;

    }


    public void createConnectionWithInvite(String invitationId, String inviteDetails, Promise<Integer> promise) {
        Log.d(TAG, "createConnectionWithInvite() called with: invitationId = [" + invitationId + "], inviteDetails = ["
                + inviteDetails + "], promise = [" + promise + "]");
        try {
            ConnectionApi.vcxCreateConnectionWithInvite(invitationId, inviteDetails).exceptionally((t) -> {
                Log.e(TAG, "createConnectionWithInvite: ", t);
                promise.reject("FutureException", t.getMessage());
                return -1;
            }).thenAccept(connectionHandle -> {
                if (connectionHandle != -1) {
                    Utils.resolveIfValid(promise, connectionHandle);
                }
            });

        } catch (Exception e) {
            promise.reject("VCXException", e.getMessage());
        }
    }


    public void vcxAcceptInvitation(int connectionHandle, String connectionType, Promise<String> promise) {
        Log.d(TAG, "acceptInvitation() called with: connectionHandle = [" + connectionHandle + "], connectionType = ["
                + connectionType + "], promise = [" + promise + "]");
        try {
            ConnectionApi.vcxAcceptInvitation(connectionHandle, connectionType).exceptionally((t) -> {
                Log.e(TAG, "vcxAcceptInvitation: ", t);
                promise.reject("FutureException", t.getMessage());
                return null;
            }).thenAccept(inviteDetails -> Utils.resolveIfValid(promise, inviteDetails));
        } catch (VcxException e) {
            e.printStackTrace();
            promise.reject(e);
        }
    }


    public void getSerializedConnection(int connectionHandle, Promise<String> promise) {
        // TODO:KS call vcx_connection_serialize and pass connectionHandle
        try {
            ConnectionApi.connectionSerialize(connectionHandle).exceptionally((t) -> {
                Log.e(TAG, "getSerializedConnection: ", t);
                promise.reject("FutureException", t.getMessage());
                return null;
            }).thenAccept(state -> {
                Utils.resolveIfValid(promise, state);
            });
        } catch (VcxException e) {
            promise.reject("VCXException", e.getMessage());
            e.printStackTrace();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Context context;
        private String genesisPool;
        private Integer genesisPoolResId;
        private String agency;

        private Builder() {
        }

        public Builder withContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder withGenesisPool(String genesisPool) {
            this.genesisPool = genesisPool;
            return this;
        }

        public Builder withGenesisPool(@RawRes int genesisPoolResId) {
            this.genesisPoolResId = genesisPoolResId;
            return this;
        }

        public Builder withAgency(String agency) {
            this.agency = agency;
            return this;
        }

        public ConnectMeVcx build() {
            ConnectMeVcx connectMeVcx = new ConnectMeVcx();
            connectMeVcx.context = context;
            connectMeVcx.agency = this.agency;
            connectMeVcx.genesisPool = this.genesisPool;
            connectMeVcx.genesisPoolResId = this.genesisPoolResId;
            return connectMeVcx;
        }
    }
}
