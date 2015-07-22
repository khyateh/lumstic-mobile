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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lumstic.ashoka.com.lumstic.Adapters.CompleteResponsesAdapter;
import lumstic.ashoka.com.lumstic.Adapters.DBAdapter;
import lumstic.ashoka.com.lumstic.Models.CompleteResponses;
import lumstic.ashoka.com.lumstic.Models.Questions;
import lumstic.ashoka.com.lumstic.Models.Surveys;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.IntentConstants;
import lumstic.ashoka.com.lumstic.Utils.LumsticApp;

public class CompleteResponsesActivity extends Activity {

    private DBAdapter dbAdapter;
    private ActionBar actionBar;

    private ListView listView;
    private TextView responseCount;
    private TextView surveyTitle;
    private LinearLayout uploadContainer;
    private LumsticApp lumsticApp;

    private Surveys surveys;
    private Questions identifierQuestion;
    private CompleteResponsesAdapter completeResponsesAdapter;

    private int completeResponseCount = 0;
    private int identifierQuestionId = 0;

    private List<CompleteResponses> completeResponseses;
    private List<Integer> completeResponsesId;
    private List<String> identifierQuestionAnswers;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_responses);
        //setting up action bar
        actionBar = getActionBar();
        actionBar.setTitle("Completed Responses");
        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_ic_back);
        actionBar.setDisplayShowTitleEnabled(true);
        //declaring adapter and array lists
        dbAdapter = new DBAdapter(CompleteResponsesActivity.this);
        completeResponseses = new ArrayList<CompleteResponses>();
        completeResponsesId = new ArrayList<Integer>();
        identifierQuestionAnswers = new ArrayList<String>();
        lumsticApp= (LumsticApp) getApplication();
        //setting up views
        responseCount = (TextView) findViewById(R.id.complete_response_count);
        surveyTitle = (TextView) findViewById(R.id.survey_title_text);
        uploadContainer = (LinearLayout) findViewById(R.id.upload_container);
        surveys = new Surveys();
        //get survey object from previous activity
        surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);
        completeResponseCount = dbAdapter.getCompleteResponse(surveys.getId());
        completeResponsesId = dbAdapter.getCompleteResponsesIds(surveys.getId());
        //if count not available set gone
        if (completeResponseCount == 0) {
            uploadContainer.setVisibility(View.GONE);
        }
        surveyTitle.setText(surveys.getName());
        responseCount.setText(completeResponseCount + "");
        for (int j = 0; j < surveys.getQuestions().size(); j++) {
            if (surveys.getQuestions().get(j).getIdentifier() == 1) {
                try {
                    identifierQuestion = surveys.getQuestions().get(j);
                    identifierQuestionId = surveys.getQuestions().get(j).getId();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            for (int i = 0; i < completeResponseCount; i++) {
                identifierQuestionAnswers.add(dbAdapter.getAnswer(completeResponsesId.get(i), identifierQuestionId));
                completeResponseses.add(i, new CompleteResponses(String.valueOf(completeResponsesId.get(i)), identifierQuestion.getContent() + " :" + "  " + identifierQuestionAnswers.get(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        listView = (ListView) findViewById(R.id.listview);
        completeResponsesAdapter = new CompleteResponsesAdapter(getApplicationContext(), completeResponseses, surveys);
        listView.setAdapter(completeResponsesAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.complete_responses, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_fetch) {

            Intent i = new Intent(CompleteResponsesActivity.this, ActiveSurveyActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        if (id == R.id.action_logout) {
            final Dialog dialog = new Dialog(CompleteResponsesActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
            dialog.setContentView(R.layout.logout_dialog);
            dialog.show();
            Button button = (Button) dialog.findViewById(R.id.okay);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lumsticApp.getPreferences().setAccessToken("");
                    finish();
                    Intent i = new Intent(CompleteResponsesActivity.this, LoginActivity.class);
                    startActivity(i);
                    dialog.dismiss();

                }
            });
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
