package com.lumstic.ashoka.ui;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.adapters.DBAdapter;
import com.lumstic.ashoka.adapters.IncompleteResponsesAdapter;
import com.lumstic.ashoka.models.IncompleteResponse;
import com.lumstic.ashoka.models.Questions;
import com.lumstic.ashoka.models.Surveys;
import com.lumstic.ashoka.utils.IntentConstants;
import com.lumstic.ashoka.views.RobotoRegularTextView;

import java.util.ArrayList;
import java.util.List;


public class IncompleteResponseActivity extends BaseActivity {

    private IncompleteResponsesAdapter incompleteResponsesAdapter;
    private DBAdapter dbAdapter;
    private ActionBar actionBar;

    private Surveys surveys;
    private Questions identifierQuestion;

    private ListView listView;
    private RobotoRegularTextView responseCount, surveyTitle;
    private int incompleteResponseCount = 0;
    private int identifierQuestionId = 0;

    private List<IncompleteResponse> incompleteResponseList;
    private List<Integer> incompleteResponsesId;
    private List<String> identifierQuestionAnswers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomplete_response);

        //setting up action bar
        actionBar = getActionBar();
        actionBar.setTitle("Incomplete Responses");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        dbAdapter = new DBAdapter(IncompleteResponseActivity.this);

        //initialize lists
        incompleteResponseList = new ArrayList<>();
        incompleteResponsesId = new ArrayList<>();
        identifierQuestionAnswers = new ArrayList<>();
        surveys = new Surveys();

        //defining views
        responseCount = (RobotoRegularTextView) findViewById(R.id.incomplete_response_count);
        surveyTitle = (RobotoRegularTextView) findViewById(R.id.survey_title_text);


        surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);


        //get counts
        updateUI();

        surveyTitle.setText(surveys.getName());

        for (int j = 0; j < surveys.getQuestionsList().size(); j++) {
            if (surveys.getQuestionsList().get(j).getIdentifier() == 1) {
                identifierQuestion = surveys.getQuestionsList().get(j);
                identifierQuestionId = surveys.getQuestionsList().get(j).getId();
            } else {
                identifierQuestion = surveys.getQuestionsList().get(0);
                identifierQuestionId = surveys.getQuestionsList().get(0).getId();
            }
        }

        for (int i = 0; i < incompleteResponseCount; i++) {
            identifierQuestionAnswers.add(dbAdapter.getAnswer(incompleteResponsesId.get(i), identifierQuestionId, 0));
            try {
                incompleteResponseList.add(i, new IncompleteResponse(String.valueOf(incompleteResponsesId.get(i)), identifierQuestion.getContent() + " :" + "  " + identifierQuestionAnswers.get(i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        listView = (ListView) findViewById(R.id.listview);

        incompleteResponsesAdapter = new IncompleteResponsesAdapter(getApplicationContext(), incompleteResponseList, surveys);

        incompleteResponsesAdapter.notifyDataSetChanged();
        listView.setAdapter(incompleteResponsesAdapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String responseNumber = (String) view.findViewById(R.id.response_number_text)
                        .getTag();
                Intent intent = new Intent(getApplicationContext(), NewResponseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(IntentConstants.SURVEY, surveys);
                intent.putExtra(IntentConstants.RESPONSE_ID, Integer.parseInt(responseNumber));
                startActivity(intent);
            }
        });

    }

    private void updateUI() {
        incompleteResponseCount = dbAdapter.getIncompleteResponse(surveys.getId());
        incompleteResponsesId = dbAdapter.getIncompleteResponsesIds(surveys.getId());
        responseCount.setText(Integer.toString(incompleteResponseCount));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        if (item.getItemId() == R.id.action_delete) {
            dbAdapter.deleteFromAnswerTableWithResponseID((String) info.targetView.findViewById(R.id.response_number_text).getTag());
            dbAdapter.deleteFromResponseTable(surveys.getId(), (String) info.targetView.findViewById(R.id.response_number_text).getTag());
            incompleteResponseList.remove(info.position);
            incompleteResponsesAdapter.notifyDataSetChanged();
            updateUI();
            return true;
        }
        return false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (appController.getPreferences().getBackPressed()) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_complete_responses, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_logout) {
            final Dialog dialog = new Dialog(IncompleteResponseActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
            dialog.setContentView(R.layout.logout_dialog);
            dialog.show();
            Button button = (Button) dialog.findViewById(R.id.okay);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    appController.getPreferences().setAccessToken("");

                    Intent i = new Intent(IncompleteResponseActivity.this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    dialog.dismiss();
                    finish();

                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
    }
}
