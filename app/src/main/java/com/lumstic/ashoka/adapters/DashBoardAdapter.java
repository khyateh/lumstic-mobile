package com.lumstic.ashoka.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.models.Surveys;
import com.lumstic.ashoka.utils.CommonUtil;
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
            ViewHolder.endDateLabel = (RobotoLightTextView) localView.findViewById(R.id.end_date);
            localView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) localView.getTag();
        }

        Surveys survey = (Surveys) getItem(i);
        if (survey != null ) {

            viewHolder.surveyName.setText(survey.getName());
            if(!survey.getName().equals(CommonUtil.SURVEY_MIDLINE_SEPARATOR_TEXT)) {
                int numRespondents = survey.getRespondentList().size();
                int completedRespondents = dbAdapter.getCompletedRespondents(survey.getId());
                int incompleteResponseCount = dbAdapter.getIncompleteResponse(survey.getId());
                viewHolder.completedSurvey.setText("Complete   " + dbAdapter.getCompleteResponse(survey.getId()));
                viewHolder.incompleteSurvey.setText("Incomplete   " + Integer.toString(incompleteResponseCount + numRespondents - completedRespondents));
                viewHolder.endDate.setText(survey.getExpiryDate());

                viewHolder.completedSurvey.setVisibility(View.VISIBLE);
                viewHolder.incompleteSurvey.setVisibility(View.VISIBLE);
                viewHolder.endDate.setVisibility(View.VISIBLE);
                viewHolder.endDateLabel.setVisibility(View.VISIBLE);
                viewHolder.surveyName.setPadding(0, 0, 0, 0);
                LinearLayout container = (LinearLayout) localView.findViewById(R.id.container);
                container.setPadding(0, 0, 0, 10);
                viewHolder.surveyName.setTextSize(19);

            }else{
                viewHolder.completedSurvey.setVisibility(View.GONE);
                viewHolder.incompleteSurvey.setVisibility(View.GONE);
                viewHolder.endDate.setVisibility(View.GONE);
                viewHolder.endDateLabel.setVisibility(View.GONE);
                viewHolder.surveyName.setPadding(8, 90, 0, 0);

                viewHolder.surveyName.setTextSize(20);


                LinearLayout container = (LinearLayout) localView.findViewById(R.id.container);
                container.setPadding(0, 0, 0, 0);
            }
        }
        return localView;
    }

    private static class ViewHolder {
        static RobotoLightTextView endDate, endDateLabel;
        RobotoRegularTextView surveyName, completedSurvey, incompleteSurvey;
    }
}
