package msdk.java.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.system.ErrnoException;
import android.system.Os;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import msdk.java.logger.Logger;

import static msdk.java.utils.CommonUtils.getBetweenStrings;
import static msdk.java.utils.CommonUtils.getFileContents;
import static msdk.java.utils.CommonUtils.getRootDir;

public class StorageUtils {
    public static void configureStoragePermissions(Context context) {
        Logger.getInstance().i("Configuring storage permissions");
        try {
            Os.setenv("EXTERNAL_STORAGE", getRootDir(context), true);
            // When we restore data, then we are not calling createOneTimeInfo
            // and hence ca-crt is not written within app directory
            // since the logic to write ca cert checks for file existence
            // we won't have to pay too much cost for calling this function inside init
            writeCACert(context);
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
                cert_file.createNewFile();
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
        File genesisFile = new File(CommonUtils.getRootDir(context), "pool_transactions_genesis");
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

    public static String createWalletDir(Context context) {
        File walletDir = new File(getRootDir(context), "indy_client/wallet");
        walletDir.mkdir();
        return walletDir.getAbsolutePath().toString();
    }
}
