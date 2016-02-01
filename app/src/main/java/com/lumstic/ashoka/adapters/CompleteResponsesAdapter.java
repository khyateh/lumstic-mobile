package com.lumstic.ashoka.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.models.CompleteResponse;
import com.lumstic.ashoka.models.Surveys;
import com.lumstic.ashoka.views.RobotoLightTextView;
import com.lumstic.ashoka.views.RobotoRegularTextView;

import java.util.List;


public class CompleteResponsesAdapter extends BaseAdapter {

    Context mContext;
    List<CompleteResponse> completeResponseList;
    Surveys surveys;
    LayoutInflater mInflater;

    public CompleteResponsesAdapter(Context mContext, List<CompleteResponse> completeResponseList, Surveys surveys) {
        this.mContext = mContext;
        this.surveys = surveys;
        this.completeResponseList = completeResponseList;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return completeResponseList.size();
    }

    @Override
    public Object getItem(int i) {
        return completeResponseList.get(i);
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

        CompleteResponse completeResponse = (CompleteResponse) getItem(i);
        if (completeResponse != null) {
            viewHolder.responseNumber.setText("Response: " + completeResponse.getResponseNumber());
            viewHolder.responseText.setText(completeResponse.getResponseText());
            viewHolder.responseNumber.setTag(completeResponse.getResponseNumber());
        }
        return localView;
    }

    private static class ViewHolder {
        RobotoRegularTextView responseNumber;
        RobotoLightTextView responseText;
    }
}
