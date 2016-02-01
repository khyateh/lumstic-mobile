package com.lumstic.ashoka.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.models.IncompleteResponse;
import com.lumstic.ashoka.models.Surveys;
import com.lumstic.ashoka.views.RobotoLightTextView;
import com.lumstic.ashoka.views.RobotoRegularTextView;

import java.util.List;


public class IncompleteResponsesAdapter extends BaseAdapter {
    Context mContext;
    Surveys surveys;
    List<IncompleteResponse> incompleteResponseList;
    LayoutInflater mInflater;

    public IncompleteResponsesAdapter(Context mContext, List<IncompleteResponse> incompleteResponseList, Surveys surveys) {
        this.mContext = mContext;
        this.surveys = surveys;
        this.incompleteResponseList = incompleteResponseList;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return incompleteResponseList.size();
    }

    public Object getItem(int i) {
        return incompleteResponseList.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        View localView = view;
        if (localView == null) {
            localView = mInflater.inflate(R.layout.item_complete_responses, null);
            viewHolder = new ViewHolder();
            viewHolder.responseNumber = (RobotoRegularTextView) localView.findViewById(R.id.response_number_text);
            viewHolder.responseText = (RobotoLightTextView) localView.findViewById(R.id.response_description_text);
            localView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) localView.getTag();
        }

        IncompleteResponse incompleteResponse = (IncompleteResponse) getItem(i);
        if (incompleteResponse != null) {
            viewHolder.responseNumber.setText("Response: " + incompleteResponse.getResponseNumber());
            viewHolder.responseNumber.setTag(incompleteResponse.getResponseNumber());
            viewHolder.responseText.setText(incompleteResponse.getResponseText());
        }
        return localView;
    }

    private static class ViewHolder {
        RobotoRegularTextView responseNumber;
        RobotoLightTextView responseText;
    }

}
