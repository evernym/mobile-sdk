package msdk.java.utils;

import android.content.Context;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

public class CommonUtils {
    private static final int BUFFER_SIZE = 4096;

    public static String createWalletKey() {
        int lengthOfKey = 128;
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[lengthOfKey];
        random.nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    static String getFileContents(File file) throws IOException {
        StringBuffer text = new StringBuffer(99999);
        FileInputStream fileStream = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
        for (String line; (line = br.readLine()) != null; )
            text.append(line + System.lineSeparator());
        return text.toString();
    }

    public static String getBetweenStrings(
            String text,
            String textFrom,
            String textTo) {

        String result = "";

        // Cut the beginning of the text to not occasionally meet a
        // 'textTo' value in it:
        result =
                text.substring(
                        text.indexOf(textFrom) + textFrom.length(),
                        text.length());

        // Cut the excessive ending of the text:
        result =
                result.substring(
                        0,
                        result.indexOf(textTo));

        return result;
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
}
