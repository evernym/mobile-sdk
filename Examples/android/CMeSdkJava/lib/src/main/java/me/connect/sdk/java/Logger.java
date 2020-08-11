package me.connect.sdk.java;

import android.util.Log;

import pl.brightinventions.slf4android.LogLevel;

public class Logger {
    private static final String TAG = "ConnectMeVcx";
    private LogLevel logLevel = LogLevel.INFO;

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
}
