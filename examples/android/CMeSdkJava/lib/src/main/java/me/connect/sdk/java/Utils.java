package me.connect.sdk.java;

import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.ContextWrapper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by abdussami on 23/05/18.
 */

public class Utils {
    private static final int BUFFER_SIZE = 4096;
    private static String TAG = "BRIDGEUTILS";
    static final String ROOT_DIR = "connectMeVcx";

    public static void configureStoragePermissions(Context context) {
        Logger.getInstance().i("Configuring storage permissions");
        try {
            // When we restore data, then we are not calling createOneTimeInfo
            // and hence ca-crt is not written within app directory
            // since the logic to write ca cert checks for file existence
            // we won't have to pay too much cost for calling this function inside init
            Utils.writeCACert(context);
            Os.setenv("EXTERNAL_STORAGE", Utils.getRootDir(context), true);
        } catch (ErrnoException e) {
            Logger.getInstance().e("Failed to set environment variable storage", e);
        }
    }

    public static void writeCACert(Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        File cert_file = new File(getRootDir(cw), "cacert.pem");
        Logger.getInstance().d("writeCACert() called with: context = [" + context + "] and writing to file: " + cert_file.getAbsolutePath());
        if (!cert_file.exists()) {
            try {
                FileWriter fw = new FileWriter(cert_file);
                fw.write(generateCaCertContents());
                fw.flush();
                fw.close();
            } catch (IOException e) {
                Logger.getInstance().e("writeCACert: ", e);
            }
        } else {
            Logger.getInstance().d("cacert.pem file already exists: " + cert_file.getAbsolutePath());
        }
    }

    static String generateCaCertContents() {
        File folder = new File("/system/etc/security/cacerts");
        File[] listOfFiles = folder.listFiles();
        StringBuilder sb = new StringBuilder(99999);
        try {
            for (File certFile : listOfFiles) {
                if (certFile.isFile()) {

                    sb.append(System.lineSeparator());
                    sb.append("-----BEGIN CERTIFICATE-----");
                    sb.append(System.lineSeparator()).append(
                            getBetweenStrings(getFileContents(certFile),
                                    "-----BEGIN CERTIFICATE-----",
                                    "-----END CERTIFICATE-----"));
                    sb.append("-----END CERTIFICATE-----");
                    sb.append(System.lineSeparator());
                    sb.append(System.lineSeparator());
                }
            }
            return sb.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static File writeGenesisFile(Context context, Integer genesisPoolResId) {
        File genesisFile = new File(Utils.getRootDir(context), "pool_transactions_genesis");
        if (!genesisFile.exists()) {
            try (FileOutputStream stream = new FileOutputStream(genesisFile)) {
                Logger.getInstance().d("writing poolTxnGenesis to file: " + genesisFile.getAbsolutePath());
                if (genesisPoolResId != null) {
                    try (InputStream genesisStream = context.getResources().openRawResource(genesisPoolResId)) {
                        byte[] buffer = new byte[8 * 1024];
                        int bytesRead;
                        while ((bytesRead = genesisStream.read(buffer)) != -1) {
                            stream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return genesisFile;
    }

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

    private static String getBetweenStrings(
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

    static String getRootDir(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator + ROOT_DIR;
    }

    static String makeWalletName(String name) {
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
