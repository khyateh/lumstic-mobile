package lumstic.example.com.lumstic.UI;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import lumstic.example.com.lumstic.Adapters.OurAdapter;
import lumstic.example.com.lumstic.Models.Questions;
import lumstic.example.com.lumstic.R;
import lumstic.example.com.lumstic.Utils.IntentConstants;

public class TestActivity extends Activity {

    ViewPager viewPager;
    OurAdapter ourAdapter;
    int questionCount;
    List<Questions> questionsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        viewPager = ( ViewPager)findViewById(R.id.pager);

        questionsList = new ArrayList<Questions>();
        questionsList = (List<Questions>) getIntent().getExtras().getSerializable(IntentConstants.QUESTIONS);
        questionCount = questionsList.size();
        ourAdapter = new  OurAdapter(questionsList,TestActivity.this);
        viewPager.setAdapter(ourAdapter);
    }

}
