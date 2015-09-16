package com.lumstic.ashoka.utils;

import com.lumstic.ashoka.models.Surveys;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

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
