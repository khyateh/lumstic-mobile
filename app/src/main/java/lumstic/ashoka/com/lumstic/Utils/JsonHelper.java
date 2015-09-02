package lumstic.ashoka.com.lumstic.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import lumstic.ashoka.com.lumstic.Models.Surveys;

public class JsonHelper {


    public List<Surveys> tryParsing(String rawJson) {
        try {
            JSONArray jsonArray = new JSONArray(rawJson);
            JSONParser jsonParser = new JSONParser();
            return jsonParser.parseSurvey(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
