package lumstic.ashoka.com.lumstic.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

import lumstic.ashoka.com.lumstic.Models.CompleteResponses;
import lumstic.ashoka.com.lumstic.Models.Surveys;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.UI.NewResponseActivity;
import lumstic.ashoka.com.lumstic.Utils.IntentConstants;

public class CompleteResponsesAdapter extends BaseAdapter {

    Context context;
    ViewHolder viewHolder;
    List<CompleteResponses> completeResponseses;
    Surveys surveys;

    public CompleteResponsesAdapter(Context context, List<CompleteResponses> completeResponseses, Surveys surveys) {
        this.context = context;
        this.surveys = surveys;
        this.completeResponseses = completeResponseses;
    }

    public int getCount() {
        return completeResponseses.size();
    }

    @Override
    public Object getItem(int i) {
        return completeResponseses.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.item_complete_responses, null);
        viewHolder = new ViewHolder();
        viewHolder.container = (LinearLayout) view.findViewById(R.id.container);
        viewHolder.responseNumber = (TextView) view.findViewById(R.id.response_number_text);
        viewHolder.responseText = (TextView) view.findViewById(R.id.response_description_text);
        view.setTag(viewHolder);
        final CompleteResponses completeResponses = (CompleteResponses) getItem(i);
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.responseNumber.setText("Response: " + completeResponses.getResponseNumber());
        viewHolder.responseText.setText(completeResponses.getResponseText());
        viewHolder.responseNumber.setTextIsSelectable(true);
        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewResponseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(IntentConstants.SURVEY, (java.io.Serializable) surveys);
                intent.putExtra(IntentConstants.RESPONSE_ID, Integer.parseInt(completeResponses.getResponseNumber()));
                context.startActivity(intent);
            }
        });
        return view;
    }

    private static class ViewHolder {
        TextView responseNumber, responseText;
        LinearLayout container;
    }
}
