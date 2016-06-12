package com.lumstic.ashoka.ui;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.lumstic.ashoka.models.Answers;
import com.lumstic.ashoka.models.Choices;
import com.lumstic.ashoka.models.IdentifierChoices;
import com.lumstic.ashoka.models.Identifiers;
import com.lumstic.ashoka.models.IncompleteResponse;
import com.lumstic.ashoka.models.Questions;
import com.lumstic.ashoka.models.Respondent;
import com.lumstic.ashoka.models.Responses;
import com.lumstic.ashoka.models.Surveys;
import com.lumstic.ashoka.utils.CommonUtil;
import com.lumstic.ashoka.utils.IntentConstants;
import com.lumstic.ashoka.views.RobotoLightEditText;
import com.lumstic.ashoka.views.RobotoRegularTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class IncompleteResponseActivity extends BaseActivity {

    private IncompleteResponsesAdapter incompleteResponsesAdapter;
    private DBAdapter dbAdapter;
    private ActionBar actionBar;

    private Surveys surveys;
    private Questions identifierQuestion;

    private ListView listView;
    private RobotoLightEditText inputFilter;
    private RobotoRegularTextView responseCount, surveyTitle;
    private int incompleteResponseCount = 0;
    private int identifierQuestionId = 0;

    private List<IncompleteResponse> incompleteResponseList;
    private List<Integer> incompleteResponsesId;
    private List<String> identifierQuestionAnswers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableLocation = true; //<- must be before super.onCreate
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

        //refreshList(false);

        incompleteResponsesAdapter = new IncompleteResponsesAdapter(getApplicationContext(), incompleteResponseList, surveys);
        incompleteResponsesAdapter.notifyDataSetChanged();
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(incompleteResponsesAdapter);
        incompleteResponsesAdapter.notifyDataSetChanged();
        registerForContextMenu(listView);



        inputFilter = (RobotoLightEditText) findViewById(R.id.inputFilter);
        inputFilter.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                incompleteResponsesAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {


            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String responseNumber = (String) view.findViewById(R.id.response_number_text)
                        .getTag();
                Intent intent = new Intent(getApplicationContext(), NewResponseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(IntentConstants.SURVEY, surveys);
                intent.putExtra(IntentConstants.RESPONSE_ID, Integer.parseInt(responseNumber));

                //get the incomplete response object
                IncompleteResponse ir = ((IncompleteResponse) incompleteResponsesAdapter.getItem(i));

                //check if it has a respondent
                if (ir.getRespondent() != null) {
                    List<Object> parms = new ArrayList<Object>();
                    parms.add(intent);
                    parms.add(ir);
                    requestLocation(parms);
                }else{
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onLocationReceived(Object parm) {
        List<Object> parms = (ArrayList<Object>) parm;
        Intent intent = (Intent) parms.get(0);
        IncompleteResponse ir = (IncompleteResponse) parms.get(1);

        createRespondentBlankResponse(intent,ir);
        startActivity(intent);
    }

    private void refreshList(boolean notify) {

        if(notify) {
            incompleteResponsesAdapter.Clear();
        }


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

        identifierQuestionAnswers.clear();
        for (int i = 0; i < incompleteResponseCount; i++) {
            identifierQuestionAnswers.add(dbAdapter.getAnswer(incompleteResponsesId.get(i), identifierQuestionId, 0));
            try {
                incompleteResponseList.add(i, new IncompleteResponse(String.valueOf(incompleteResponsesId.get(i)), identifierQuestion.getContent() + " : " + "  " + identifierQuestionAnswers.get(i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        //add the new respondents to the incomplete responses list if they have not already answered
        for (Respondent r: surveys.getRespondentList())
        {
            if( !dbAdapter.doesResponseExist(r.getId(), surveys.getId())) {
                IncompleteResponse ir = new IncompleteResponse(String.valueOf(r.getId()), r.getTag());
                ir.setRespondent(r);
                incompleteResponseList.add(incompleteResponseList.size(), ir);
            }
        }

        if(notify){
            incompleteResponsesAdapter.refresh();
            inputFilter.setText("");
        }
    }

    private void createRespondentBlankResponse(Intent intent, IncompleteResponse ir) {
        //create a response from the respondent object in the db
        Respondent respondent = ir.getRespondent();
        int responseId = dbAdapter.getResponseIdFromRespondent(respondent.getId(), surveys.getId());
        if(responseId == 0){
            //set set location and uuid
            respondent.setMobileId(UUID.randomUUID().toString());
            respondent.setLatitude(CommonUtil.getValidLatitude(appController));
            respondent.setLongitude(CommonUtil.getValidLongitude(appController));
            respondent.setUserId(Integer.parseInt(appController.getPreferences().getUserId()));
            respondent.setSurveyId(surveys.getId());
            //save blank response to db
            responseId = ((int)dbAdapter.insertDataResponsesTable(((Responses) respondent)));
            //insert tracking record
            dbAdapter.insertDataRespondentsTable(responseId, respondent.getId(), surveys.getId());
            for(Identifiers a: respondent.getIdentifiers())
            {
                a.setResponseId(((int) responseId));
                int answerid = (int) dbAdapter.insertDataAnswersTable(a);

                IdentifierChoices identChoices = a.getIdentifierChoices();

                for(Choices choice: identChoices.getChoices())
                {
                    choice.setAnswerId(answerid);
                    String content = dbAdapter.getOptionContentFromID(choice.getOptionId());
                    choice.setOption(content);
                    choice.setType(a.getType());
                    dbAdapter.insertDataChoicesTable(choice);
                }


            }


        }
        intent.putExtra(IntentConstants.RESPONSE_ID, ((int) responseId));
    }

    private void updateUI() {
        int numRespondents = surveys.getRespondentList().size();
        int completedRespondents = dbAdapter.getCompletedRespondents(surveys.getId());
        incompleteResponseCount = dbAdapter.getIncompleteResponse(surveys.getId());
        incompleteResponsesId = dbAdapter.getIncompleteResponsesIds(surveys.getId());
        responseCount.setText(Integer.toString(incompleteResponseCount + (numRespondents-completedRespondents)  ));
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
        else{
            refreshList(true);
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
            appController.getPreferences().setBackPressed(true);
           // NavUtils.navigateUpFromSameTask(this);
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
