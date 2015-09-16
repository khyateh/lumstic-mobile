package com.lumstic.ashoka.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.models.Surveys;

import java.util.List;


public class DashBoardAdapter extends BaseAdapter {
    Context context;

    List<Surveys> surveyList;
    DBAdapter dbAdapter;
    LayoutInflater inflater;

    public DashBoardAdapter(Context context, List<Surveys> surveyList) {
        this.context = context;
        this.surveyList = surveyList;
        dbAdapter = new DBAdapter(context);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return surveyList.size();
    }

    @Override
    public Object getItem(int i) {
        return surveyList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_active_survey, null);
            viewHolder = new ViewHolder();
            viewHolder.surveyName = (TextView) view.findViewById(R.id.survey_name_text);
            viewHolder.completedSurvey = (TextView) view.findViewById(R.id.complete_survey_text);
            viewHolder.incompleteSurvey = (TextView) view.findViewById(R.id.incomplete_survey_text);
            viewHolder.endDate = (TextView) view.findViewById(R.id.end_date_text);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }


        Surveys survey = (Surveys) getItem(i);
        if (survey != null) {
            viewHolder.completedSurvey.setText("Complete   " + dbAdapter.getCompleteResponse(survey.getId()));
            viewHolder.incompleteSurvey.setText("Incomplete   " + dbAdapter.getIncompleteResponse(survey.getId()));
            viewHolder.surveyName.setText(survey.getName());
            viewHolder.endDate.setText(survey.getExpiryDate());
        }
        return view;
    }

    private static class ViewHolder {
        TextView surveyName, completedSurvey, incompleteSurvey, endDate;
    }
}
