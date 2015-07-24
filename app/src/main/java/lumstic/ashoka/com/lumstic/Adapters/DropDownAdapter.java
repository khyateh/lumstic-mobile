package lumstic.ashoka.com.lumstic.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import lumstic.ashoka.com.lumstic.Models.DropDown;
import lumstic.ashoka.com.lumstic.R;

/**
 * Created by work on 24/7/15.
 */
public class DropDownAdapter extends BaseAdapter{

    List<DropDown> dropDowns;
    LayoutInflater layoutInflater;
    Context  context;

    public DropDownAdapter(Context context, List<DropDown> dropDowns) {
        this.context=context;
        this.dropDowns = dropDowns;
        layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dropDowns.size();
    }

    @Override
    public Object getItem(int i) {
        return dropDowns.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView=layoutInflater.inflate(R.layout.spinner_row_item,parent,false);
        TextView textView=(TextView)convertView.findViewById(R.id.spinner_item);
        textView.setText(dropDowns.get(position).getValue());
        textView.setTag(dropDowns.get(position).getTag());

        return convertView;


    }

//    @Override
//    public int getPosition(DropDown item) {
//        return super.getPosition(item);
//    }
//
//    @Override
//    public DropDown getItem(int position) {
//        return position;
//    }
}
