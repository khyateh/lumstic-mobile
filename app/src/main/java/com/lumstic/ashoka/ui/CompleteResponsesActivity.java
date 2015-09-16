package com.lumstic.ashoka.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.adapters.CompleteResponsesAdapter;
import com.lumstic.ashoka.adapters.DBAdapter;
import com.lumstic.ashoka.models.Answers;
import com.lumstic.ashoka.models.CompleteResponse;
import com.lumstic.ashoka.models.Questions;
import com.lumstic.ashoka.models.Surveys;
import com.lumstic.ashoka.utils.CommonUtil;
import com.lumstic.ashoka.utils.IntentConstants;
import com.lumstic.ashoka.utils.JSONParser;
import com.lumstic.ashoka.utils.NetworkUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class CompleteResponsesActivity extends BaseActivity {

    private DBAdapter dbAdapter;
    private ActionBar actionBar;
    private String timestamp = "";
    private ListView listView;
    private TextView responseCount;
    private TextView surveyTitle;
    private int surveyUploadCount = 0, surveyUploadFailedCount = 0;
    private LinearLayout uploadContainer;
    private ProgressDialog progressDialog;
    private String uploadUrl = "/api/responses.json?";
    private String recordUrl = "/api/records";
    private Surveys surveys;
    private Questions identifierQuestion;
    private CompleteResponsesAdapter completeResponsesAdapter;
    private List<Answers> answers;
    private int completeResponseCount = 0;
    private int identifierQuestionId = 0;
    private String loginUrl = "/api/login";
    private List<CompleteResponse> completeResponseList;
    private List<Integer> completedResponseIds;
    private List<String> identifierQuestionAnswers;
    private boolean asynTaskCheck = false;
    private JSONParser jsonParser;

    private static HttpEntity createStringEntity(JSONObject params) {
        StringEntity se = null;
        try {
            se = new StringEntity(params.toString(), "UTF-8");
            se.setContentType("application/json; charset=UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("TAG", "Failed to create StringEntity", e);
        }
        return se;
    }

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
        completeResponseList = new ArrayList<>();
        completedResponseIds = new ArrayList<>();
        identifierQuestionAnswers = new ArrayList<>();
        uploadUrl = baseUrl + uploadUrl;
        recordUrl = baseUrl + recordUrl;
        jsonParser = new JSONParser();
        //setting up views
        responseCount = (TextView) findViewById(R.id.complete_response_count);
        surveyTitle = (TextView) findViewById(R.id.survey_title_text);
        uploadContainer = (LinearLayout) findViewById(R.id.upload_container);
        surveys = new Surveys();
        //get survey object from previous activity
        surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);

        updateUI();

        surveyTitle.setText(surveys.getName());

        for (int j = 0; j < surveys.getQuestions().size(); j++) {
            if (surveys.getQuestions().get(j).getIdentifier() == 1) {
                identifierQuestion = surveys.getQuestions().get(j);
                identifierQuestionId = surveys.getQuestions().get(j).getId();
            } else {
                identifierQuestion = surveys.getQuestions().get(0);
                identifierQuestionId = surveys.getQuestions().get(0).getId();
            }
        }

        for (int i = 0; i < completeResponseCount; i++) {
            identifierQuestionAnswers.add(dbAdapter.getAnswer(completedResponseIds.get(i), identifierQuestionId, 0));
            completeResponseList.add(i, new CompleteResponse(String.valueOf(completedResponseIds.get(i)), identifierQuestion.getContent() + " :" + "  " + identifierQuestionAnswers.get(i)));
        }

        listView = (ListView) findViewById(R.id.listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String responseNumber = (String) view.findViewById(R.id.response_number_text).getTag();

                Intent intent = new Intent(getApplicationContext(), NewResponseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(IntentConstants.SURVEY, (java.io.Serializable) surveys);
                intent.putExtra(IntentConstants.RESPONSE_ID, Integer.parseInt(responseNumber));
                startActivity(intent);
            }
        });
        completeResponsesAdapter = new CompleteResponsesAdapter(getApplicationContext(), completeResponseList, surveys);
        listView.setAdapter(completeResponsesAdapter);
        registerForContextMenu(listView);
        uploadContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                progressDialog = new ProgressDialog(CompleteResponsesActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Sync in Progress");


                surveyUploadCount = 0;
                surveyUploadFailedCount = 0;
                asynTaskCheck = false;
                Long tsLong = System.currentTimeMillis() / 1000;
                timestamp = tsLong.toString();

                if (NetworkUtil.iSConnected(getApplicationContext()) == NetworkUtil.TYPE_CONNECTED) {
                    progressDialog.show();
                    new uploadingMultiRecordResponse().execute();
                } else {
                    lumsticApp.showToast("Please check your internet connection");
                }


            }
        });


    }

    private void updateUI() {
        completeResponseCount = dbAdapter.getCompleteResponse(surveys.getId());
        completedResponseIds = dbAdapter.getCompleteResponsesIds(surveys.getId());
        //if count not available set gone
        if (completeResponseCount == 0) {
            uploadContainer.setVisibility(View.GONE);
        }
        responseCount.setText(Integer.toString(completeResponseCount));
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
            completeResponseList.remove(info.position);
            completeResponsesAdapter.notifyDataSetChanged();
            updateUI();
            return true;
        }
        return false;
    }

    private void doTaskResponseUploading() {
        if (asynTaskCheck) {
            lumsticApp.showToast("Something went wrong , please try again");
            progressDialog.dismiss();
        } else {
            completedResponseIds = dbAdapter.getCompleteResponsesIds(surveys.getId());
            for (int i = 0; i < completedResponseIds.size(); i++) {
                answers = new ArrayList<>();
                completedResponseIds.get(i);
                answers = null;
                answers = dbAdapter.getAnswerByResponseId(completedResponseIds.get(i));
                String lat = dbAdapter.getLatitudeFromResponseIDAndSurveyID(completedResponseIds.get(i), surveys.getId());
                String lon = dbAdapter.getLongitudeFromResponseIDAndSurveyID(completedResponseIds.get(i), surveys.getId());
                JSONObject obj = new JSONObject();
                JSONObject localJsonObject = CommonUtil.getAnswerJsonObject(answers, dbAdapter);
                String mobilId = dbAdapter.getMobileIDFromResponseIDAndSurveyID(completedResponseIds.get(i), surveys.getId());
                try {
                    obj.put("status", "complete");
                    obj.put("survey_id", surveys.getId());
                    obj.put("updated_at", timestamp);
                    obj.put("longitude", lon);
                    obj.put("latitude", lat);
                    obj.put("user_id", lumsticApp.getPreferences().getUserId());
                    obj.put("organization_id", lumsticApp.getPreferences().getOrganizationId());
                    obj.put("access_token", lumsticApp.getPreferences().getAccessToken());
                    obj.put("mobile_id", mobilId);
                    obj.put("answers_attributes", localJsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new uploadResponse().execute(localJsonObject.toString(), obj.toString(), surveys.getId() + "", timestamp, lat + "", lon + "", mobilId, completedResponseIds.get(i) + "");
            }
        }

    }

    public void generateRecordId() {

        for (int i = 0; i < completedResponseIds.size(); i++) {
            int responseId = completedResponseIds.get(i);
            Cursor localRecordCursor = dbAdapter.getRecordIdsByResponseId(responseId);
            localRecordCursor.moveToFirst();

            if (localRecordCursor.getCount() > 0) {
                for (int l = 0; l < localRecordCursor.getCount(); l++) {

                    int webId = localRecordCursor.getInt(localRecordCursor.getColumnIndex(DBAdapter.DBhelper.WEB_ID));
                    int recordId = localRecordCursor.getInt(localRecordCursor.getColumnIndex(DBAdapter.DBhelper.ID));
                    int categoryId = localRecordCursor.getInt(localRecordCursor.getColumnIndex
                            (DBAdapter.DBhelper.CATEGORY_ID));
                    if (webId == 0) {
                        //default url
                        Integer[] params = {categoryId, responseId, recordId};
                        getWebIdFromServer(params);
                    } else {
                        //dynamic url
                        Integer[] params = {categoryId,
                                recordId, responseId};
                        getWebIdFromServerCaseTwo(params);
                    }
                    localRecordCursor.moveToNext();
                }
            }


        }


    }


    public void getWebIdFromServer(Integer[] integer) {

        int responseId = integer[1];
        int oldRecordId = integer[2];
        String recordsyncString = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("category_id", integer[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(recordUrl);
        //attributes for survey sync
        try {
            httppost.addHeader("access_token", lumsticApp.getPreferences().getAccessToken());
            httppost.setEntity(createStringEntity(jsonObject));
            HttpResponse httpResponse = httpclient.execute(httppost);
            HttpEntity httpEntity = httpResponse.getEntity();
            recordsyncString = EntityUtils.toString(httpEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int newRecordId = jsonParser.parseRecordIdResult(recordsyncString);

        if (newRecordId != 0) {
            dbAdapter.updateRecordsTable(newRecordId, responseId);
            dbAdapter.updateAnswerTable(responseId, newRecordId, oldRecordId);
        } else {
            asynTaskCheck = true;
        }

    }

    public void getWebIdFromServerCaseTwo(Integer[] integer) {
        int webId;
        int responseId = integer[2];


        webId = integer[1];
        String recordsyncString = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("category_id", integer[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpClient httpclient = new DefaultHttpClient();

        HttpPut httppost = new HttpPut(baseUrl + "/api/records/" + webId + ".json");
        //attributes for survey sync
        try {
            httppost.addHeader("access_token", lumsticApp.getPreferences().getAccessToken());
            httppost.setEntity(createStringEntity(jsonObject));
            HttpResponse httpResponse = httpclient.execute(httppost);
            HttpEntity httpEntity = httpResponse.getEntity();
            recordsyncString = EntityUtils.toString(httpEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int recordId = jsonParser.parseRecordIdResult(recordsyncString);
        if (recordId != 0) {
            dbAdapter.updateRecordsTable(recordId, responseId);
            dbAdapter.updateAnswerTable(responseId, recordId, webId);
        } else {
            asynTaskCheck = true;
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_complete_responses, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_logout) {
            CommonUtil.Logout(CompleteResponsesActivity.this, lumsticApp);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public class uploadingMultiRecordResponse extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            if (CommonUtil.isTokenExpired(lumsticApp)) {
                CommonUtil.reValidateToken(lumsticApp, baseUrl + loginUrl);
            }
            generateRecordId();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            doTaskResponseUploading();
        }
    }

    public class uploadResponse extends AsyncTask<String, Void, String> {

        String localResponseID;
        private String syncString = null;

        protected String doInBackground(String... string) {


            String answers_attributes = string[0];
            String response = string[1];
            String survey_id = string[2];
            String updated_at = string[3];
            String lat = string[4];
            String lon = string[5];
            String mob_id = string[6];
            localResponseID = string[7];
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(uploadUrl);

            JSONObject finalJsonObject = new JSONObject();
            try {
                finalJsonObject.put("answers_attributes", new JSONObject(answers_attributes));
                finalJsonObject.put("response", new JSONObject(response));
                finalJsonObject.put("status", "complete");
                finalJsonObject.put("survey_id", survey_id);
                finalJsonObject.put("updated_at", updated_at);
                finalJsonObject.put("longitude", lon + "");
                finalJsonObject.put("latitude", lat + "");
                finalJsonObject.put("access_token", lumsticApp.getPreferences().getAccessToken());
                finalJsonObject.put("user_id", lumsticApp.getPreferences().getUserId());
                finalJsonObject.put("organization_id", lumsticApp.getPreferences().getOrganizationId());
                finalJsonObject.put("mobile_id", mob_id);
                finalJsonObject.put("format", "json");
                finalJsonObject.put("action", "create");
                finalJsonObject.put("controller", "api/v1/responses");
            } catch (Exception e) {
                e.printStackTrace();
            }


            Log.e("TAG", "FINAL->>" + finalJsonObject.toString());

            try {
                httppost.addHeader("access_token", lumsticApp.getPreferences().getAccessToken());

                StringEntity se = new StringEntity(finalJsonObject.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setEntity(se);

                HttpResponse httpResponse = httpclient.execute(httppost);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    syncString = EntityUtils.toString(httpEntity);
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return syncString;
        }

        @Override
        protected void onPostExecute(String s) {
            if (jsonParser.parseSyncResult(s)) {
                surveyUploadCount++;
                dbAdapter.deleteFromResponseTableOnUpload(surveys.getId(), localResponseID);
                dbAdapter.deleteFromAnswerTableWithResponseID(localResponseID);
            } else {
                surveyUploadFailedCount++;
            }


            if ((surveyUploadCount + surveyUploadFailedCount) == completeResponseCount) {
                Toast.makeText(CompleteResponsesActivity.this, "Responses uploaded successfully:  " + surveyUploadCount + "    Errors:" + (completeResponseCount - surveyUploadCount), Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                finish();
            }
        }
    }
}
