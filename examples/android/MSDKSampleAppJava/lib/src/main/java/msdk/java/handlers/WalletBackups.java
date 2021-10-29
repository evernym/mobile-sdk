package msdk.java.handlers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.wallet.WalletApi;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import java9.util.concurrent.CompletableFuture;
import msdk.java.logger.Logger;
import msdk.java.utils.CommonUtils;

/**
 * Class containing methods to work with backups
 */
public class WalletBackups {
    public static final String TAG = "ConnectMeVcx";

    private WalletBackups() {
    }

    /**
     * Create backup archive for specified wallet
     *
     * @param context     context
     * @param walletName  name of the wallet
     * @param backupKey   key to encrypt archive
     * @param archiveName name of the archive file
     * @return Completable future with resulting file path string
     */
    public static @NonNull
    CompletableFuture<String> create(@NonNull Context context, @NonNull String walletName, @NonNull String backupKey,
                                     @NonNull String archiveName) {
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            String walletPath = CommonUtils.getRootDir(context) + File.separator + "indy_client/wallet" + File.separator + CommonUtils.makeWalletName(walletName);
            String archivePath = CommonUtils.getRootDir(context) + File.separator + archiveName + ".zip";
            ZipFile zipFile = new ZipFile(archivePath, backupKey.toCharArray());
            File walletFolder = new File(walletPath);
            ZipParameters zipParams = new ZipParameters();
            zipParams.setEncryptFiles(true);
            zipParams.setEncryptionMethod(EncryptionMethod.AES);
            zipFile.addFolder(walletFolder, zipParams);
            result.complete(archivePath);
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    }

    /**
     * Restore wallet archive
     *
     * @param context     context
     * @param backupKey   key to decrypt archive
     * @param archivePath path to archive file
     * @return CompletableFuture
     */
    public static @NonNull
    CompletableFuture<Void> restore(@NonNull Context context, @NonNull String backupKey, @NonNull String archivePath) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        try {
            String root = CommonUtils.getRootDir(context) + File.separator + "indy_client/wallet";
            ZipFile zipFile = new ZipFile(archivePath, backupKey.toCharArray());
            zipFile.extractAll(root);
            result.complete(null);
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
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
        Logger.getInstance().i("Creating backup");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            WalletApi.createWalletBackup(sourceId, backupKey)
                    .exceptionally(t -> {
                        Logger.getInstance().e("Failed to create wallet backup: ", t);
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
                                        Logger.getInstance().e("Failed to serialize wallet backup: ", t);
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
        String walletPath = CommonUtils.getRootDir(context) + File.separator + relativePath;
        String backupPath = CommonUtils.getRootDir(context) + File.separator + archivePath;
        CommonUtils.zipFiles(walletPath, backupPath);
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
        Logger.getInstance().i("Performing backup");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            String pathToArchive = generateBackupArchive(context, "indy_client", "backup-" + System.currentTimeMillis());
            WalletApi.deserializeBackupWallet(serializedBackup)
                    .exceptionally(t -> {
                        Logger.getInstance().e("Failed to deserialize wallet backup: ", t);
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
                                            Logger.getInstance().e("Failed to backup wallet: ", err);
                                            result.completeExceptionally(err);
                                            return;
                                        }
                                        try {
                                            WalletApi.serializeBackupWallet(backupHandle)
                                                    .exceptionally(t -> {
                                                        Logger.getInstance().e("Failed to serialize wallet backup: ", t);
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
        Logger.getInstance().i("Restoring backup");
        CompletableFuture<Void> result = new CompletableFuture<>();

        try {
            JSONObject config = new JSONObject();
            config.put("wallet_name", "restoredWalletName");
            config.put("wallet_key", "restoredWalletKey");
            config.put("backup_key", backupKey);
            config.put("exported_wallet_path", CommonUtils.getRootDir(context) + "/restored");
            WalletApi.restoreWalletBackup(config.toString())
                    .whenComplete((v, err) -> {
                        if (err != null) {
                            Logger.getInstance().e("Failed to backup wallet: ", err);
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
        Logger.getInstance().i("Awaiting backup state change");
        int count = 1;
        try {
            Integer handle = WalletApi.deserializeBackupWallet(serializedBackup).get();
            while (true) {
                Logger.getInstance().i("Awaiting backup state change: attempt #" + count);
                Integer state = WalletApi.updateWalletBackupState(handle).get();
                Logger.getInstance().i("Awaiting backup state change: update state=" + state);
                if (state == 4 || state == 2) {
                    return WalletApi.serializeBackupWallet(handle).get();
                }
                count++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Logger.getInstance().e("Failed to await proof state", e);
            e.printStackTrace();
        }
        return serializedBackup;
    }
}
