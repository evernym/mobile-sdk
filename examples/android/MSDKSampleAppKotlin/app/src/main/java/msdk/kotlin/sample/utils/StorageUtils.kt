package msdk.kotlin.sample.utils

import android.content.Context
import android.content.ContextWrapper
import android.system.ErrnoException
import android.system.Os
import msdk.kotlin.sample.logger.Logger.Companion.instance
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException

object StorageUtils {
    fun configureStoragePermissions(context: Context) {
        instance.i("Configuring storage permissions")
        try {
            Os.setenv("EXTERNAL_STORAGE", CommonUtils.getRootDir(context), true)
            // When we restore data, then we are not calling createOneTimeInfo
            // and hence ca-crt is not written within app directory
            // since the logic to write ca cert checks for file existence
            // we won't have to pay too much cost for calling this function inside init
        } catch (e: ErrnoException) {
            instance.e(
                "Failed to set environment variable storage",
                e
            )
        }
    }
}