package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import lumstic.ashoka.com.lumstic.Adapters.CompleteResponsesAdapter;
import lumstic.ashoka.com.lumstic.Adapters.DBAdapter;
import lumstic.ashoka.com.lumstic.Models.Answers;
import lumstic.ashoka.com.lumstic.Models.CompleteResponses;
import lumstic.ashoka.com.lumstic.Models.Questions;
import lumstic.ashoka.com.lumstic.Models.Surveys;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.CommonUtil;
import lumstic.ashoka.com.lumstic.Utils.IntentConstants;
import lumstic.ashoka.com.lumstic.Utils.JSONParser;
import lumstic.ashoka.com.lumstic.Utils.NetworkUtil;

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
    private List<CompleteResponses> completeResponseses;
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
        completeResponseses = new ArrayList<>();
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
        completeResponseCount = dbAdapter.getCompleteResponse(surveys.getId());
        completedResponseIds = dbAdapter.getCompleteResponsesIds(surveys.getId());
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
                identifierQuestionAnswers.add(dbAdapter.getAnswer(completedResponseIds.get(i), identifierQuestionId, 0));
                completeResponseses.add(i, new CompleteResponses(String.valueOf(completedResponseIds.get(i)), identifierQuestion.getContent() + " :" + "  " + identifierQuestionAnswers.get(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        listView = (ListView) findViewById(R.id.listview);
        completeResponsesAdapter = new CompleteResponsesAdapter(getApplicationContext(), completeResponseses, surveys);
        listView.setAdapter(completeResponsesAdapter);
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
}
