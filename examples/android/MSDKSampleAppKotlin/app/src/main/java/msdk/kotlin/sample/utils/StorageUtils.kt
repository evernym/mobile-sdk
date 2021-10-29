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
            writeCACert(context)
        } catch (e: ErrnoException) {
            instance.e(
                "Failed to set environment variable storage",
                e
            )
        }
    }

    fun writeCACert(context: Context) {
        val cw = ContextWrapper(context)
        val cert_file = File(CommonUtils.getRootDir(cw), "cacert.pem")
        instance.d("writeCACert() called with: context = [" + context + "] and writing to file: " + cert_file.absolutePath)
        if (!cert_file.exists()) {
            try {
                cert_file.createNewFile()
                val fw = FileWriter(cert_file)
                fw.write(generateCaCertContents())
                fw.flush()
                fw.close()
            } catch (e: IOException) {
                instance.e("writeCACert: ", e)
            }
        } else {
            instance.d("cacert.pem file already exists: " + cert_file.absolutePath)
        }
    }

    fun generateCaCertContents(): String {
        val folder = File("/system/etc/security/cacerts")
        val listOfFiles = folder.listFiles()
        val sb = StringBuilder(99999)
        try {
            for (certFile in listOfFiles) {
                if (certFile.isFile) {
                    sb.append(System.lineSeparator())
                    sb.append("-----BEGIN CERTIFICATE-----")
                    sb.append(System.lineSeparator()).append(
                        CommonUtils.getBetweenStrings(
                            CommonUtils.getFileContents(certFile),
                            "-----BEGIN CERTIFICATE-----",
                            "-----END CERTIFICATE-----"
                        )
                    )
                    sb.append("-----END CERTIFICATE-----")
                    sb.append(System.lineSeparator())
                    sb.append(System.lineSeparator())
                }
            }
            return sb.toString()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return ""
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}