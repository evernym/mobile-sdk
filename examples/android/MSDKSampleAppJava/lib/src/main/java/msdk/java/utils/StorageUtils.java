package msdk.java.utils;

import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;

import msdk.java.logger.Logger;

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
        } catch (ErrnoException e) {
            Logger.getInstance().e("Failed to set environment variable storage", e);
        }
    }
}
