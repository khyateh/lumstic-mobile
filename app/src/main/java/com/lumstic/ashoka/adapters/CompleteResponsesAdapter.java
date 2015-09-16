package com.lumstic.ashoka.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.models.CompleteResponse;
import com.lumstic.ashoka.models.Surveys;

import java.util.List;


public class CompleteResponsesAdapter extends BaseAdapter {

    Context context;

    List<CompleteResponse> completeResponse;
    Surveys surveys;
    LayoutInflater inflater;

    public CompleteResponsesAdapter(Context context, List<CompleteResponse> completeResponse, Surveys surveys) {
        this.context = context;
        this.surveys = surveys;
        this.completeResponse = completeResponse;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return completeResponse.size();
    }

    @Override
    public Object getItem(int i) {
        return completeResponse.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_complete_responses, null);
            viewHolder = new ViewHolder();
            viewHolder.responseNumber = (TextView) view.findViewById(R.id.response_number_text);
            viewHolder.responseText = (TextView) view.findViewById(R.id.response_description_text);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final CompleteResponse completeResponse = (CompleteResponse) getItem(i);
        if (completeResponse != null) {
            viewHolder.responseNumber.setText("Response: " + completeResponse.getResponseNumber());
            viewHolder.responseText.setText(completeResponse.getResponseText());
            viewHolder.responseNumber.setTag(completeResponse.getResponseNumber());
        }
        return view;
    }

    private static class ViewHolder {
        TextView responseNumber, responseText;
    }
}
