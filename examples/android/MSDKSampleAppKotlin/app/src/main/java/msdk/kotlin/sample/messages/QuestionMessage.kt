package msdk.kotlin.sample.messages

import org.json.JSONException
import org.json.JSONObject
import java.util.*

class QuestionMessage(
    val id: String,
    val type: String?,
    val questionText: String,
    val questionDetail: String,
    val responses: List<Response>
) {

    class Response(val text: String, val nonce: String) {
        val response: JSONObject?
            get() {
                try {
                    val response = JSONObject()
                    response.put("text", text)
                    response.put("nonce", nonce)
                    return response
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                return null
            }

    }

    companion object {
        /**
         * Temporary method to parse structured question message JSON string and extract [QuestionMessage] from it.
         *
         * @param message [Message]
         * @return parsed [QuestionMessage]
         */
        fun parse(message: Message): QuestionMessage {
            return try {
                val msg = JSONObject(message.payload)
                val id = msg.getString("@id")
                val questionText = msg.getString("question_text")
                val questionDetail = msg.getString("question_detail")
                val responses =
                    ArrayList<Response>()
                val jsonResponses = msg.getJSONArray("valid_responses")
                for (i in 0 until jsonResponses.length()) {
                    val response = jsonResponses.getJSONObject(i)
                    val text = response.getString("text")
                    val nonce = response.optString("nonce")
                    val res =
                        Response(text, nonce)
                    responses.add(res)
                }
                QuestionMessage(
                    id,
                    message.type,
                    questionText,
                    questionDetail,
                    responses
                )
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

}