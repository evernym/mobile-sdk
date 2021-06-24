package me.connect.sdk.java.sample.db;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.connect.sdk.java.message.StructuredMessageHolder.Response;

public class ResponseConverter {
    @TypeConverter
    public static List<Response> fromString(String value) {
        try {
            List<Response> responses = new ArrayList<>();
            JSONArray json = new JSONArray(value);
            for (int i = 0; i < json.length(); i++) {
                JSONObject entry = json.getJSONObject(i);
                Response response = new Response(entry.getString("text"), entry.getString("nonce"));
                responses.add(response);
            }
            return responses;
        } catch (Exception e) {
            return null;
        }
    }

    @TypeConverter
    public static String fromResponseList(List<Response> responses) {
        try {
            JSONArray json = new JSONArray();
            if (responses != null) {
                for (Response r : responses) {
                    JSONObject entry = new JSONObject();
                    entry.put("text", r.getText());
                    entry.put("nonce", r.getNonce());
                    json.put(entry);
                }
                return json.toString();
            }
            return null;
        } catch (JSONException e) {
            return null;
        }
    }

}
