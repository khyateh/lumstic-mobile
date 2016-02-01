package com.lumstic.ashoka.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.models.Surveys;
import com.lumstic.ashoka.views.RobotoLightTextView;
import com.lumstic.ashoka.views.RobotoRegularTextView;

import java.util.List;


public class DashBoardAdapter extends BaseAdapter {
    Context mContext;
    List<Surveys> surveyList;
    DBAdapter dbAdapter;
    LayoutInflater mInflater;

    public DashBoardAdapter(Context mContext, List<Surveys> surveyList) {
        this.mContext = mContext;
        this.surveyList = surveyList;
        dbAdapter = new DBAdapter(mContext);
        mInflater = (LayoutInflater) mContext
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
        View localView = view;
        if (localView == null) {
            localView = mInflater.inflate(R.layout.item_active_survey, null);
            viewHolder = new ViewHolder();
            viewHolder.surveyName = (RobotoRegularTextView) localView.findViewById(R.id.survey_name_text);
            viewHolder.completedSurvey = (RobotoRegularTextView) localView.findViewById(R.id.complete_survey_text);
            viewHolder.incompleteSurvey = (RobotoRegularTextView) localView.findViewById(R.id.incomplete_survey_text);
            viewHolder.endDate = (RobotoLightTextView) localView.findViewById(R.id.end_date_text);
            localView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) localView.getTag();
        }


        Surveys survey = (Surveys) getItem(i);
        if (survey != null) {
            viewHolder.completedSurvey.setText("Complete   " + dbAdapter.getCompleteResponse(survey.getId()));
            viewHolder.incompleteSurvey.setText("Incomplete   " + dbAdapter.getIncompleteResponse(survey.getId()));
            viewHolder.surveyName.setText(survey.getName());
            viewHolder.endDate.setText(survey.getExpiryDate());
        }
        return localView;
    }

    private static class ViewHolder {
        RobotoLightTextView endDate;
        RobotoRegularTextView surveyName, completedSurvey, incompleteSurvey;
    }
}
