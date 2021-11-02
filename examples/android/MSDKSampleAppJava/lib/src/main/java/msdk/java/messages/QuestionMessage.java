package msdk.java.messages;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuestionMessage {
    final String id;
    final String type;
    final String questionText;
    final String questionDetail;
    final List<Response> responses;

    public QuestionMessage(String id, String type, String questionText, String questionDetail, List<Response> responses) {
        this.id = id;
        this.type = type;
        this.questionText = questionText;
        this.questionDetail = questionDetail;
        this.responses = responses;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getQuestionDetail() {
        return questionDetail;
    }

    public List<Response> getResponses() {
        return responses;
    }

    public static class Response {
        final String text;
        final String nonce;

        public Response(String text, String nonce) {
            this.text = text;
            this.nonce = nonce;
        }

        public JSONObject getResponse() {
            try {
                JSONObject response = new JSONObject();
                response.put("text", text);
                response.put("nonce", nonce);
                return response;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String getText() {
            return text;
        }

        public String getNonce() {
            return nonce;
        }
    }

    /**
     * Temporary method to parse structured question message JSON string and extract {@link QuestionMessage} from it.
     *
     * @param message {@link Message}
     * @return parsed {@link QuestionMessage}
     */
    public static QuestionMessage parse(Message message) {
        try {
            JSONObject msg = new JSONObject(message.getPayload());
            String id = msg.getString("@id");
            String questionText = msg.getString("question_text");
            String questionDetail = msg.getString("question_detail");
            ArrayList<Response> responses = new ArrayList<>();
            JSONArray jsonResponses = msg.getJSONArray("valid_responses");
            for (int i = 0; i < jsonResponses.length(); i++) {
                JSONObject response = jsonResponses.getJSONObject(i);
                String text = response.getString("text");
                String nonce = response.optString("nonce");
                QuestionMessage.Response res = new QuestionMessage.Response(text, nonce);
                responses.add(res);
            }
            return new QuestionMessage(id, message.getType(), questionText, questionDetail, responses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
