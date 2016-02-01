package com.lumstic.ashoka.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.models.DropDown;

import java.util.List;

public class DropDownAdapter extends BaseAdapter {

    List<DropDown> dropDownList;
    LayoutInflater mInflater;
    Context mContext;

    public DropDownAdapter(Context mContext, List<DropDown> dropDownList) {
        this.mContext = mContext;
        this.dropDownList = dropDownList;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dropDownList.size();
    }

    @Override
    public Object getItem(int i) {
        return dropDownList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        View localView = convertView;

        if (localView == null) {
            viewHolder = new ViewHolder();
            localView = mInflater.inflate(R.layout.spinner_row_item, null);
            viewHolder.tvSpinner = (TextView) localView.findViewById(R.id.spinner_item);
            localView.setTag(R.string.app_name, viewHolder);
        } else {
            viewHolder = (ViewHolder) localView.getTag(R.string.app_name);
        }


        viewHolder.tvSpinner.setText(dropDownList.get(position).getValue());
        viewHolder.tvSpinner.setTag(dropDownList.get(position).getTag());

        return localView;


    }

    private static class ViewHolder {
        TextView tvSpinner;
    }

}
