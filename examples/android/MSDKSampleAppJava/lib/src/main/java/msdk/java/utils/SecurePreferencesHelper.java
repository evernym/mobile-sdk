package msdk.java.utils;

import android.content.Context;

import de.adorsys.android.securestoragelibrary.SecurePreferences;

public class SecurePreferencesHelper {

    private static final int chunkSize = 240;

    private static String getNumberOfChunksKey(String key) {
        return key + "_numberOfChunks";
    }

    public static String[] splitEqually(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        String[] ret = new String[((text.length() + size - 1) / size)];

        for (int start = 0, retIndex = 0; start < text.length(); start += size) {
            ret[retIndex] = text.substring(start, Math.min(text.length(), start + size));
            retIndex++;
        }
        return ret;
    }

    public static void setLongStringValue(Context context, String key, String value) {
        //String[] chunks = value.split("(?<=\\G.{240})");
        String[] chunks = splitEqually(value, chunkSize);

        SecurePreferences.setValue(context, getNumberOfChunksKey(key), chunks.length);

        for (int i = 0; i < chunks.length; ++i) {
            SecurePreferences.setValue(context, key + i, chunks[i]);
        }
    }

    public static String getLongStringValue(Context context, String key, String defaultValue) {
        int numberOfChunks = SecurePreferences.getIntValue(context, getNumberOfChunksKey(key), 0);

        if (numberOfChunks == 0) {
            return defaultValue;
        }

        StringBuffer longString = new StringBuffer();
        for (int i = 0; i < numberOfChunks; ++i) {
            longString.append(SecurePreferences.getStringValue(context, key + i, ""));
        }

        return longString.toString();
    }

    public static void removeLongStringValue(Context context, String key) {
        int numberOfChunks = SecurePreferences.getIntValue(context, getNumberOfChunksKey(key), 0);

        //(0 until numberOfChunks).map { SecurePreferences.removeValue("$key$it") }
        for (int i = 0; i < numberOfChunks; ++i) {
            SecurePreferences.removeValue(context, key + i);
        }
        SecurePreferences.removeValue(context, getNumberOfChunksKey(key));
    }

    public static boolean containsLongStringValue(Context context, String key) {
        return SecurePreferences.contains(context, getNumberOfChunksKey(key));
    }
}
