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
    LayoutInflater layoutInflater;
    Context context;

    public DropDownAdapter(Context context, List<DropDown> dropDownList) {
        this.context = context;
        this.dropDownList = dropDownList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        convertView = layoutInflater.inflate(R.layout.spinner_row_item, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.spinner_item);
        textView.setText(dropDownList.get(position).getValue());
        textView.setTag(dropDownList.get(position).getTag());

        return convertView;


    }

}
