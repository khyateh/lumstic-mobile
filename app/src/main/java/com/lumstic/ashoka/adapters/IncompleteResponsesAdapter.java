package com.lumstic.ashoka.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.models.IncompleteResponse;
import com.lumstic.ashoka.models.Surveys;

import java.util.List;


public class IncompleteResponsesAdapter extends BaseAdapter {
    Context context;
    Surveys surveys;
    List<IncompleteResponse> incompleteResponse;
    LayoutInflater inflater;

    public IncompleteResponsesAdapter(Context context, List<IncompleteResponse> incompleteResponse, Surveys surveys) {
        this.context = context;
        this.surveys = surveys;
        this.incompleteResponse = incompleteResponse;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return incompleteResponse.size();
    }

    public Object getItem(int i) {
        return incompleteResponse.get(i);
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

        final IncompleteResponse incompleteResponse = (IncompleteResponse) getItem(i);
        if (incompleteResponse != null) {
            viewHolder.responseNumber.setText("Response: " + incompleteResponse.getResponseNumber());
            viewHolder.responseNumber.setTag(incompleteResponse.getResponseNumber());
            viewHolder.responseText.setText(incompleteResponse.getResponseText());
        }
        return view;
    }

    private static class ViewHolder {
        TextView responseNumber, responseText;
    }

}
