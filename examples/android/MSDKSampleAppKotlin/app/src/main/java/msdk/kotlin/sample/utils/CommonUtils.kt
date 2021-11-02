package msdk.kotlin.sample.utils

import android.content.Context
import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object CommonUtils {
    private const val BUFFER_SIZE = 4096
    fun readDataFromUrl(url: String?): String? {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(url!!)
            .build()
        try {
            val response = client.newCall(request).execute()
            return response.body!!.string()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun createWalletKey(): String {
        val lengthOfKey = 128
        val random = SecureRandom()
        val bytes = ByteArray(lengthOfKey)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun getRootDir(context: Context): String {
        return context.filesDir.absolutePath
    }

    fun makeWalletName(name: String?): String {
        return String.format("%s-wallet", name)
    }

    @Throws(IOException::class)
    fun zipFiles(sourcePath: String?, outputPath: String?) {
        val source = File(sourcePath)
        val dest = FileOutputStream(outputPath)
        ZipOutputStream(BufferedOutputStream(dest)).use { out ->
            if (source.isDirectory) {
                zipFolder(out, source, source.parent.length)
            } else {
                writeBytes(out, source, 0)
            }
        }
    }

    @Throws(IOException::class)
    private fun zipFolder(
        out: ZipOutputStream,
        folder: File,
        basePathLength: Int
    ) {
        val files = folder.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                zipFolder(out, file, basePathLength)
            } else {
                writeBytes(out, file, basePathLength)
            }
        }
    }

    @Throws(IOException::class)
    private fun writeBytes(
        zipOutput: ZipOutputStream,
        inputFile: File,
        basePathLength: Int
    ) {
        val unmodifiedFilePath = inputFile.path
        val relativePath = unmodifiedFilePath.substring(basePathLength)
        BufferedInputStream(
            FileInputStream(unmodifiedFilePath),
            BUFFER_SIZE
        ).use { origin ->
            val entry = ZipEntry(relativePath)
            entry.time = inputFile.lastModified()
            zipOutput.putNextEntry(entry)
            var bytesRead: Int
            val data = ByteArray(BUFFER_SIZE)
            while (origin.read(data, 0, BUFFER_SIZE).also { bytesRead = it } != -1) {
                zipOutput.write(data, 0, bytesRead)
            }
        }
    }

    fun convertToJSONObject(init: String?): JSONObject? {
        try {
            return init?.let { JSONObject(it) }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return JSONObject()
    }
}