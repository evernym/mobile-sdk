package me.connect.sdk.java;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VcxStaticData {

    public static LogFileObserver logFileObserver = null;
    //public static String uniqueAndroidID = null;


    public static void initLoggerFile(final Context context, String logFilePath, int maxFileSizeBytes) {
        // create the log file if it does not exist
        try {
            if (!new File(logFilePath).exists()) {
                new FileWriter(logFilePath).close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        // Now monitor the logFile and empty it out when it's size is
        // larger than MAX_ALLOWED_FILE_BYTES
        VcxStaticData.logFileObserver = new LogFileObserver(logFilePath, maxFileSizeBytes);
        VcxStaticData.logFileObserver.startWatching();

        pl.brightinventions.slf4android.FileLogHandlerConfiguration fileHandler = pl.brightinventions.slf4android.LoggerConfiguration.fileLogHandler(context);
        fileHandler.setFullFilePathPattern(logFilePath);
        fileHandler.setRotateFilesCountLimit(1);
        // Prevent slf4android from rotating the log file as we will handle that. The
        // way that we prevent slf4android from rotating the log file is to set the log
        // file size limit to 1 million bytes higher that our MAX_ALLOWED_FILE_BYTES
        fileHandler.setLogFileSizeLimitInBytes(maxFileSizeBytes + 1000000);
        pl.brightinventions.slf4android.LoggerConfiguration.configuration().addHandlerToRootLogger(fileHandler);

        // !!TODO: Remove the pl.brightinventions.slf4android.LoggerConfiguration.configuration() console logger

    }


}
