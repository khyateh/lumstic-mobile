package com.lumstic.data.ui;

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

import com.lumstic.data.R;
import com.lumstic.data.adapters.DBAdapter;
import com.lumstic.data.adapters.DashBoardAdapter;
import com.lumstic.data.models.Answers;
import com.lumstic.data.models.Categories;
import com.lumstic.data.models.Options;
import com.lumstic.data.models.Questions;
import com.lumstic.data.models.Surveys;
import com.lumstic.data.utils.CommonUtil;
import com.lumstic.data.utils.IntentConstants;
import com.lumstic.data.utils.JSONParser;
import com.lumstic.data.utils.JsonHelper;
import com.lumstic.data.utils.NetworkUtil;
import com.lumstic.data.views.RobotoMediumButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private RobotoMediumButton uploadButton;
    private List<Integer> completedResponseIds, incompletedResponseIds;
    private List<Surveys> surveysList;
    private int surveyUploadCount = 0, surveyUploadFailedCount = 0;
    private int completeCount = 0;
    private int totalCompletedResponses = 0;
    private String timestamp = "";
    private String loginUrl = "/api/login";
    private String recordUrl = "/api/records";
    private String fetchUrl = "/api/deep_surveys?access_token=";
    private String uploadUrl = "/api/responses%s?access_token=%s";

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
        enableLocation = true; //<- must be before super.onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        dashboardContainer = (RelativeLayout) findViewById(R.id.dashboardContainer);
        activeSurveyContainer = (RelativeLayout) findViewById(R.id.activeSurveyContainer);
        uploadContainer = (LinearLayout) findViewById(R.id.upload_container);
        listView = (ListView) findViewById(R.id.active_survey_list);
        uploadContainer = (LinearLayout) findViewById(R.id.upload_container);


        //setting up action bar
        actionBar = getActionBar();
        actionBar.setTitle("Dashboard");
        dbAdapter = new DBAdapter(DashBoardActivity.this);


        uploadUrl = baseUrl + uploadUrl;
        recordUrl = baseUrl + recordUrl;
        fetchUrl = baseUrl + fetchUrl;


        jsonHelper = new JsonHelper();
        jsonParser = new JSONParser();
        surveysList = jsonHelper.tryParsing(appController.getPreferences().getSurveyData());


        progressDialog = new ProgressDialog(DashBoardActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage("Sync in Progress");

        initListView();
        checkForUploadUI();
        uploadContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                surveyUploadCount = 0;
                surveyUploadFailedCount = 0;
                totalCompletedResponses = 0;
                asynTaskCheck = false;

                Long tsLong = System.currentTimeMillis() / 1000;
                timestamp = tsLong.toString();
                if (completeCount > 0) {

                    if (NetworkUtil.iSConnected(getApplicationContext()) == NetworkUtil.TYPE_CONNECTED) {
                        progressDialog.setMessage("Sync in Progress");
                        progressDialog.show();
                        new UploadingMultiRecordResponse().execute();
                    } else {
                        appController.showToast("Please check your internet connection");
                    }


                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Surveys surveys = surveysList.get(i);
                if (!surveys.getName().equals(CommonUtil.SURVEY_MIDLINE_SEPARATOR_TEXT)) {
                    Intent intent = new Intent(DashBoardActivity.this, SurveyDetailsActivity.class);
                    intent.putExtra(IntentConstants.SURVEY, surveys);
                    startActivity(intent);
                }
            }
        });
    }

    private void initListView() {
        if (!appController.getPreferences().getSurveyData().equals("")) {

            activeSurveyContainer.setVisibility(View.VISIBLE);

            if (surveysList != null) {
                dashBoardAdapter = new DashBoardAdapter(getApplicationContext(), surveysList);
                listView.setAdapter(dashBoardAdapter);

            }

        }
    }

    private void checkForUploadUI() {
        completeCount = dbAdapter.getCompleteResponseFull() + dbAdapter.getIncompleteAnswersToUploadCount(appController.getPreferences().getLastSucessfulUpload());
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

            if (!question.getOptions().isEmpty())
                addOptions(question);
        }
    }

    public void addOptions(Questions questions) {
        for (int k = 0; k < questions.getOptions().size(); k++) {
            Options options = questions.getOptions().get(k);
            dbAdapter.insertDataOptionsTable(options);
            if (!options.getQuestionsList().isEmpty())
                addNestedQuestions(options);
            if (!options.getCategoriesList().isEmpty())
                addNestedCategories(options);
        }
    }

    public void addNestedCategories(Options options) {
        for (int d = 0; d < options.getCategoriesList().size(); d++) {
            Categories categories = options.getCategoriesList().get(d);
            dbAdapter.insertDataCategoriesTable(categories);
            if (!categories.getQuestionsList().isEmpty())
                addQuestionFromCategories(categories);
        }
    }

    public void addNestedQuestions(Options options) {
        for (int l = 0; l < options.getQuestionsList().size(); l++) {
            Questions question = options.getQuestionsList().get(l);
            dbAdapter.insertDataQuestionTable(question);
            if (!question.getOptions().isEmpty())
                addOptions(question);
        }
    }

    public void addCategories(Surveys surveys) {
        for (int h = 0; h < surveys.getCategoriesList().size(); h++) {
            Categories categories = surveys.getCategoriesList().get(h);
            dbAdapter.insertDataCategoriesTable(categories);
            if (!categories.getQuestionsList().isEmpty())
                addQuestionFromCategories(categories);
        }
    }

    public void addQuestions(Surveys surveys) {

        for (int j = 0; j < surveys.getQuestionsList().size(); j++) {
            Questions question = surveys.getQuestionsList().get(j);
            dbAdapter.insertDataQuestionTable(question);
            if (!question.getOptions().isEmpty())
                addOptions(question);
        }
    }


    private void doTaskResponseUploading() {
        if (asynTaskCheck) {
            appController.showToast("Something went wrong , please try again");
            progressDialog.dismiss();
        } else {

            for (int w = 0; w < surveysList.size(); w++) {
                int surveyId = surveysList.get(w).getId();
                completedResponseIds = dbAdapter.getCompleteResponsesIds(surveyId);
                incompletedResponseIds = dbAdapter.getIncompleteAndUnsyncdResponsesIds(surveyId,appController.getPreferences().getLastSucessfulUpload());
                totalCompletedResponses = totalCompletedResponses + completedResponseIds.size() + incompletedResponseIds.size();

                //upload completed responses
                for (int i = 0; i < completedResponseIds.size(); i++) {
                    uploadSingleResponse(surveyId, completedResponseIds.get(i), CommonUtil.SURVEY_STATUS_COMPLETE);
                }

                //upload incomplete responses
                for (int i = 0; i < incompletedResponseIds.size(); i++) {
                    uploadSingleResponse(surveyId, incompletedResponseIds.get(i), CommonUtil.SURVEY_STATUS_INCOMPLETE);
                }
            }
        }

    }

    private void uploadSingleResponse(int surveyId, int responseId, String status ) {
        answers = dbAdapter.getAnswerByResponseId(responseId);
        String lat = dbAdapter.getLatitudeFromResponseIDAndSurveyID(responseId, surveyId);
        String lon = dbAdapter.getLongitudeFromResponseIDAndSurveyID(responseId, surveyId);
        JSONObject obj = new JSONObject();
        JSONObject localJsonObject = CommonUtil.getAnswerJsonObject(answers, dbAdapter);
        String mobilId = dbAdapter.getMobileIDFromResponseIDAndSurveyID(responseId, surveyId);
        int serverId = dbAdapter.getServerIDFromResponseID(responseId);
        String action = serverId>0?CommonUtil.VERB_PUT:CommonUtil.VERB_POST;
        try {

            //TODO add response_id and Server_id fields here, on server update the create to be an upsert that returns the is mapping
            obj.put("response_id", responseId);
            obj.put("server_id", serverId);
            obj.put("status", status);
            obj.put("survey_id", surveyId);
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

        new UploadResponse(action).execute(localJsonObject.toString(), obj.toString(), surveyId + "", timestamp, lat + "", lon + "", mobilId, responseId + "", serverId + "");
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
                localRecordCursor.close();
            }

        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dash_board, menu);
        try {
            MenuItem mi = menu.getItem(0).getSubMenu().getItem(1);
            mi.setTitle(mi.getTitle() + " " + appController.getPreferences().getUsername());
        }
        catch(Exception e){
            e.printStackTrace();
        }
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
                                            totalCompletedResponses = 0;
                                            asynTaskCheck = false;
                                            new UploadingMultiRecordResponse().execute();
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
                appController.showToast("Please check your internet connection");
            }
            return true;
        } else if (id == R.id.action_logout) {
            CommonUtil.logout(DashBoardActivity.this, appController);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
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

            if (CommonUtil.isTokenExpired(appController)) {
                CommonUtil.reValidateToken(appController, baseUrl + loginUrl);
            }

            HttpURLConnection conn = null;
            String jsString = null;
            try {
                String urlString = String.format("%s%s", fetchUrl,  appController.getPreferences().getAccessToken() );
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                //TODO below line is for testing
                int resCode =  conn.getResponseCode();
               // CommonUtil.printmsg("response code from server:: "+ resCode);

                InputStream is = new BufferedInputStream(conn.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                StringBuilder responseBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = br.readLine()) != null) responseBuilder.append(inputStr);
                jsonFetchString = responseBuilder.toString();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            conn.disconnect();
            }
       // CommonUtil.printmsg("Survey data from server:: "+ jsonFetchString);
            return jsonFetchString;

        }

        boolean validateSurvey(Surveys surveyToValidate){
            return surveyToValidate.getQuestionsList() != null && !surveyToValidate.getQuestionsList().isEmpty() ;
        }

        @Override
        protected void onPostExecute(String s) {

            try {
               // CommonUtil.printmsg("raw Survey data from server:: "+ s);
                if (s != null && s.length() > 0) {
                    appController.getPreferences().setSurveyData(s);
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
                    if(validateSurvey(surveys)) {
                        dbAdapter.insertDataSurveysTable(surveys);
                        if (!surveys.getCategoriesList().isEmpty())
                            addCategories(surveys);
                        if (!surveys.getQuestionsList().isEmpty())
                            addQuestions(surveys);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            listView.setAdapter(dashBoardAdapter);
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

    public class UploadResponse extends AsyncTask<String, Void, String> {
        String localResponseID;
        String localSurveyID;
        String verb = CommonUtil.VERB_POST;
        private String syncString = null;

        public UploadResponse(String action){
            verb = action;
        }

        protected String doInBackground(String... string) {


            String answersAttributes = string[0];
            String response = string[1];
            localSurveyID = string[2];
            String updatedAt = string[3];
            String lat = string[4];
            String lon = string[5];
            String mobId = string[6];
            localResponseID = string[7];
            String server_id = string[8];

            JSONObject finalJsonObject = new JSONObject();
            try {
                finalJsonObject.put("answers_attributes", new JSONObject(answersAttributes));
                finalJsonObject.put("response", new JSONObject(response));
                finalJsonObject.put("status", "complete");
                finalJsonObject.put("survey_id", localSurveyID);
                finalJsonObject.put("updated_at", updatedAt);
                finalJsonObject.put("longitude", lon + "");
                finalJsonObject.put("latitude", lat + "");
                finalJsonObject.put("access_token", appController.getPreferences().getAccessToken());
                finalJsonObject.put("user_id", appController.getPreferences().getUserId());
                finalJsonObject.put("organization_id", appController.getPreferences().getOrganizationId());
                finalJsonObject.put("mobile_id", mobId);
                finalJsonObject.put("format", "json");
                finalJsonObject.put("action", "create");
                finalJsonObject.put("controller", "api/responses");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("TAG", "FINAL->>" + finalJsonObject.toString());


            HttpURLConnection conn = null;
            int res;
            String resMess =null;
            try {

                String urlString = String.format(uploadUrl, CommonUtil.VERB_POST.equals(verb)? "":"/"+server_id , appController.getPreferences().getAccessToken());
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod(verb);
                conn.setRequestProperty("CONTENT_TYPE", "application/json");
//                conn.setRequestProperty("access_token", appController.getPreferences().getAccessToken());
                conn.setDoOutput(true);
                conn.setDoInput(true);

                //testing if needed to handle non standard ports
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                conn.setRequestProperty("Accept", "*/*");

                conn.setChunkedStreamingMode(0);
                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                out.write(finalJsonObject.toString().getBytes());
                out.flush();
                out.close();

                try {
                    res = conn.getResponseCode();
                    resMess = conn.getResponseMessage();
                }
                catch (Exception e) {
                    res = conn.getResponseCode();
                    resMess = conn.getResponseMessage();
                }

                if( res == 200 ) {
                    BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder responseBuilder = new StringBuilder();
                    String inputStr;

                    while ((inputStr = br.readLine()) != null) responseBuilder.append(inputStr);
                    syncString = responseBuilder.toString();
                    br.close();
                }
                else {
                    BufferedInputStream is = new BufferedInputStream(conn.getErrorStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder responseBuilder = new StringBuilder();
                    String inputStr;

                    while ((inputStr = br.readLine()) != null) responseBuilder.append(inputStr);
                    String errString = responseBuilder.toString();
                    br.close();


                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }

            return syncString;
        }

        @Override
        protected void onPostExecute(String s) {

            JSONObject result;
            try {
                result = new JSONObject(s);
                if( CommonUtil.SURVEY_STATUS_COMPLETE.equals(result.get("status"))) {
                    //handle complete response results
                    if (jsonParser.parseSyncResult(s)) {
                        surveyUploadCount++;
                        int ret = dbAdapter.deleteFromResponseTableOnUpload(Integer.parseInt(localSurveyID), localResponseID);
                        if(ret>0){
                            dbAdapter.deleteFromAnswerTableWithResponseID(localResponseID);
                        }

                    } else {
                        surveyUploadFailedCount++;
                    }
                }else{
                    //handle incomplete response results

                    //set the Server ID in the response record
                    String server_id = result.getString("id");
                    dbAdapter.updateResponse_ServerId(localResponseID, server_id);
                    surveyUploadCount++;
                }
            } catch (JSONException e) {
                surveyUploadFailedCount++;
                e.printStackTrace();
            }

            completeCount = dbAdapter.getCompleteResponseFull();

            if ((surveyUploadCount + surveyUploadFailedCount) == totalCompletedResponses) {
                Toast.makeText(DashBoardActivity.this, "Responses uploaded successfully:  " + surveyUploadCount + "    Errors:" + (totalCompletedResponses - surveyUploadCount), Toast.LENGTH_LONG).show();
                if (completeCount != 0)
                    uploadContainer.setVisibility(View.VISIBLE);
                else
                    uploadContainer.setVisibility(View.GONE);


                //TODO clean up the respondents table

                //set lastupload date here
                appController.getPreferences().setLastSucessfulUpload(CommonUtil.getCurrentTimeStamp());

                progressDialog.dismiss();
            }
        }
    }
}
