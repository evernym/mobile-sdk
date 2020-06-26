package me.connect.sdk.java;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;
import com.evernym.sdk.vcx.utils.UtilsApi;
import com.evernym.sdk.vcx.vcx.AlreadyInitializedException;
import com.evernym.sdk.vcx.vcx.VcxApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.security.SecureRandom;

public class ConnectMeVcx {

    public static final String TAG = "ConnectMeVcx";

    private Context context;

    public ConnectMeVcx(Context context) {
        this.context = context;
        VcxStaticData.uniqueAndroidID = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public void init() {

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

        File walletDir = new File(context.getFilesDir().getAbsolutePath() + "/.indy_client/wallet");
        walletDir.mkdirs();

        String agencyConfig = "{\"agency_url\":\"http://agency.evernym.com\",\"agency_did\":\"DwXzE7GdE5DNfsrRXJChSD\",\"agency_verkey\":\"844sJfb2snyeEugKvpY7Y4jZJk9LT6BnS6bnuKoiqbip\",\"wallet_name\":\"" + walletName + "\",\"wallet_key\":\"" + walletKey + "\",\"agent_seed\":null,\"enterprise_seed\":null,\"storage_config\": \"{\\\"path\\\":\\\"" + context.getFilesDir().getAbsolutePath() + "/.indy_client/wallet\\\"}\"}";
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
                File genesisFilePath = new File(context.getFilesDir().getAbsolutePath() + "/pool_transactions_genesis_DEMO");
                if (!genesisFilePath.exists()) {
                    try (FileOutputStream stream = new FileOutputStream(genesisFilePath)) {
                        Log.d(TAG, "writing poolTxnGenesis to file: " + genesisFilePath.getAbsolutePath());
                        stream.write(PoolTxnGenesis.POOL_TXN_GENESIS_PROD.getBytes());

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                try {
                    JSONObject json = new JSONObject(oneTimeInfo);
                    json.put("genesis_path", genesisFilePath.getAbsoluteFile());
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
        BridgeUtils.writeCACert(context);

        try {
            UtilsApi.vcxAgentProvisionAsync(agencyConfig).exceptionally((t) -> {
                Log.e(TAG, "createOneTimeInfo: ", t);
                promise.reject("FutureException", t.getMessage());
                return null;
            }).thenAccept(result -> {
                Log.d(TAG, "vcx::APP::async result Prov: " + result);
                BridgeUtils.resolveIfValid(promise, result);
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
        BridgeUtils.writeCACert(context);

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


    private static void requestPermission(final Context context, final Promise<String> promise) {
        VcxStaticData.LOGGER_PROMISE = promise;

        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            new AlertDialog.Builder(context)
                    .setMessage("permission storage")
                    .setPositiveButton("positive button", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    VcxStaticData.REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    }).show();

        } else {
            // permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    VcxStaticData.REQUEST_WRITE_EXTERNAL_STORAGE);
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
        VcxStaticData.LOG_FILE_PATH = context.getFilesDir().getAbsolutePath() +
                "/connectme.rotating." + uniqueIdentifier + ".log";
        VcxStaticData.ENCRYPTED_LOG_FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +
                "/connectme.rotating." + uniqueIdentifier + ".log.enc";
        //get the documents directory:
        Log.d(TAG, "Setting vcx logger to: " + VcxStaticData.LOG_FILE_PATH);

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //RUNTIME PERMISSION Android M
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Context currentActivity = ((MainApplication) context.getApplicationContext()).getCurrentActivity();
                if (currentActivity == null && context instanceof Activity) {
                    currentActivity = context;
                } else if (currentActivity == null) {
                    promise.reject("ERR-103", "The current activity is null!! This is not allowed!!");
                    throw new IllegalStateException("The current activity is null!! This is not allowed!!");
                }

                requestPermission(currentActivity, promise);
            } else {
                VcxStaticData.initLoggerFile(context);
                promise.resolve(VcxStaticData.LOG_FILE_PATH);
            }
        } else {
            promise.reject("ERR-102", "Media is not mounted!!");
        }

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
                    BridgeUtils.resolveIfValid(promise, connectionHandle);
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
            }).thenAccept(inviteDetails -> BridgeUtils.resolveIfValid(promise, inviteDetails));
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
                BridgeUtils.resolveIfValid(promise, state);
            });
        } catch (VcxException e) {
            promise.reject("VCXException", e.getMessage());
            e.printStackTrace();
        }
    }
}
