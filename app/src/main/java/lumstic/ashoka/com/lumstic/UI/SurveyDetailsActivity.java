package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import lumstic.ashoka.com.lumstic.Adapters.DBAdapter;
import lumstic.ashoka.com.lumstic.Models.Answers;
import lumstic.ashoka.com.lumstic.Models.Responses;
import lumstic.ashoka.com.lumstic.Models.Surveys;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.CommonUtil;
import lumstic.ashoka.com.lumstic.Utils.IntentConstants;
import lumstic.ashoka.com.lumstic.Utils.JSONParser;
import lumstic.ashoka.com.lumstic.Utils.LumsticApp;
import lumstic.ashoka.com.lumstic.Utils.NetworkUtil;

public class SurveyDetailsActivity extends BaseActivity {

    private LinearLayout completeResponsesLinearLayout;
    private LinearLayout incompleteResponsesLinearLayout;
    private Button addResponsesButton;
    private RelativeLayout uploadButton;
    private TextView surveyTitleText, surveyDescriptionText, endDateText;
    private TextView completeTv, incompleteTv;

    private ActionBar actionBar;
    private LumsticApp lumsticApp;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_details);
        actionBar = getActionBar();
        lumsticApp = (LumsticApp) getApplication();
        if (lumsticApp.getPreferences().getBaseUrl() == null) {
            baseUrl = SurveyDetailsActivity.this.getResources().getString(R.string.server_url);
        } else
            baseUrl = lumsticApp.getPreferences().getBaseUrl();
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
        surveyTitleText = (TextView) findViewById(R.id.survey_title_text);
        surveyDescriptionText = (TextView) findViewById(R.id.survey_description_text);
        endDateText = (TextView) findViewById(R.id.end_date_text);
        completeTv = (TextView) findViewById(R.id.complete_response);
        incompleteTv = (TextView) findViewById(R.id.incomplete_response);
        incompleteResponsesLinearLayout = (LinearLayout) findViewById(R.id.incomplete_response_container);
        completeResponsesLinearLayout = (LinearLayout) findViewById(R.id.complete_response_container);
        responses = new Responses();
        dbAdapter = new DBAdapter(SurveyDetailsActivity.this);

        //if no response which is complete hide upload button
        if (dbAdapter.getCompleteResponse(surveys.getId()) == 0) {
            uploadButton.setVisibility(View.GONE);
        }
        if (getIntent().hasExtra(IntentConstants.SURVEY)) {

            surveyTitleText.setText(surveys.getName());
            surveyDescriptionText.setText(surveys.getDescription());
            endDateText.setText(surveys.getExpiryDate());
        }


        addResponsesButton = (Button) findViewById(R.id.add_responses_button);
        incompleteResponsesLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                lumsticApp.getPreferences().setBack_pressed(false);
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
                        new uploadingMultiRecordResponse().execute();
                    } else {
                        lumsticApp.showToast("Please check your internet connection");
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

        incompleteCount = dbAdapter.getIncompleteResponse(surveys.getId());
        completeCount = dbAdapter.getCompleteResponse(surveys.getId());
        incompleteTv.setText(incompleteCount + "");
        completeTv.setText(completeCount + "");
        addResponsesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (true) {
                    if (getIntent().hasExtra(IntentConstants.SURVEY)) {
                        responses.setSurveyId(surveys.getId());
                        responses.setStatus("incomplete");
                        responses.setMobileId(UUID.randomUUID().toString());
                        responses.setLatitude(CommonUtil.getValidLatitude(lumsticApp));
                        responses.setLongitude(CommonUtil.getValidLongitude(lumsticApp));
                        Intent intent = new Intent(SurveyDetailsActivity.this, NewResponseActivity.class);
                        intent.putExtra(IntentConstants.SURVEY, (java.io.Serializable) surveys);
                        startActivity(intent);
                    }
                    dbAdapter.insertDataResponsesTable(responses);
                    finish();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SurveyDetailsActivity.this);
                    alertDialogBuilder
                            .setMessage("GPS is disabled in your device. Enable it?")
                            .setCancelable(false)
                            .setPositiveButton("Enable GPS",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            Intent callGPSSettingIntent = new Intent(
                                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivity(callGPSSettingIntent);
                                        }
                                    });
                    alertDialogBuilder.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();

                }

            }
        });


    }

    public Boolean isGPSEnable() {
        return ((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        incompleteCount = dbAdapter.getIncompleteResponse(surveys.getId());
        completeCount = dbAdapter.getCompleteResponse(surveys.getId());
        incompleteTv.setText(incompleteCount + "");
        completeTv.setText(completeCount + "");
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
            CommonUtil.Logout(SurveyDetailsActivity.this, lumsticApp);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void getWebIdFromServer(Integer[] integer) {

        int answerId = 0;
        answerId = integer[1];
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

        int recordId = jsonParser.parseRecordIdResult(recordsyncString);

        if (recordId != 0) {
            dbAdapter.updateForMultiRecord(answerId, recordId);
        } else {
            asynTaskCheck = true;
        }

    }

    public void getWebIdFromServerCaseTwo(Integer[] integer) {
        int answerId;
        int webId;

        answerId = integer[1];
        webId = integer[2];
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
            dbAdapter.updateForMultiRecord(answerId, recordId);
        } else {
            asynTaskCheck = true;
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

            }


            Log.e("TAG", "FINAL->>" + finalJsonObject.toString());

            try {
                httppost.addHeader("access_token", lumsticApp.getPreferences().getAccessToken());

                StringEntity se = new StringEntity(finalJsonObject.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setEntity(se);

                HttpResponse httpResponse = httpclient.execute(httppost);
                Log.e("TAG", "HTTP RESPONSE STATUS CODE->>" + httpResponse.getStatusLine().getStatusCode());
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
                dbAdapter.deleteFromResponseTableOnUpload(surveyId, localResponseID);
                dbAdapter.deleteFromAnswerTableWithResponseID(localResponseID);
            } else {
                surveyUploadFailedCount++;
            }


            if ((surveyUploadCount + surveyUploadFailedCount) == completeCount) {
                Toast.makeText(SurveyDetailsActivity.this, "Responses uploaded successfully:  " + surveyUploadCount + "    Errors:" + (completeCount - surveyUploadCount), Toast.LENGTH_LONG).show();
                completeCount = dbAdapter.getCompleteResponse(surveys.getId());
                completeTv.setText(completeCount + "");
                lumsticApp.getPreferences().setBack_pressed(true);
                progressDialog.dismiss();
                finish();
            }
        }
    }

    private void doTaskResponseUploading() {
        if (asynTaskCheck) {
            lumsticApp.showToast("Something went wrong , please try again");
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

    public void generateRecordId() {
        for (int k = 0; k < completedResponseIds.size(); k++) {
            int responseId = completedResponseIds.get(k);
            for (int i = 0; i < surveys.getCategories().size(); i++) {
                if (surveys.getCategories().get(i).getType().equals(CommonUtil.CATEGORY_TYPE_MULTI_RECORD)) {


                    for (int j = 0; j < surveys.getCategories().get(i).getQuestionsList().size(); j++) {
                        Cursor cursor = dbAdapter.getRecords(surveys.getCategories().get(i).getQuestionsList().get(j).getId(), responseId);

                        cursor.moveToFirst();
                        if (cursor.getCount() > 0) {
                            for (int l = 0; l < cursor.getCount(); l++) {

                                int webId = cursor.getInt(cursor.getColumnIndex(DBAdapter.DBhelper.WEB_ID));
                                int answerId = cursor.getInt(cursor.getColumnIndex(DBAdapter.DBhelper.ID));
                                if (webId == 0) {
                                    //default url
                                    Integer[] categoryId = {surveys.getCategories().get(i).getId(), answerId};
                                    getWebIdFromServer(categoryId);
                                } else {
                                    int recordId = cursor.getInt(cursor.getColumnIndex(DBAdapter.DBhelper.RECORD_ID));
                                    //dynamic url
                                    Integer[] categoryId = {surveys.getCategories().get(i).getId(), answerId, recordId};
                                    getWebIdFromServerCaseTwo(categoryId);
                                }
                                cursor.moveToNext();
                            }
                        }
                    }
                }
            }

        }

    }


}

