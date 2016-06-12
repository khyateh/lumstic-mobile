package com.lumstic.ashoka.utils;

import android.text.format.DateFormat;

import com.lumstic.ashoka.models.Respondent;
import com.lumstic.ashoka.models.Surveys;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JsonHelper {


    public List<Surveys> tryParsing(String rawJson) {
        try {
            JSONArray jsonArray = new JSONArray(rawJson);
            JSONParser jsonParser = new JSONParser();
            return getSurveyListSortedByType(getSortedSurveyList(jsonParser.parseSurvey(jsonArray)));
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

    private List<Surveys> getSurveyListSortedByType(List<Surveys>
                                                      localSurveyList) {

        List<Surveys> sortedSurveyList = localSurveyList;

        //add the separator
        Surveys sep = new Surveys();
        sep.setName(CommonUtil.SURVEY_MIDLINE_SEPARATOR_TEXT);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd hhmmss");
        dateFormatter.setLenient(false);
        Date today = new Date();
        String s = dateFormatter.format(today);
        sep.setPublishedOn(s);
        sep.setRespondentList(new ArrayList<Respondent>());
        sortedSurveyList.add(sortedSurveyList.size(),sep );

        for (int out = localSurveyList.size() - 1; out >= 0; out--) {
            for (int i = 0; i < out; i++) {
                int n = i + 1;
                long a = 0, b = 0;
                try {
                    a = sortedSurveyList.get(i).getRespondentList().size()>0?0:1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    b = sortedSurveyList.get(n).getRespondentList().size()>0?0:1;
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
