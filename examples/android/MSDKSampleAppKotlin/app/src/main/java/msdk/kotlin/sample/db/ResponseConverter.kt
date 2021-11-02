package msdk.kotlin.sample.db

import androidx.room.TypeConverter
import msdk.kotlin.sample.messages.QuestionMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class ResponseConverter {
    @TypeConverter
    fun fromString(value: String?): List<QuestionMessage.Response> = try {
        val responses = mutableListOf<QuestionMessage.Response>()
        val json = JSONArray(value)
        for (i in 0 until json.length()) {
            val entry = json.getJSONObject(i)
            val response = QuestionMessage.Response(entry.getString("text"), entry.getString("nonce"))
            responses.add(response)
        }
        responses
    } catch (e: Exception) {
        emptyList()
    }

    @TypeConverter
    fun fromResponseList(responses: List<QuestionMessage.Response>?): String {
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
