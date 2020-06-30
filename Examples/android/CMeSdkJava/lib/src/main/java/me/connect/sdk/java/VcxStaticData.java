package me.connect.sdk.java;

import android.content.Context;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class VcxStaticData {

    private int addConnectionCalls = 0;

    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 501;
    public static final String SECURE_PREF_VCXCONFIG = "me.connect.vcxConfig";

    public static String LOG_FILE_PATH = "";
    public static String ENCRYPTED_LOG_FILE_PATH = "";
    public static int MAX_ALLOWED_FILE_BYTES = 10000000;
    public static LogFileObserver logFileObserver = null;
    public static Promise<String> LOGGER_PROMISE;
    public static String uniqueAndroidID = null;

    public Map<String, String> oneTimeAddConnection = new HashMap<String, String>(){{
        put("userDID", "3akhf906816kahfadhfas85");
        put("verificationKey", "3akhf906816kahfadhfas853akhf906816kahfadhfas85");
    }};

    public Map<String, String> secondTimeAddConnection = new HashMap<String, String>(){{
        put("userDID", "user1Did");
        put("verificationKey", "user1VerificationKey");
    }};

    public Map<String, String> allOtherAddConnection = new HashMap<String, String>(){{
        put("userDID", "user2Did");
        put("verificationKey", "user2VerificationKey");
    }};

    public String addConnection() {
        if (addConnectionCalls == 0) {
            addConnectionCalls += 1;
            return new JSONObject(oneTimeAddConnection).toString();
        }

        if (addConnectionCalls == 1) {
            addConnectionCalls += 1;
            return new JSONObject(secondTimeAddConnection).toString();
        }

        return new JSONObject(allOtherAddConnection).toString();
    }


    public String generateClaimRequest() {
        Map<String, String> claimRequest = new HashMap<String, String>(){{
            put("blinded_ms", "blindedMasterSecret");
            put("schema_seq_no", "1243");
            put("issuer_did", "issuerDID");
        }};

        return new JSONObject(claimRequest).toString();
    }

    public static void resolveLoggerPromise(String logFilePath) {
        if(LOGGER_PROMISE != null) {
            LOGGER_PROMISE.resolve(logFilePath);
        }
    }

    public static void rejectLoggerPromise(String code, String message) {
        if(LOGGER_PROMISE != null) {
            LOGGER_PROMISE.reject(code, message);
        }
    }

    public static void initLoggerFile(final Context context) {
        // create the log file if it does not exist
        try {
            if(! new File(VcxStaticData.LOG_FILE_PATH).exists()) {
                new FileWriter(VcxStaticData.LOG_FILE_PATH).close();
            }
        } catch(IOException ex) {
            ex.printStackTrace();
            return;
        }

        // Now monitor the logFile and empty it out when it's size is
        // larger than MAX_ALLOWED_FILE_BYTES
        VcxStaticData.logFileObserver = new LogFileObserver(VcxStaticData.LOG_FILE_PATH, VcxStaticData.MAX_ALLOWED_FILE_BYTES);
        VcxStaticData.logFileObserver.startWatching();

        pl.brightinventions.slf4android.FileLogHandlerConfiguration fileHandler = pl.brightinventions.slf4android.LoggerConfiguration.fileLogHandler(context);
        fileHandler.setFullFilePathPattern(VcxStaticData.LOG_FILE_PATH);
        fileHandler.setRotateFilesCountLimit(1);
        // Prevent slf4android from rotating the log file as we will handle that. The
        // way that we prevent slf4android from rotating the log file is to set the log
        // file size limit to 1 million bytes higher that our MAX_ALLOWED_FILE_BYTES
        fileHandler.setLogFileSizeLimitInBytes(VcxStaticData.MAX_ALLOWED_FILE_BYTES + 1000000);
        pl.brightinventions.slf4android.LoggerConfiguration.configuration().addHandlerToRootLogger(fileHandler);

        // !!TODO: Remove the pl.brightinventions.slf4android.LoggerConfiguration.configuration() console logger

    }


}
