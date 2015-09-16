package com.lumstic.ashoka.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.adapters.DBAdapter;
import com.lumstic.ashoka.adapters.DashBoardAdapter;
import com.lumstic.ashoka.models.Answers;
import com.lumstic.ashoka.models.Categories;
import com.lumstic.ashoka.models.Options;
import com.lumstic.ashoka.models.Questions;
import com.lumstic.ashoka.models.Surveys;
import com.lumstic.ashoka.utils.CommonUtil;
import com.lumstic.ashoka.utils.IntentConstants;
import com.lumstic.ashoka.utils.JSONParser;
import com.lumstic.ashoka.utils.JsonHelper;
import com.lumstic.ashoka.utils.NetworkUtil;
import com.lumstic.ashoka.views.RobotoBlackButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DashBoardActivity extends BaseActivity {

    private ActionBar actionBar;
    private boolean asynTaskCheck = false;
    private JsonHelper jsonHelper;
    private DBAdapter dbAdapter;
    private DashBoardAdapter dashBoardAdapter;
    private ProgressDialog progressDialog;
    private JSONParser jsonParser;
    private ListView listView;
    private LinearLayout uploadContainer;
    private RelativeLayout dashboardContainer, activeSurveyContainer;
    private RobotoBlackButton uploadButton;
    private List<Integer> completedResponseIds;
    private List<Surveys> surveysList;
    private int surveyUploadCount = 0, surveyUploadFailedCount = 0;
    private int completeCount = 0;
    private int totalCompletedResponses = 0;
    private String timestamp = "";
    private String loginUrl = "/api/login";
    private String recordUrl = "/api/records";
    private String fetchUrl = "/api/deep_surveys?access_token=";
    private String uploadUrl = "/api/responses.json?";

    private List<Answers> answers;

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

    public static int daysBetween(String publishDate, String expiryDate) {
        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = null, d2 = null;
        try {
            d1 = dfDate.parse(publishDate);
            d2 = dfDate.parse(expiryDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int daysToExpire = 0;
        try {
            daysToExpire = (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return daysToExpire;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        dashboardContainer = (RelativeLayout) findViewById(R.id.dashboardContainer);
        activeSurveyContainer = (RelativeLayout) findViewById(R.id.activeSurveyContainer);
        uploadContainer = (LinearLayout) findViewById(R.id.upload_container);
        listView = (ListView) findViewById(R.id.active_survey_list);
        uploadContainer = (LinearLayout) findViewById(R.id.upload_container);
        uploadButton = (RobotoBlackButton) findViewById(R.id.upload_all);


        //setting up action bar
        actionBar = getActionBar();
        actionBar.setTitle("Dashboard");
        dbAdapter = new DBAdapter(DashBoardActivity.this);


        uploadUrl = baseUrl + uploadUrl;
        recordUrl = baseUrl + recordUrl;
        fetchUrl = baseUrl + fetchUrl;


        jsonHelper = new JsonHelper();
        jsonParser = new JSONParser();
        surveysList = jsonHelper.tryParsing(lumsticApp.getPreferences().getSurveyData());


        progressDialog = new ProgressDialog(DashBoardActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage("Sync in Progress");

        initListView();
        checkForUploadUI();
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                surveyUploadCount = 0;
                surveyUploadFailedCount = 0;
                asynTaskCheck = false;

                Long tsLong = System.currentTimeMillis() / 1000;
                timestamp = tsLong.toString();
                if (completeCount > 0) {

                    if (NetworkUtil.iSConnected(getApplicationContext()) == NetworkUtil.TYPE_CONNECTED) {
                        progressDialog.setMessage("Sync in Progress");
                        progressDialog.show();
                        new uploadingMultiRecordResponse().execute();
                    } else {
                        lumsticApp.showToast("Please check your internet connection");
                    }


                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Surveys surveys = surveysList.get(i);
                Intent intent = new Intent(DashBoardActivity.this, SurveyDetailsActivity.class);
                intent.putExtra(IntentConstants.SURVEY, surveys);
                startActivity(intent);
            }
        });
    }

    private void initListView() {
        if (!lumsticApp.getPreferences().getSurveyData().equals("")) {

            activeSurveyContainer.setVisibility(View.VISIBLE);

            if (surveysList != null) {
                dashBoardAdapter = new DashBoardAdapter(getApplicationContext(), surveysList);
                listView.setAdapter(dashBoardAdapter);

            }

        }
    }

    private void checkForUploadUI() {
        completeCount = dbAdapter.getCompleteResponseFull();
        try {
            dashBoardAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (completeCount != 0) {
            uploadContainer.setVisibility(View.VISIBLE);
        } else {
            uploadContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkForUploadUI();

    }

    public void addQuestionFromCategories(Categories categories) {
        for (int m = 0; m < categories.getQuestionsList().size(); m++) {
            Questions question = categories.getQuestionsList().get(m);
            dbAdapter.insertDataQuestionTable(question);

            if (question.getOptions().size() > 0)
                addOptions(question);
        }
    }

    public void addOptions(Questions questions) {
        for (int k = 0; k < questions.getOptions().size(); k++) {
            Options options = questions.getOptions().get(k);
            dbAdapter.insertDataOptionsTable(options);
            if (options.getQuestions().size() > 0)
                addNestedQuestions(options);
            if (options.getCategories().size() > 0)
                addNestedCategories(options);
        }
    }

    public void addNestedCategories(Options options) {
        for (int d = 0; d < options.getCategories().size(); d++) {
            Categories categories = options.getCategories().get(d);
            dbAdapter.insertDataCategoriesTable(categories);
            if (categories.getQuestionsList().size() > 0)
                addQuestionFromCategories(categories);
        }
    }

    public void addNestedQuestions(Options options) {
        for (int l = 0; l < options.getQuestions().size(); l++) {
            Questions question = options.getQuestions().get(l);
            dbAdapter.insertDataQuestionTable(question);
            if (question.getOptions().size() > 0)
                addOptions(question);
        }
    }

    public void addCategories(Surveys surveys) {
        for (int h = 0; h < surveys.getCategories().size(); h++) {
            Categories categories = surveys.getCategories().get(h);
            dbAdapter.insertDataCategoriesTable(categories);
            if (categories.getQuestionsList().size() > 0)
                addQuestionFromCategories(categories);
        }
    }

    public void addQuestions(Surveys surveys) {

        for (int j = 0; j < surveys.getQuestions().size(); j++) {
            Questions question = surveys.getQuestions().get(j);
            dbAdapter.insertDataQuestionTable(question);
            if (question.getOptions().size() > 0)
                addOptions(question);
        }
    }


    private void doTaskResponseUploading() {
        if (asynTaskCheck) {
            lumsticApp.showToast("Something went wrong , please try again");
            progressDialog.dismiss();
        } else {

            for (int w = 0; w < surveysList.size(); w++) {
                completedResponseIds = dbAdapter.getCompleteResponsesIds(surveysList.get(w).getId());

                totalCompletedResponses = totalCompletedResponses + completedResponseIds.size();

                for (int i = 0; i < completedResponseIds.size(); i++) {
                    answers = new ArrayList<>();
                    completedResponseIds.get(i);
                    answers = null;
                    answers = dbAdapter.getAnswerByResponseId(completedResponseIds.get(i));
                    String lat = dbAdapter.getLatitudeFromResponseIDAndSurveyID(completedResponseIds.get(i), surveysList.get(w).getId());
                    String lon = dbAdapter.getLongitudeFromResponseIDAndSurveyID(completedResponseIds.get(i), surveysList.get(w).getId());
                    JSONObject obj = new JSONObject();

                    JSONObject localJsonObject = CommonUtil.getAnswerJsonObject(answers, dbAdapter);
                    String mobilId = dbAdapter.getMobileIDFromResponseIDAndSurveyID(completedResponseIds.get(i), surveysList.get(w).getId());
                    try {
                        obj.put("status", "complete");
                        obj.put("survey_id", surveysList.get(w).getId());
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


                    new uploadResponse().execute(localJsonObject.toString(), obj.toString(), surveysList.get(w).getId() + "", timestamp, lat + "", lon + "", mobilId, completedResponseIds.get(i) + "");
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

    public void generateRecordId() {
        for (int w = 0; w < surveysList.size(); w++) {
            completedResponseIds = dbAdapter.getCompleteResponsesIds(surveysList.get(w).getId());
            for (int k = 0; k < completedResponseIds.size(); k++) {
                int responseId = completedResponseIds.get(k);
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

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dash_board, menu);
        return true;
    }

    private boolean checkForExpiredSurveys() {
        boolean isSyncRequired = false;
        if (surveysList != null) {
            for (int i = 0; i < surveysList.size(); i++) {
                int daysToExpire = daysBetween(surveysList.get(i).getPublishedOn(), surveysList.get(i).getExpiryDate());
                int completedResponseCount = dbAdapter.getCompleteResponse(surveysList.get(i).getId());
                Log.e("TAG", "Completed Response : " + completedResponseCount + " Days To Expire : " + daysToExpire);
                if (completedResponseCount > 0 && daysToExpire <= 0) {
                    isSyncRequired = true;
                    break;
                }
            }
        }
        return isSyncRequired;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_fetch) {
            if (NetworkUtil.iSConnected(getApplicationContext()) == NetworkUtil.TYPE_CONNECTED) {
                if (checkForExpiredSurveys()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DashBoardActivity.this);
                    alertDialogBuilder
                            .setMessage("You have completed responses for an expired survey. Please sync responses before fetching..")
                            .setCancelable(false)
                            .setPositiveButton("yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            progressDialog.setMessage("Sync in Progress");
                                            progressDialog.show();
                                            surveyUploadCount = 0;
                                            surveyUploadFailedCount = 0;
                                            asynTaskCheck = false;
                                            new uploadingMultiRecordResponse().execute();
                                        }
                                    });
                    alertDialogBuilder.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                } else {
                    new FetchSurvey().execute();
                }

            } else {
                lumsticApp.showToast("Please check your internet connection");
            }
            return true;
        } else if (id == R.id.action_logout) {
            CommonUtil.Logout(DashBoardActivity.this, lumsticApp);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class FetchSurvey extends AsyncTask<Void, Void, String> {
        String jsonFetchString;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.setMessage("Fetching surveys..");
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {

            if (CommonUtil.isTokenExpired(lumsticApp)) {
                CommonUtil.reValidateToken(lumsticApp, baseUrl + loginUrl);
            }

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(fetchUrl + lumsticApp.getPreferences().getAccessToken());
                HttpResponse httpResponse = httpclient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                jsonFetchString = EntityUtils.toString(httpEntity);


            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonFetchString;
        }

        @Override
        protected void onPostExecute(String s) {

            try {
                if (s != null && s.length() > 0) {
                    lumsticApp.getPreferences().setSurveyData(s);
                    surveysList = jsonHelper.tryParsing(s);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (s != null && s.length() > 0) {
                if (surveysList != null) {
                    dashBoardAdapter = new DashBoardAdapter(getApplicationContext(), surveysList);
                    progressDialog.dismiss();
                    dashboardContainer.setVisibility(View.GONE);
                    activeSurveyContainer.setVisibility(View.VISIBLE);
                    Toast.makeText(DashBoardActivity.this, "Saving surveys to the device", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressDialog.dismiss();
                Toast.makeText(DashBoardActivity.this, "Unable to fetch survey,Please try again", Toast.LENGTH_SHORT).show();
            }
            completeCount = dbAdapter.getCompleteResponseFull();

            if (dbAdapter.getCompleteResponseFull() != 0) {
                uploadContainer.setVisibility(View.VISIBLE);
            } else {
                uploadContainer.setVisibility(View.GONE);
            }

            try {
                for (int i = 0; i < surveysList.size(); i++) {
                    Surveys surveys = surveysList.get(i);
                    dbAdapter.insertDataSurveysTable(surveys);
                    if (surveys.getCategories().size() > 0)
                        addCategories(surveys);
                    if (surveys.getQuestions().size() > 0)
                        addQuestions(surveys);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            listView.setAdapter(dashBoardAdapter);
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

    public class uploadResponse extends AsyncTask<String, Void, String> {
        String localResponseID;
        String localSurveyID;
        private String syncString = null;

        protected String doInBackground(String... string) {


            String answers_attributes = string[0];
            String response = string[1];
            localSurveyID = string[2];
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
                finalJsonObject.put("survey_id", localSurveyID);
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
                dbAdapter.deleteFromResponseTableOnUpload(Integer.parseInt(localSurveyID), localResponseID);
                dbAdapter.deleteFromAnswerTableWithResponseID(localResponseID);
            } else {
                surveyUploadFailedCount++;
            }

            completeCount = dbAdapter.getCompleteResponseFull();

            if ((surveyUploadCount + surveyUploadFailedCount) == totalCompletedResponses) {
                Toast.makeText(DashBoardActivity.this, "Responses uploaded successfully:  " + surveyUploadCount + "    Errors:" + (totalCompletedResponses - surveyUploadCount), Toast.LENGTH_LONG).show();
                if (completeCount != 0)
                    uploadContainer.setVisibility(View.VISIBLE);
                else
                    uploadContainer.setVisibility(View.GONE);

                progressDialog.dismiss();
            }
        }
    }


}
