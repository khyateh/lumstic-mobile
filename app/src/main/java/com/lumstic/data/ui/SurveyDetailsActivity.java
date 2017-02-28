package com.lumstic.data.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.lumstic.data.R;
import com.lumstic.data.adapters.DBAdapter;
import com.lumstic.data.models.Answers;
import com.lumstic.data.models.Responses;
import com.lumstic.data.models.Surveys;
import com.lumstic.data.utils.CommonUtil;
import com.lumstic.data.utils.IntentConstants;
import com.lumstic.data.utils.JSONParser;
import com.lumstic.data.utils.NetworkUtil;
import com.lumstic.data.views.RobotoLightTextView;
import com.lumstic.data.views.RobotoRegularButton;
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
import java.util.UUID;


public class SurveyDetailsActivity extends BaseActivity {

    private LinearLayout completeResponsesLinearLayout;
    private LinearLayout incompleteResponsesLinearLayout;
    private RobotoRegularButton addResponsesButton;
    private RelativeLayout uploadButton;
    private RobotoRegularTextView surveyTitleText, surveyDescriptionText;
    private RobotoLightTextView endDateText;
    private RobotoRegularTextView completeTv, incompleteTv;

    private ActionBar actionBar;
    private Surveys surveys;
    private Responses responses;
    private JSONParser jsonParser;
    private DBAdapter dbAdapter;
    private ProgressDialog progressDialog;
    private String timestamp = "";
    private String baseUrl = "";
    private String loginUrl = "/api/login";
    private String uploadUrl = "/api/responses.json?";
    private String recordUrl = "/api/records";

    private int surveyUploadCount = 0, surveyUploadFailedCount = 0;
    private int completeCount = 0, incompleteCount = 0;
    private int surveyId = 0;
    private boolean asynTaskCheck = false;
    private List<Answers> answers;
    private List<Integer> completedResponseIds;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableLocation = true; //<- must be before super.onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_details);
        actionBar = getActionBar();
        if (appController.getPreferences().getBaseUrl() == null) {
            baseUrl = SurveyDetailsActivity.this.getResources().getString(R.string.server_url);
        } else
            baseUrl = appController.getPreferences().getBaseUrl();
        uploadUrl = baseUrl + uploadUrl;
        recordUrl = baseUrl + recordUrl;
        jsonParser = new JSONParser();
        completedResponseIds = new ArrayList<>();

