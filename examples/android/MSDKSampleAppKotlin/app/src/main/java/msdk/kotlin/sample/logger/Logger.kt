package msdk.kotlin.sample.logger

import android.content.Context
import android.util.Log
import msdk.kotlin.sample.utils.CommonUtils
import org.slf4j.LoggerFactory
import pl.brightinventions.slf4android.LogLevel
import pl.brightinventions.slf4android.LoggerConfiguration
import java.io.File

class Logger {
    private var logLevel = LogLevel.INFO
    fun v(message: String?) {
        if (logLevel.androidLevel <= Log.VERBOSE) {
            Log.v(TAG, message!!)
        }
    }

    fun v(message: String?, throwable: Throwable?) {
        if (logLevel.androidLevel <= Log.VERBOSE) {
            Log.v(TAG, message, throwable)
        }
    }

    fun d(message: String?) {
        if (logLevel.androidLevel <= Log.DEBUG) {
            Log.d(TAG, message!!)
        }
    }

    fun d(message: String?, throwable: Throwable?) {
        if (logLevel.androidLevel <= Log.DEBUG) {
            Log.d(TAG, message, throwable)
        }
    }

    fun i(message: String?) {
        if (logLevel.androidLevel <= Log.INFO) {
            Log.i(TAG, message!!)
        }
    }

    fun i(message: String?, throwable: Throwable?) {
        if (logLevel.androidLevel <= Log.INFO) {
            Log.i(TAG, message, throwable)
        }
    }

    fun w(message: String?) {
        if (logLevel.androidLevel <= Log.WARN) {
            Log.w(TAG, message!!)
        }
    }

    fun w(message: String?, throwable: Throwable?) {
        if (logLevel.androidLevel <= Log.WARN) {
            Log.w(TAG, message, throwable)
        }
    }

    fun e(message: String?) {
        if (logLevel.androidLevel <= Log.ERROR) {
            Log.e(TAG, message!!)
        }
    }

    fun e(message: String?, throwable: Throwable?) {
        if (logLevel.androidLevel <= Log.ERROR) {
            Log.e(TAG, message, throwable)
        }
    }

    fun setLogLevel(logLevel: LogLevel) {
        this.logLevel = logLevel
    }

    private object Singleton {
        val INSTANCE = Logger()
    }

    companion object {
        const val TAG = "MSDKSampleApp"
        const val LOG_MAX_SIZE_DEFAULT = 1000000
        val LOGGER_NAMES = arrayOf(
            "com.evernym.sdk.vcx.LibVcx.native",
            "VcxException",
            "ConnectionApi",
            "CredentialApi",
            "CredentialDefApi",
            "IssuerApi",
            "DisclosedProofApi",
            "ProofApi",
            "SchemaApi",
            "TokenApi",
            "UtilsApi",
            "VcxApi",
            "WalletApi"
        )

        @JvmStatic
        val instance: Logger
            get() = Singleton.INSTANCE

        fun configureLogger(context: Context) {
            instance
                .i("Configuring logger and file storage")
            for (name in LOGGER_NAMES) {
                LoggerFactory.getLogger(name)
                LoggerConfiguration.configuration().setLogLevel(name, LogLevel.DEBUG)
            }
            initLoggerFile(context)
        }

        private fun initLoggerFile(context: Context) {
            val logFile =
                File(CommonUtils.getRootDir(context), "me.connect.rotating.log")
            val logFilePath = logFile.absolutePath
            instance
                .d("Setting vcx logger to: $logFilePath")

            // create the log file if it does not exist
            try {
                val file = File(logFilePath)
                if (!file.exists()) {
                    file.createNewFile()
                }
            } catch (ex: Exception) {
                instance
                    .e("Failed to create log file", ex)
                return
            }

            // Now monitor the logFile and empty it out when it's size is
            // larger than MAX_ALLOWED_FILE_BYTES
            val logFileObserver = LogFileObserver(
                logFilePath,
                LOG_MAX_SIZE_DEFAULT
            )
            logFileObserver.startWatching()
            val fileHandler =
                LoggerConfiguration.fileLogHandler(context)
            fileHandler.setFullFilePathPattern(logFilePath)
            fileHandler.setRotateFilesCountLimit(1)
            // Prevent slf4android from rotating the log file as we will handle that. The
            // way that we prevent slf4android from rotating the log file is to set the log
            // file size limit to 1 million bytes higher that our MAX_ALLOWED_FILE_BYTES
            fileHandler.setLogFileSizeLimitInBytes(LOG_MAX_SIZE_DEFAULT + 1000000)
            for (name in LOGGER_NAMES) {
                LoggerConfiguration.configuration().addHandlerToLogger(name, fileHandler)
            }
        }
    }
}