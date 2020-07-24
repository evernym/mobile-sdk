package me.connect.sdk.java;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.wallet.WalletApi;

import org.json.JSONObject;

import java.io.IOException;

import java9.util.concurrent.CompletableFuture;

/**
 * Class containing methods to work with backups
 */
class WalletBackups {
    public static final String TAG = "ConnectMeVcx";

    private WalletBackups() {
    }

    /**
     * Create wallet backup
     *
     * @param sourceId  - String with institution's personal identification for the user
     * @param backupKey - String representing the User's Key for securing  the exported wallet
     * @return CompletableFuture containing JSON string with serialized wallet backup
     */
    @ExperimentalWalletBackup
    static @NonNull
    CompletableFuture<String> createBackup(@NonNull String sourceId, @NonNull String backupKey) {
        Log.i(Connections.TAG, "Creating backup");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            WalletApi.createWalletBackup(sourceId, backupKey)
                    .exceptionally(t -> {
                        Log.e(Connections.TAG, "Failed to create wallet backup: ", t);
                        result.completeExceptionally(t);
                        return null;
                    })
                    .thenAccept(backupHandle -> {
                        if (backupHandle == null) {
                            return;
                        }
                        try {
                            WalletApi.serializeBackupWallet(backupHandle)
                                    .exceptionally(t -> {
                                        Log.e(Connections.TAG, "Failed to serialize wallet backup: ", t);
                                        result.completeExceptionally(t);
                                        return null;
                                    })
                                    .thenAccept(serializedHandle -> {
                                        if (serializedHandle == null) {
                                            return;
                                        }
                                        result.complete(serializedHandle);
                                    });
                        } catch (VcxException e) {
                            result.completeExceptionally(e);
                        }
                    });
        } catch (VcxException e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     * Generate ZIP archive in directories relative to library root directory.
     *
     * @param context      Context
     * @param relativePath relative path to file or directory to put into archive
     * @param archivePath  relative archive path
     * @return absolute path of archive
     * @throws IOException in case there are problems during working with files
     */
    static String generateBackupArchive(Context context, String relativePath, String archivePath) throws IOException {
        String walletPath = Utils.getRootDir(context) + "/" + relativePath;
        String backupPath = Utils.getRootDir(context) + "/" + archivePath;
        Utils.zipFiles(walletPath, backupPath);
        return backupPath;
    }

    /**
     * Perform backup process
     *
     * @param context          context
     * @param serializedBackup String containing serialized backup wallet handle
     * @return CompletableFuture containig serialized backup wallet handle
     */
    @ExperimentalWalletBackup
    static @NonNull
    CompletableFuture<String> performBackup(@NonNull Context context, @NonNull String serializedBackup) {
        Log.i(Connections.TAG, "Performing backup");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            String pathToArchive = generateBackupArchive(context, "indy_client", "backup-" + System.currentTimeMillis());
            WalletApi.deserializeBackupWallet(serializedBackup)
                    .exceptionally(t -> {
                        Log.e(Connections.TAG, "Failed to deserialize wallet backup: ", t);
                        result.completeExceptionally(t);
                        return null;
                    })
                    .thenAccept(backupHandle -> {
                        if (backupHandle == null) {
                            return;
                        }
                        try {
                            WalletApi.backupWalletBackup(backupHandle, pathToArchive)
                                    .whenComplete((v, err) -> {
                                        if (err != null) {
                                            Log.e(Connections.TAG, "Failed to backup wallet: ", err);
                                            result.completeExceptionally(err);
                                            return;
                                        }
                                        try {
                                            WalletApi.serializeBackupWallet(backupHandle)
                                                    .exceptionally(t -> {
                                                        Log.e(Connections.TAG, "Failed to serialize wallet backup: ", t);
                                                        result.completeExceptionally(t);
                                                        return null;
                                                    })
                                                    .thenAccept(serializedHandle -> {
                                                        if (serializedHandle == null) {
                                                            return;
                                                        }
                                                        result.complete(serializedHandle);
                                                    });
                                        } catch (VcxException e) {
                                            result.completeExceptionally(e);
                                        }
                                    });
                        } catch (VcxException e) {
                            result.completeExceptionally(e);
                        }
                    });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     * Restores wallet
     *
     * @param context   context
     * @param backupKey backup wallet key used during backup wallet creation
     * @return CompletableFuture with nothing
     */
    @ExperimentalWalletBackup
    static @NonNull
    CompletableFuture<Void> restoreBackup(@NonNull Context context, @NonNull String backupKey) {
        // Todo Context object should be taken from ConnectMeVcx after merge
        Log.i(Connections.TAG, "Restoring backup");
        CompletableFuture<Void> result = new CompletableFuture<>();

        try {
            JSONObject config = new JSONObject();
            config.put("wallet_name", "restoredWalletName");
            config.put("wallet_key", "restoredWalletKey");
            config.put("backup_key", backupKey);
            config.put("exported_wallet_path", Utils.getRootDir(context) + "/restored");
            WalletApi.restoreWalletBackup(config.toString())
                    .whenComplete((v, err) -> {
                        if (err != null) {
                            Log.e(Connections.TAG, "Failed to backup wallet: ", err);
                            result.completeExceptionally(err);
                        } else {
                            result.complete(null);
                        }
                    });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    /**
     * Loops indefinitely until wallet backup status is not changed
     *
     * @param serializedBackup string containing serialized wallet backup
     * @return string containing serialized wallet backup
     */
    @ExperimentalWalletBackup
    static @NonNull
    String awaitBackupStatusChange(@NonNull String serializedBackup) {
        Log.i(Connections.TAG, "Awaiting backup state change");
        int count = 1;
        try {
            Integer handle = WalletApi.deserializeBackupWallet(serializedBackup).get();
            while (true) {
                Log.i(Connections.TAG, "Awaiting backup state change: attempt #" + count);
                Integer state = WalletApi.updateWalletBackupState(handle).get();
                Log.i(Connections.TAG, "Awaiting backup state change: update state=" + state);
                if (state == 4 || state == 2) {
                    return WalletApi.serializeBackupWallet(handle).get();
                }
                count++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Log.e(Connections.TAG, "Failed to await proof state", e);
            e.printStackTrace();
        }
        return serializedBackup;
    }
}