        if (getIntent().hasExtra(IntentConstants.SURVEY)) {
            surveys = new Surveys();
            surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);
            actionBar.setTitle(surveys.getName());
        } else
            actionBar.setTitle("Survey Detail");
        surveyId = surveys.getId();
        //setting up actionbar
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.drawable.ic_action_ic_back);
        actionBar.setDisplayShowTitleEnabled(true);
        //setting up views
        uploadButton = (RelativeLayout) findViewById(R.id.upload_button);
        surveyTitleText = (RobotoRegularTextView) findViewById(R.id.survey_title_text);
        surveyDescriptionText = (RobotoRegularTextView) findViewById(R.id.survey_description_text);
        endDateText = (RobotoLightTextView) findViewById(R.id.end_date_text);
        completeTv = (RobotoRegularTextView) findViewById(R.id.complete_response);
        incompleteTv = (RobotoRegularTextView) findViewById(R.id.incomplete_response);
        incompleteResponsesLinearLayout = (LinearLayout) findViewById(R.id.incomplete_response_container);
        completeResponsesLinearLayout = (LinearLayout) findViewById(R.id.complete_response_container);
        responses = new Responses();
        dbAdapter = new DBAdapter(SurveyDetailsActivity.this);

        //if no response which is complete hide upload button
        if (dbAdapter.getCompleteResponse(surveys.getId()) == 0) {
            uploadButton.setVisibility(View.GONE);
        }

        //##
        uploadButton.setEnabled(false);
        //##

        if (getIntent().hasExtra(IntentConstants.SURVEY)) {

            surveyTitleText.setText(surveys.getName());
            surveyDescriptionText.setText(surveys.getDescription());
            endDateText.setText(surveys.getExpiryDate());
        }


        addResponsesButton = (RobotoRegularButton) findViewById(R.id.add_responses_button);
        incompleteResponsesLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                appController.getPreferences().setBackPressed(false);
                Intent intent = new Intent(SurveyDetailsActivity.this, IncompleteResponseActivity.class);
                intent.putExtra(IntentConstants.SURVEY, surveys);
                startActivity(intent);
                finish();
            }
        });

        //on upload button click
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                surveyUploadCount = 0;
                surveyUploadFailedCount = 0;
                asynTaskCheck = false;
                Long tsLong = System.currentTimeMillis() / 1000;
                timestamp = tsLong.toString();

                if (completeCount > 0) {
                    progressDialog = new ProgressDialog(SurveyDetailsActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Sync in Progress");

                    completedResponseIds = dbAdapter.getCompleteResponsesIds(surveyId);

                    if (NetworkUtil.iSConnected(getApplicationContext()) == NetworkUtil.TYPE_CONNECTED) {
                        progressDialog.show();
                        new UploadingMultiRecordResponse().execute();
                    } else {
                        appController.showToast("Please check your internet connection");
                    }
                }

            }
        });

        completeResponsesLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SurveyDetailsActivity.this, CompleteResponsesActivity.class);
                i.putExtra(IntentConstants.SURVEY, surveys);
                startActivity(i);
            }
        });

        SetCounts();
        addResponsesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLocation(null);
            }
        });


    }

    @Override
    protected void onLocationReceived(Object parm) {
        if (getIntent().hasExtra(IntentConstants.SURVEY)) {
            responses.setSurveyId(surveys.getId());
            responses.setStatus("incomplete");
            responses.setMobileId(UUID.randomUUID().toString());
            responses.setLatitude(CommonUtil.getValidLatitude(appController));
            responses.setLongitude(CommonUtil.getValidLongitude(appController));
            Intent intent = new Intent(SurveyDetailsActivity.this, NewResponseActivity.class);
            intent.putExtra(IntentConstants.SURVEY, surveys);
            startActivity(intent);
        }
        dbAdapter.insertDataResponsesTable(responses);
        finish();
    }

    private void SetCounts() {
        int numRespondents = surveys.getRespondentList().size();
        int completedRespondents = dbAdapter.getCompletedRespondents(surveys.getId());
        incompleteCount = dbAdapter.getIncompleteResponse(surveys.getId());
        completeCount = dbAdapter.getCompleteResponse(surveys.getId());
        incompleteTv.setText(Integer.toString(incompleteCount + numRespondents - completedRespondents) );
        completeTv.setText( Integer.toString(completeCount));
    }

    public Boolean isGPSEnable() {
        return ((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetCounts();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_complete_responses, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_logout) {
            CommonUtil.logout(SurveyDetailsActivity.this, appController);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void doTaskResponseUploading() {
        if (asynTaskCheck) {
            appController.showToast("Something went wrong , please try again");
            progressDialog.dismiss();
        } else {
            completedResponseIds = dbAdapter.getCompleteResponsesIds(surveyId);
            for (int i = 0; i < completedResponseIds.size(); i++) {
                answers = new ArrayList<>();
                completedResponseIds.get(i);
                answers = null;
                answers = dbAdapter.getAnswerByResponseId(completedResponseIds.get(i));
                String lat = dbAdapter.getLatitudeFromResponseIDAndSurveyID(completedResponseIds.get(i), surveyId);
                String lon = dbAdapter.getLongitudeFromResponseIDAndSurveyID(completedResponseIds.get(i), surveyId);
                JSONObject obj = new JSONObject();
                JSONObject localJsonObject = CommonUtil.getAnswerJsonObject(answers, dbAdapter);
                String mobilId = dbAdapter.getMobileIDFromResponseIDAndSurveyID(completedResponseIds.get(i), surveyId);
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

    public class UploadResponse extends AsyncTask<String, Void, String> {

        String localResponseID;
        private String syncString = null;

        protected String doInBackground(String... string) {


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
                finalJsonObject.put("answers_attributes", new JSONObject(answersAttributes));
                finalJsonObject.put("response", new JSONObject(response));
                finalJsonObject.put("status", "complete");
                finalJsonObject.put("survey_id", surveyId);
                finalJsonObject.put("updated_at", updatedAt);
                finalJsonObject.put("longitude", lon + "");
                finalJsonObject.put("latitude", lat + "");
                finalJsonObject.put("access_token", appController.getPreferences().getAccessToken());
                finalJsonObject.put("user_id", appController.getPreferences().getUserId());
                finalJsonObject.put("organization_id", appController.getPreferences().getOrganizationId());
                finalJsonObject.put("mobile_id", mobId);
                finalJsonObject.put("format", "json");
                finalJsonObject.put("action", "create");
                finalJsonObject.put("controller", "api/v1/responses");
            }catch(JSONException je){je.printStackTrace();}

        //TODO jyothi Dec 29
            try {
                Log.e("TAG", "FINAL->>" + finalJsonObject.toString(100));
               // CommonUtil.printmsg("Response JSON being uploaded to server::SURVEY_DETAIL_ACTIVITY" + finalJsonObject.toString(100));
            }catch(JSONException je){je.printStackTrace();}


            try {
                httppost.addHeader("access_token", appController.getPreferences().getAccessToken());

                StringEntity se = new StringEntity(finalJsonObject.toString());

               // throw new RuntimeExecutionException(new IOException());
                 se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                  httppost.setEntity(se);

                HttpResponse httpResponse = httpclient.execute(httppost);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    syncString = EntityUtils.toString(httpEntity);
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {//TODO jyothi uncomment this after testing ****
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return syncString;
        }

        @Override
        protected void onPostExecute(String s) {
            //TODO Jyothi feb 27 2016 to fix loss of data when upload failed.
            if (null != s && jsonParser.parseSyncResult(s)) {
                surveyUploadCount++;
                dbAdapter.deleteFromResponseTableOnUpload(surveyId, localResponseID);
                dbAdapter.deleteFromAnswerTableWithResponseID(localResponseID);
            } else {
                surveyUploadFailedCount++;
            }


            if ((surveyUploadCount + surveyUploadFailedCount) == completeCount) {
                Toast.makeText(SurveyDetailsActivity.this, "Responses uploaded successfully:  " + surveyUploadCount + "    Errors:" + (completeCount - surveyUploadCount), Toast.LENGTH_LONG).show();
                completeCount = dbAdapter.getCompleteResponse(surveys.getId());
                completeTv.setText(Integer.toString(completeCount));
                appController.getPreferences().setBackPressed(true);
                progressDialog.dismiss();
                finish();
            }
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
    }
}

