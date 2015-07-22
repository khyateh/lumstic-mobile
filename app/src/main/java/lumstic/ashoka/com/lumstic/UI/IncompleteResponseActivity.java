package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lumstic.ashoka.com.lumstic.Adapters.DBAdapter;
import lumstic.ashoka.com.lumstic.Adapters.IncompleteResponsesAdapter;
import lumstic.ashoka.com.lumstic.Models.IncompleteResponses;
import lumstic.ashoka.com.lumstic.Models.Questions;
import lumstic.ashoka.com.lumstic.Models.Surveys;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.IntentConstants;
import lumstic.ashoka.com.lumstic.Utils.LumsticApp;

public class IncompleteResponseActivity extends Activity {

    private IncompleteResponsesAdapter incompleteResponsesAdapter;
    private DBAdapter dbAdapter;
    private ActionBar actionBar;
    private LumsticApp lumsticApp;

    private Surveys surveys;
    private Questions identifierQuestion;

    private ListView listView;
    private TextView responseCount;
    private TextView surveyTitle;

    private int incompleteResponseCount = 0;
    private int identifierQuestionId = 0;

    private List<IncompleteResponses> incompleteResponseses;
    private List<Integer> incompleteResponsesId;
    private List<String> identifierQuestionAnswers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomplete_response);

        lumsticApp= (LumsticApp) getApplication();
        //setting up action bar
        actionBar = getActionBar();
        actionBar.setTitle("Incomplete Responses");
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.drawable.ic_action_ic_back);
        actionBar.setDisplayShowTitleEnabled(true);
        dbAdapter = new DBAdapter(IncompleteResponseActivity.this);

        //initialize lists
        incompleteResponseses = new ArrayList<IncompleteResponses>();
        incompleteResponsesId = new ArrayList<Integer>();
        identifierQuestionAnswers = new ArrayList<String>();
        surveys = new Surveys();

        //defining views
        responseCount = (TextView) findViewById(R.id.incomplete_response_count);
        surveyTitle = (TextView) findViewById(R.id.survey_title_text);


        surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);
        //get counts
        incompleteResponseCount = dbAdapter.getIncompleteResponse(surveys.getId());
        incompleteResponsesId = dbAdapter.getIncompleteResponsesIds(surveys.getId());
        surveyTitle.setText(surveys.getName());
        responseCount.setText(incompleteResponseCount + "");
        for (int j = 0; j < surveys.getQuestions().size(); j++) {
            if (surveys.getQuestions().get(j).getIdentifier() == 1) {
                identifierQuestion = surveys.getQuestions().get(j);
                identifierQuestionId = surveys.getQuestions().get(j).getId();
            }
        }

        for (int i = 0; i < incompleteResponseCount; i++) {
            identifierQuestionAnswers.add(dbAdapter.getAnswer(incompleteResponsesId.get(i), identifierQuestionId));
            try {
                incompleteResponseses.add(i, new IncompleteResponses(String.valueOf(incompleteResponsesId.get(i)), identifierQuestion.getContent() + " :" + "  " + identifierQuestionAnswers.get(i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        listView = (ListView) findViewById(R.id.listview);

        incompleteResponsesAdapter = new IncompleteResponsesAdapter(getApplicationContext(), incompleteResponseses, surveys);

        incompleteResponsesAdapter.notifyDataSetChanged();
        listView.setAdapter(incompleteResponsesAdapter);

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(lumsticApp.getPreferences().getBack_pressed()){
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.incomplete_response, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_fetch) {

            Intent i = new Intent(IncompleteResponseActivity.this, ActiveSurveyActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        if (id == R.id.action_logout) {
            final Dialog dialog = new Dialog(IncompleteResponseActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
            dialog.setContentView(R.layout.logout_dialog);
            dialog.show();
            Button button = (Button) dialog.findViewById(R.id.okay);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lumsticApp.getPreferences().setAccessToken("");
                    finish();
                    Intent i = new Intent(IncompleteResponseActivity.this, LoginActivity.class);
                    startActivity(i);
                    dialog.dismiss();

                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
