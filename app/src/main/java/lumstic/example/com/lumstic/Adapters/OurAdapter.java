package lumstic.example.com.lumstic.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import lumstic.example.com.lumstic.Models.Questions;
import lumstic.example.com.lumstic.R;

public class OurAdapter extends PagerAdapter{

    List<Questions> questionsList;
    LayoutInflater mLayoutInflater;
    Context context;

    public OurAdapter(List<Questions> questionsList,Context context){

        this.context= context;
        this.questionsList = questionsList;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    public int getCount() {
        return questionsList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return true;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        LinearLayout  linearLayout= (LinearLayout) itemView.findViewById(R.id.container);

        Questions questions= questionsList.get(position);


        if (questions.getType().equals("SingleLineQuestion")) {

            LinearLayout nestedContainer = new LinearLayout(context);
            nestedContainer.setOrientation(LinearLayout.VERTICAL);
            TextView questionTextSingleLine = new TextView(context);
            questionTextSingleLine.setTextSize(18);
            questionTextSingleLine.setTextColor(Color.BLACK);
            questionTextSingleLine.setPadding(8, 12, 8, 20);
            questionTextSingleLine.setText("Q. " + questions.getOrderNumber() + ")" + "   " + questions.getContent());
            nestedContainer.addView(questionTextSingleLine);
            nestedContainer.addView(mLayoutInflater.inflate(R.layout.answer_single_line, null));
            nestedContainer.setId(questions.getId());
            nestedContainer.setTag(questions);
            //idList.add(ques.getId());
            linearLayout.addView(nestedContainer);
            //answer = (EditText) findViewById(R.id.answer_text);
            //checkHint();
        }



        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
