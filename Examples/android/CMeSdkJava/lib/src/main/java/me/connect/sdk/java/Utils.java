package me.connect.sdk.java;

import android.content.Context;
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
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.ContextWrapper;

/**
 * Created by abdussami on 23/05/18.
 */

class Utils {
    private static final int BUFFER_SIZE = 4096;
    private static String TAG = "BRIDGEUTILS";
    static final String ROOT_DIR = "connectMeVcx";

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

    static boolean makeRootDir(Context context) {
        String rootDir = getRootDir(context);
        return new File(rootDir).mkdirs();
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
}
