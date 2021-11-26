package msdk.java.utils;

import android.content.Context;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class CommonUtils {
    private static final int BUFFER_SIZE = 4096;

    public static String createWalletKey() {
        int lengthOfKey = 128;
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[lengthOfKey];
        random.nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static String getRootDir(Context context) {
        return context.getFilesDir().getAbsolutePath() ;
    }

    public static String makeWalletName(String name) {
        return String.format("%s-wallet", name);
    }

    public static void zipFiles(String sourcePath, String outputPath) throws IOException {
        File source = new File(sourcePath);
        FileOutputStream dest = new FileOutputStream(outputPath);
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest))) {
            if (source.isDirectory()) {
                zipFolder(out, source, source.getParent().length());
            } else {
                writeBytes(out, source, 0);
            }
        }
    }

    private static void zipFolder(ZipOutputStream out, File folder, int basePathLength) throws IOException {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                zipFolder(out, file, basePathLength);
            } else {
                writeBytes(out, file, basePathLength);
            }
        }
    }

    private static void writeBytes(ZipOutputStream zipOutput, File inputFile, int basePathLength) throws IOException {
        String unmodifiedFilePath = inputFile.getPath();
        String relativePath = unmodifiedFilePath.substring(basePathLength);
        try (BufferedInputStream origin = new BufferedInputStream(new FileInputStream(unmodifiedFilePath), BUFFER_SIZE)) {
            ZipEntry entry = new ZipEntry(relativePath);
            entry.setTime(inputFile.lastModified());
            zipOutput.putNextEntry(entry);
            int bytesRead;
            byte[] data = new byte[BUFFER_SIZE];
            while ((bytesRead = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                zipOutput.write(data, 0, bytesRead);
            }
        }
    }

    public static JSONObject convertToJSONObject(String init) {
        try {
            if (init == null) {
                return null;
            }
            return new JSONObject(init);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static String getThreadId(JSONObject message) throws JSONException {
        String threadId;
        if (message.optJSONObject("~thread") != null) {
            threadId = message.getJSONObject("~thread").getString("thid");
        } else {
            threadId = message.getString("@id");
        }
        return threadId;
    }
}
