package me.connect.sdk.java;

import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import org.slf4j.LoggerFactory;

import java.io.File;

import pl.brightinventions.slf4android.FileLogHandlerConfiguration;
import pl.brightinventions.slf4android.LogLevel;
import pl.brightinventions.slf4android.LoggerConfiguration;

public class Logger {
    private LogLevel logLevel = LogLevel.INFO;

    public static final String TAG = "MSDKSampleApp";
    public static final int LOG_MAX_SIZE_DEFAULT = 1_000_000;
    public static final String[] LOGGER_NAMES = new String[]{
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
    };

    public static Logger getInstance() {
        return Singleton.INSTANCE;
    }

    public void v(String message) {
        if (logLevel.getAndroidLevel() <= Log.VERBOSE) {
            Log.v(TAG, message);
        }
    }

    public void v(String message, Throwable throwable) {
        if (logLevel.getAndroidLevel() <= Log.VERBOSE) {
            Log.v(TAG, message, throwable);
        }
    }

    public void d(String message) {
        if (logLevel.getAndroidLevel() <= Log.DEBUG) {
            Log.d(TAG, message);
        }
    }

    public void d(String message, Throwable throwable) {
        if (logLevel.getAndroidLevel() <= Log.DEBUG) {
            Log.d(TAG, message, throwable);
        }
    }

    public void i(String message) {
        if (logLevel.getAndroidLevel() <= Log.INFO) {
            Log.i(TAG, message);
        }
    }

    public void i(String message, Throwable throwable) {
        if (logLevel.getAndroidLevel() <= Log.INFO) {
            Log.i(TAG, message, throwable);
        }
    }

    public void w(String message) {
        if (logLevel.getAndroidLevel() <= Log.WARN) {
            Log.w(TAG, message);
        }
    }

    public void w(String message, Throwable throwable) {
        if (logLevel.getAndroidLevel() <= Log.WARN) {
            Log.w(TAG, message, throwable);
        }
    }

    public void e(String message) {
        if (logLevel.getAndroidLevel() <= Log.ERROR) {
            Log.e(TAG, message);
        }
    }

    public void e(String message, Throwable throwable) {
        if (logLevel.getAndroidLevel() <= Log.ERROR) {
            Log.e(TAG, message, throwable);
        }
    }

    void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    private static class Singleton {
        private static final Logger INSTANCE = new Logger();
    }

    public static void configureLogger(Context context) {
        Logger.getInstance().i("Configuring logger and file storage");
        for (String name : Logger.LOGGER_NAMES) {
            LoggerFactory.getLogger(name);
            LoggerConfiguration.configuration().setLogLevel(name, LogLevel.DEBUG);
        }
        initLoggerFile(context);
    }

    private static void initLoggerFile(final Context context) {
        File logFile = new File(Utils.getRootDir(context), "me.connect.rotating.log");
        String logFilePath = logFile.getAbsolutePath();
        Logger.getInstance().d("Setting vcx logger to: " + logFilePath);

        // create the log file if it does not exist
        try {
            File file = new File(logFilePath);

            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception ex) {
            Logger.getInstance().e("Failed to create log file", ex);
            return;
        }

        // Now monitor the logFile and empty it out when it's size is
        // larger than MAX_ALLOWED_FILE_BYTES
        LogFileObserver logFileObserver = new LogFileObserver(logFilePath, LOG_MAX_SIZE_DEFAULT);
        logFileObserver.startWatching();

        FileLogHandlerConfiguration fileHandler = LoggerConfiguration.fileLogHandler(context);
        fileHandler.setFullFilePathPattern(logFilePath);
        fileHandler.setRotateFilesCountLimit(1);
        // Prevent slf4android from rotating the log file as we will handle that. The
        // way that we prevent slf4android from rotating the log file is to set the log
        // file size limit to 1 million bytes higher that our MAX_ALLOWED_FILE_BYTES
        fileHandler.setLogFileSizeLimitInBytes(LOG_MAX_SIZE_DEFAULT + 1000000);

        for (String name : Logger.LOGGER_NAMES) {
            LoggerConfiguration.configuration().addHandlerToLogger(name, fileHandler);
        }
    }
}
