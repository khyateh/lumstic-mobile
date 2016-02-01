package com.lumstic.ashoka.utils;

import com.lumstic.ashoka.models.Surveys;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class JsonHelper {


    public List<Surveys> tryParsing(String rawJson) {
        try {
            JSONArray jsonArray = new JSONArray(rawJson);
            JSONParser jsonParser = new JSONParser();
            return getSortedSurveyList(jsonParser.parseSurvey(jsonArray));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    private List<Surveys> getSortedSurveyList(List<Surveys>
                                                      localSurveyList) {

        List<Surveys> sortedSurveyList = localSurveyList;
        for (int out = localSurveyList.size() - 1; out >= 0; out--) {
            for (int i = 0; i < out; i++) {
                int n = i + 1;
                long a = 0, b = 0;
                SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    a = dfDate.parse(sortedSurveyList.get(i).getPublishedOn()).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    b = dfDate.parse(sortedSurveyList.get(n).getPublishedOn()).getTime();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (a < b) {
                    Surveys temp = sortedSurveyList.get(i);
                    sortedSurveyList.set(i, sortedSurveyList.get(n));
                    sortedSurveyList.set(n, temp);
                }
            }
        }

        return sortedSurveyList;
    }

}
