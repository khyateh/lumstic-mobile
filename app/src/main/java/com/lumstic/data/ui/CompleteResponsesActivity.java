package com.lumstic.data.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.lumstic.data.R;
import com.lumstic.data.adapters.CompleteResponsesAdapter;
import com.lumstic.data.adapters.DBAdapter;
import com.lumstic.data.models.Answers;
import com.lumstic.data.models.CompleteResponse;
import com.lumstic.data.models.Questions;
import com.lumstic.data.models.Surveys;
import com.lumstic.data.utils.CommonUtil;
import com.lumstic.data.utils.IntentConstants;
import com.lumstic.data.utils.JSONParser;
import com.lumstic.data.utils.NetworkUtil;
import com.lumstic.data.views.RobotoRegularTextView;

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
    private RobotoRegularTextView responseCount, surveyTitle;
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
        responseCount = (RobotoRegularTextView) findViewById(R.id.complete_response_count);
        surveyTitle = (RobotoRegularTextView) findViewById(R.id.survey_title_text);
        uploadContainer = (LinearLayout) findViewById(R.id.upload_container);
        surveys = new Surveys();
        //get survey object from previous activity
        surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);

        updateUI();

        surveyTitle.setText(surveys.getName());
try {
    for (int j = 0; j < surveys.getQuestionsList().size(); j++) {
        if (surveys.getQuestionsList().get(j).getIdentifier() == 1) {
            identifierQuestion = surveys.getQuestionsList().get(j);
            identifierQuestionId = surveys.getQuestionsList().get(j).getId();
        } else {
            identifierQuestion = surveys.getQuestionsList().get(0);
            identifierQuestionId = surveys.getQuestionsList().get(0).getId();
        }
    }
}
catch(Exception e){
    String m = e.getMessage();
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
                intent.putExtra(IntentConstants.SURVEY, surveys);
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
                    new UploadingMultiRecordResponse().execute();
                } else {
                    appController.showToast("Please check your internet connection");
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
        if (surveys.getRespondentList()==null || surveys.getRespondentList().size()==0) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        if (item.getItemId() == R.id.action_delete) {

            showConfirmDialog(CommonUtil.DELETE_RESPONSE_DIALOG_TITLE, CommonUtil.DELETE_RESPONSE_DIALOG_MSG,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbAdapter.deleteFromAnswerTableWithResponseID((String) info.targetView.findViewById(R.id.response_number_text).getTag());
                            dbAdapter.deleteFromResponseTable(surveys.getId(), (String) info.targetView.findViewById(R.id.response_number_text).getTag());
                            completeResponseList.remove(info.position);
                            completeResponsesAdapter.notifyDataSetChanged();
                            updateUI();
                        }
                    });

            return true;
        }
        return false;
    }

    private void doTaskResponseUploading() {
        if (asynTaskCheck) {
            appController.showToast("Something went wrong , please try again");
            progressDialog.dismiss();
        } else {
            completedResponseIds = dbAdapter.getCompleteResponsesIds(surveys.getId());
           // CommonUtil.printmsg("Total completed surveys:: " +completedResponseIds.size() +"\n");
            for (int i = 0; i < completedResponseIds.size(); i++) {

              //  CommonUtil.printmsg("UPLOADING SURVEY RESPONSE NUMBER:: " + i+"\n");
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
                    obj.put("user_id", appController.getPreferences().getUserId());
                    obj.put("organization_id", appController.getPreferences().getOrganizationId());
                    obj.put("access_token", appController.getPreferences().getAccessToken());
                    obj.put("mobile_id", mobilId);
                    obj.put("answers_attributes", localJsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new UploadResponse().execute(localJsonObject.toString(), obj.toString(), surveys.getId() + "", timestamp, lat + "", lon + "", mobilId, completedResponseIds.get(i) + "");
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

            localRecordCursor.close();
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
            httppost.addHeader("access_token", appController.getPreferences().getAccessToken());
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
            httppost.addHeader("access_token", appController.getPreferences().getAccessToken());
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
            CommonUtil.logout(CompleteResponsesActivity.this, appController);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public class UploadingMultiRecordResponse extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            if (CommonUtil.isTokenExpired(appController)) {
                CommonUtil.reValidateToken(appController, baseUrl + loginUrl);
            }
            generateRecordId();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            doTaskResponseUploading();
        }
    }

    public class UploadResponse extends AsyncTask<String, Void, String> {

        String localResponseID;
        private String syncString = null;

        protected String doInBackground(String... string) {
        //TODO redundancy in JSON sent to server. Commenting few lines to avoid redundancy

            String answersAttributes = string[0];
            String response = string[1];
            String surveyId = string[2];
            String updatedAt = string[3];
            String lat = string[4];
            String lon = string[5];
            String mobId = string[6];
            localResponseID = string[7];
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(uploadUrl);

            JSONObject finalJsonObject = new JSONObject();


            try {

                finalJsonObject.put("response", new JSONObject(response));
                finalJsonObject.put("access_token", appController.getPreferences().getAccessToken());
                finalJsonObject.put("format", "json");
                finalJsonObject.put("action", "create");
                finalJsonObject.put("controller", "api/v1/responses");
            }
            //TODO Upload of list of survey responses halts when any of the surveys throws exception.JYOTHI DEC 18/12/2016
            /*catch (Exception e) {
                e.printStackTrace();
            }*/catch(JSONException j){
                j.printStackTrace();
                //j.getMessage()
            }

            try {
                Log.e("TAG", "FINAL->>" + finalJsonObject.toString(100));
              //  CommonUtil.printmsg("Response JSON being uploaded to server::COMPLETERESPONSE" + finalJsonObject.toString(100));
            }catch(JSONException je){
              //  CommonUtil.printmsg("Response JSON being uploaded to server::COMPLETERESPONSE failed");
                je.printStackTrace();}

            try {
                httppost.addHeader("access_token", appController.getPreferences().getAccessToken());
                //TODO changing code to utf8 encode JSON
               /* StringEntity se = new StringEntity(finalJsonObject.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setEntity(se);*/
                StringEntity se = new StringEntity(finalJsonObject.toString(), "UTF-8");
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
                //CommonUtil.printmsg("string entity JSON being uploaded to server::" + se.toString());
                httppost.setEntity(se);

                HttpResponse httpResponse = httpclient.execute(httppost);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    syncString = EntityUtils.toString(httpEntity);
                }
              //  throw new IOException();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        //TODO JYOTHI Upload of list of survey responses halts when any of the surveys fails to upload.DEC 18/12/2016
            finally {

                return syncString;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            //TODO Jyothi Jan 11 2017 adding null != s
            if (null != s  && jsonParser.parseSyncResult(s)) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
    }
}
