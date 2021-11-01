package msdk.kotlin.sample.handlers

import android.content.Context
import com.evernym.sdk.vcx.VcxException
import com.evernym.sdk.vcx.wallet.WalletApi
import java9.util.concurrent.CompletableFuture
import msdk.kotlin.sample.logger.Logger
import msdk.kotlin.sample.utils.CommonUtils
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * Class containing methods to work with backups
 */
object WalletBackups {
    /**
     * Create backup archive for specified wallet
     *
     * @param context     context
     * @param walletName  name of the wallet
     * @param backupKey   key to encrypt archive
     * @param archiveName name of the archive file
     * @return Completable future with resulting file path string
     */
    fun create(
        context: Context, walletName: String, backupKey: String,
        archiveName: String
    ): CompletableFuture<String> {
        val result =
            CompletableFuture<String>()
        try {
            val walletPath =
                CommonUtils.getRootDir(context) + File.separator + "indy_client/wallet" + File.separator + CommonUtils.makeWalletName(
                    walletName
                )
            val archivePath =
                CommonUtils.getRootDir(context) + File.separator + archiveName + ".zip"
            val zipFile =
                ZipFile(archivePath, backupKey.toCharArray())
            val walletFolder = File(walletPath)
            val zipParams = ZipParameters()
            zipParams.isEncryptFiles = true
            zipParams.encryptionMethod = EncryptionMethod.AES
            zipFile.addFolder(walletFolder, zipParams)
            result.complete(archivePath)
        } catch (e: Exception) {
            result.completeExceptionally(e)
        }
        return result
    }

    /**
     * Restore wallet archive
     *
     * @param context     context
     * @param backupKey   key to decrypt archive
     * @param archivePath path to archive file
     * @return CompletableFuture
     */
    fun restore(
        context: Context,
        backupKey: String,
        archivePath: String
    ): CompletableFuture<Void?> {
        val result =
            CompletableFuture<Void?>()
        try {
            val root =
                CommonUtils.getRootDir(context) + File.separator + "indy_client/wallet"
            val zipFile =
                ZipFile(archivePath, backupKey.toCharArray())
            zipFile.extractAll(root)
            result.complete(null)
        } catch (e: Exception) {
            result.completeExceptionally(e)
        }
        return result
    }

