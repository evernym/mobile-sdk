package me.connect.sdk.java.message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class StructuredMessageHolder {
    final String id;
    final String type;
    final String questionText;
    final String questionDetail;
    final List<Response> responses;

    public StructuredMessageHolder(String id, String type, String questionText, String questionDetail, List<Response> responses) {
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
}
