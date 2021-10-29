package msdk.kotlin.sample.utils

import android.content.Context
import de.adorsys.android.securestoragelibrary.SecurePreferences

object SecurePreferencesHelper {
    private const val chunkSize = 240
    private fun getNumberOfChunksKey(key: String): String {
        return key + "_numberOfChunks"
    }

    fun splitEqually(text: String, size: Int): Array<String?> {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        val ret =
            arrayOfNulls<String>((text.length + size - 1) / size)
        var start = 0
        var retIndex = 0
        while (start < text.length) {
            ret[retIndex] = text.substring(start, Math.min(text.length, start + size))
            retIndex++
            start += size
        }
        return ret
    }

    fun setLongStringValue(
        context: Context?,
        key: String,
        value: String
    ) {
        //String[] chunks = value.split("(?<=\\G.{240})");
        val chunks =
            splitEqually(value, chunkSize)
        SecurePreferences.setValue(
            context!!,
            getNumberOfChunksKey(key),
            chunks.size
        )
        for (i in chunks.indices) {
            SecurePreferences.setValue(context, key + i, chunks[i]!!)
        }
    }

    fun getLongStringValue(
        context: Context?,
        key: String
    ): String? {
        val numberOfChunks = SecurePreferences.getIntValue(
            context!!,
            getNumberOfChunksKey(key),
            0
        )
        if (numberOfChunks == 0) {
            return null
        }
        val longString = StringBuffer()
        for (i in 0 until numberOfChunks) {
            longString.append(SecurePreferences.getStringValue(context, key + i, ""))
        }
        return longString.toString()
    }

    fun removeLongStringValue(context: Context?, key: String) {
        val numberOfChunks = SecurePreferences.getIntValue(
            context!!,
            getNumberOfChunksKey(key),
            0
        )

        //(0 until numberOfChunks).map { SecurePreferences.removeValue("$key$it") }
        for (i in 0 until numberOfChunks) {
            SecurePreferences.removeValue(context, key + i)
        }
        SecurePreferences.removeValue(context, getNumberOfChunksKey(key))
    }

    fun containsLongStringValue(
        context: Context?,
        key: String
    ): Boolean {
        return SecurePreferences.contains(
            context!!,
            getNumberOfChunksKey(key)
        )
    }
}