    /**
     * Create wallet backup
     *
     * @param sourceId  - String with institution's personal identification for the user
     * @param backupKey - String representing the User's Key for securing  the exported wallet
     * @return CompletableFuture containing JSON string with serialized wallet backup
     */
    @ExperimentalWalletBackup
    fun createBackup(
        sourceId: String,
        backupKey: String
    ): CompletableFuture<String> {
        Logger.instance.i("Creating backup")
        val result =
            CompletableFuture<String>()
        try {
            WalletApi.createWalletBackup(sourceId, backupKey)
                .exceptionally { t: Throwable? ->
                    Logger.instance
                        .e("Failed to create wallet backup: ", t)
                    result.completeExceptionally(t)
                    null
                }
                .thenAccept { backupHandle: Int? ->
                    if (backupHandle == null) {
                        return@thenAccept
                    }
                    try {
                        WalletApi.serializeBackupWallet(backupHandle)
                            .exceptionally { t: Throwable? ->
                                Logger.instance
                                    .e("Failed to serialize wallet backup: ", t)
                                result.completeExceptionally(t)
                                null
                            }
                            .thenAccept { serializedHandle: String? ->
                                if (serializedHandle == null) {
                                    return@thenAccept
                                }
                                result.complete(serializedHandle)
                            }
                    } catch (e: VcxException) {
                        result.completeExceptionally(e)
                    }
                }
        } catch (e: VcxException) {
            result.completeExceptionally(e)
        }
        return result
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
    @Throws(IOException::class)
    fun generateBackupArchive(
        context: Context,
        relativePath: String,
        archivePath: String
    ): String {
        val walletPath =
            CommonUtils.getRootDir(context) + File.separator + relativePath
        val backupPath =
            CommonUtils.getRootDir(context) + File.separator + archivePath
        CommonUtils.zipFiles(walletPath, backupPath)
        return backupPath
    }

    /**
     * Perform backup process
     *
     * @param context          context
     * @param serializedBackup String containing serialized backup wallet handle
     * @return CompletableFuture containig serialized backup wallet handle
     */
    @ExperimentalWalletBackup
    fun performBackup(
        context: Context,
        serializedBackup: String
    ): CompletableFuture<String> {
        Logger.instance.i("Performing backup")
        val result =
            CompletableFuture<String>()
        try {
            val pathToArchive = generateBackupArchive(
                context,
                "indy_client",
                "backup-" + System.currentTimeMillis()
            )
            WalletApi.deserializeBackupWallet(serializedBackup)
                .exceptionally { t: Throwable? ->
                    Logger.instance
                        .e("Failed to deserialize wallet backup: ", t)
                    result.completeExceptionally(t)
                    null
                }
                .thenAccept { backupHandle: Int? ->
                    if (backupHandle == null) {
                        return@thenAccept
                    }
                    try {
                        WalletApi.backupWalletBackup(backupHandle, pathToArchive)
                            .whenComplete { v: Void?, err: Throwable? ->
                                if (err != null) {
                                    Logger.instance
                                        .e("Failed to backup wallet: ", err)
                                    result.completeExceptionally(err)
                                    return@whenComplete
                                }
                                try {
                                    WalletApi.serializeBackupWallet(backupHandle)
                                        .exceptionally { t: Throwable? ->
                                            Logger.instance
                                                .e("Failed to serialize wallet backup: ", t)
                                            result.completeExceptionally(t)
                                            null
                                        }
                                        .thenAccept { serializedHandle: String? ->
                                            if (serializedHandle == null) {
                                                return@thenAccept
                                            }
                                            result.complete(serializedHandle)
                                        }
                                } catch (e: VcxException) {
                                    result.completeExceptionally(e)
                                }
                            }
                    } catch (e: VcxException) {
                        result.completeExceptionally(e)
                    }
                }
        } catch (e: Exception) {
            result.completeExceptionally(e)
        }
        return result
    }

    /**
     * Restores wallet
     *
     * @param context   context
     * @param backupKey backup wallet key used during backup wallet creation
     * @return CompletableFuture with nothing
     */
    @ExperimentalWalletBackup
    fun restoreBackup(
        context: Context,
        backupKey: String
    ): CompletableFuture<Void?> {
        Logger.instance.i("Restoring backup")
        val result =
            CompletableFuture<Void?>()
        try {
            val config = JSONObject()
            config.put("wallet_name", "restoredWalletName")
            config.put("wallet_key", "restoredWalletKey")
            config.put("backup_key", backupKey)
            config.put("exported_wallet_path", CommonUtils.getRootDir(context) + "/restored")
            WalletApi.restoreWalletBackup(config.toString())
                .whenComplete { v: Void?, err: Throwable? ->
                    if (err != null) {
                        Logger.instance
                            .e("Failed to backup wallet: ", err)
                        result.completeExceptionally(err)
                    } else {
                        result.complete(null)
                    }
                }
        } catch (e: Exception) {
            result.completeExceptionally(e)
        }
        return result
    }

    /**
     * Loops indefinitely until wallet backup status is not changed
     *
     * @param serializedBackup string containing serialized wallet backup
     * @return string containing serialized wallet backup
     */
    @ExperimentalWalletBackup
    fun awaitBackupStatusChange(serializedBackup: String): String {
        Logger.instance.i("Awaiting backup state change")
        var count = 1
        try {
            val handle = WalletApi.deserializeBackupWallet(serializedBackup).get()
            while (true) {
                Logger.instance
                    .i("Awaiting backup state change: attempt #$count")
                val state = WalletApi.updateWalletBackupState(handle).get()
                Logger.instance
                    .i("Awaiting backup state change: update state=$state")
                if (state == 4 || state == 2) {
                    return WalletApi.serializeBackupWallet(handle).get()
                }
                count++
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            Logger.instance.e("Failed to await proof state", e)
            e.printStackTrace()
        }
        return serializedBackup
    }
}