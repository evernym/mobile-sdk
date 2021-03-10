package test.java.utility;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

// Singleton that keeps test context variables across different test sessions
public class LocalContext {

    private static LocalContext instance;
    private static JSONObject data;

    private LocalContext() {
        data = new JSONObject();
    }

    public static LocalContext getInstance() {
        if (instance == null) {
            instance = new LocalContext();
        }
        return instance;
    }

    public void setValue(String key, String value) {
        data.put(key, value);
    }

    public void setValueList(String key, List<String> value) {
        data.put(key, value);
    }

    public String getValue(String key) {
        return data.getString(key);
    }

    public List<String> getValueList(String key) {
        List<String> list = new ArrayList<>();
        JSONArray jsonArray = (JSONArray) data.get(key);
        for (int i=0; i<jsonArray.length(); i++) {
            list.add(jsonArray.get(i).toString());
        }

        return list;
    }
}
