package com.lumstic.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.lumstic.data.R;
import com.lumstic.data.models.IncompleteResponse;
import com.lumstic.data.models.Surveys;
import com.lumstic.data.views.RobotoLightTextView;
import com.lumstic.data.views.RobotoRegularTextView;

import java.util.ArrayList;
import java.util.List;


public class IncompleteResponsesAdapter extends BaseAdapter implements Filterable {
    Context mContext;
    Surveys surveys;
    List<IncompleteResponse> incompleteResponseList;
    List<IncompleteResponse> OriginalSet = new ArrayList<IncompleteResponse>();
    LayoutInflater mInflater;

    public IncompleteResponsesAdapter(Context mContext, List<IncompleteResponse> incompleteResponseList, Surveys surveys) {
        this.mContext = mContext;
        this.surveys = surveys;
        this.incompleteResponseList = incompleteResponseList;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void Clear(){
        incompleteResponseList.clear();
        notifyDataSetChanged();
        OriginalSet.clear();
    }

    public void refresh() {
        notifyDataSetChanged();
        OriginalSet.clear();
        OriginalSet.addAll(incompleteResponseList);
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

            viewHolder = new ViewHolder();
            viewHolder.responseNumber = (RobotoRegularTextView) localView.findViewById(R.id.response_number_text);
            viewHolder.responseText = (RobotoLightTextView) localView.findViewById(R.id.response_description_text);
            localView.setTag(viewHolder);



        } else {
            viewHolder = (ViewHolder) localView.getTag();
        }

        IncompleteResponse incompleteResponse = (IncompleteResponse) getItem(i);
        if (incompleteResponse != null) {
            if(incompleteResponse.getRespondent()!=null){
                viewHolder.responseNumber.setText("Pending Survey");
            }else {
                viewHolder.responseNumber.setText("Response: " + incompleteResponse.getResponseNumber());
            }
            viewHolder.responseNumber.setTag(incompleteResponse.getResponseNumber());
            viewHolder.responseText.setText(incompleteResponse.getResponseText());
        }
        return localView;
    }

    @Override
    public Filter getFilter() {
        return new incompleteFilter() ;
    }

    private static class ViewHolder {
        RobotoRegularTextView responseNumber;
        RobotoLightTextView responseText;
    }

    private class incompleteFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            incompleteResponseList.clear();
            if(constraint == null || constraint.length() == 0){
                incompleteResponseList.addAll(OriginalSet);
            }else{
                for(IncompleteResponse j: OriginalSet){
                    if(j.getResponseText().toString().toLowerCase().contains(constraint.toString().toLowerCase()) )
                        incompleteResponseList.add(j);
                }
            }
            result.values = incompleteResponseList;
            result.count = incompleteResponseList.size();
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notifyDataSetChanged();
        }

    }

}


