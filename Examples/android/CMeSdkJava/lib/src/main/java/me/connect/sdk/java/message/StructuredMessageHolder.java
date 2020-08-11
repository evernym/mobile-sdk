package me.connect.sdk.java.message;

import java.util.List;

public class StructuredMessageHolder {
    final String id;
    final String questionText;
    final String questionDetail;
    final List<Response> responses;

    public StructuredMessageHolder(String id, String questionText, String questionDetail, List<Response> responses) {
        this.id = id;
        this.questionText = questionText;
        this.questionDetail = questionDetail;
        this.responses = responses;
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

        public String getText() {
            return text;
        }

        public String getNonce() {
            return nonce;
        }
    }
}
