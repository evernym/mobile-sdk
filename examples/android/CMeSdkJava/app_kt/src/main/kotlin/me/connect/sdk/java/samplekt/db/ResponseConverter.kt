package me.connect.sdk.java.samplekt.db

import androidx.room.TypeConverter
import me.connect.sdk.java.message.StructuredMessageHolder.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class ResponseConverter {
    @TypeConverter
    fun fromString(value: String?): List<Response> = try {
        val responses = mutableListOf<Response>()
        val json = JSONArray(value)
        for (i in 0 until json.length()) {
            val entry = json.getJSONObject(i)
            val response = Response(entry.getString("text"), entry.getString("nonce"))
            responses.add(response)
        }
        responses
    } catch (e: Exception) {
        emptyList()
    }

    @TypeConverter
    fun fromResponseList(responses: List<Response>?): String {
        try {
            val json = JSONArray()
            if (responses != null) {
                responses.forEach { r ->
                    val entry = JSONObject()
                    entry.put("text", r.text)
                    entry.put("nonce", r.nonce)
                    json.put(entry)
                }
                return json.toString()
            }
            return "[]"
        } catch (e: JSONException) {
            return "[]"
        }
    }
}
