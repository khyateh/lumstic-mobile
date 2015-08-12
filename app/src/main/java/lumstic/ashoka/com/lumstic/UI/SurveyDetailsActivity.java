package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lumstic.ashoka.com.lumstic.Adapters.DBAdapter;
import lumstic.ashoka.com.lumstic.Models.Answers;
import lumstic.ashoka.com.lumstic.Models.Questions;
import lumstic.ashoka.com.lumstic.Models.Responses;
import lumstic.ashoka.com.lumstic.Models.Surveys;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.IntentConstants;
import lumstic.ashoka.com.lumstic.Utils.JSONParser;
import lumstic.ashoka.com.lumstic.Utils.LumsticApp;

public class SurveyDetailsActivity extends Activity {

    private LinearLayout completeResponsesLinearLayout;
    private LinearLayout incompleteResponsesLinearLayout;
    private Button addResponsesButton;
    private RelativeLayout uploadButton;
    private TextView surveyTitleText, surveyDescriptionText, endDateText;
    private TextView completeTv, incompleteTv;

    private ActionBar actionBar;
    private LumsticApp lumsticApp;
    private Answers ans;
    private Surveys surveys;
    private Responses responses;
    private JSONParser jsonParser;
    private JSONArray jsonArray;
    private DBAdapter dbAdapter;
    private ProgressDialog progressDialog;
    private LocationManager locationManager;

    private String timestamp = "";
    private String baseUrl = "";
    private String uploadUrl = "/api/responses.json?";
    private String recordUrl = "/api/records";
    private String mobilId;
    private String syncString = "";
    private String jsonStr = null;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private double lat = 0, lon = 0;
    private int surveyUploadCount = 0;
    private int completeCount = 0, incompleteCount = 0;
    private int surveyId = 0;
    private int multiRecordCount = 0;
    private int multiRecordAsyncCount = 0;
    private boolean asynTaskCheck = false;

    private List<Questions> questionsList;
    private List<Answers> answerses;
    private List<Integer> completedResponseIds;

    private static HttpEntity createStringEntity(JSONObject params) {
        StringEntity se = null;
        try {
            se = new StringEntity(params.toString(), "UTF-8");
            se.setContentType("application/json; charset=UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("TAG", "Failed to create StringEntity", e);
            // exception = e;
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
        completedResponseIds = new ArrayList<Integer>();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (getIntent().hasExtra(IntentConstants.SURVEY)) {
            surveys = new Surveys();
            surveys = (Surveys) getIntent().getExtras().getSerializable(IntentConstants.SURVEY);
            actionBar.setTitle(surveys.getName());
            questionsList = new ArrayList<Questions>();
            questionsList = surveys.getQuestions();
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
                asynTaskCheck = false;
                multiRecordCount = 0;
                multiRecordAsyncCount = 0;
                if (checkLocationOn()) {
                    Location location = getLocation();
                    if (null != location) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                    }
                } else {
                    lat = 18.54194666666656;
                    lon = 73.8291466666657;
                }
                Long tsLong = System.currentTimeMillis() / 1000;
                timestamp = tsLong.toString();

                if (completeCount > 0) {
                    progressDialog = new ProgressDialog(SurveyDetailsActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Sync in Progress");
                    progressDialog.show();
                    completedResponseIds = dbAdapter.getCompleteResponsesIds(surveyId);


//////////////////////////
                    getMultiRecordCount();
                    generateRecordId();
/////////////////////
                    Log.e("TAG", "Do....While Before");
                    do {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.e("TAG", "Do....While" + multiRecordAsyncCount + ":" + multiRecordCount);
                    } while (multiRecordAsyncCount != multiRecordCount || asynTaskCheck);
                    Log.e("TAG", "Do....While After");

                    if (asynTaskCheck) {
                        lumsticApp.showToast("Something went wrong , please try again");
                        progressDialog.dismiss();
                    } else {
                        completedResponseIds = dbAdapter.getCompleteResponsesIds(surveyId);
                        for (int i = 0; i < completedResponseIds.size(); i++) {
                            answerses = new ArrayList<Answers>();
                            completedResponseIds.get(i);
                            answerses = null;
                            answerses = dbAdapter.getAnswerByResponseId(completedResponseIds.get(i));
                            jsonArray = new JSONArray();

                            JSONObject obj = new JSONObject();
                            try {
                                obj.put("status", "complete");
                                obj.put("survey_id", surveys.getId());
                                obj.put("updated_at", timestamp);
                                obj.put("longitude", lon);
                                obj.put("latitude", lat);
                                obj.put("user_id", lumsticApp.getPreferences().getUserId());
                                obj.put("organization_id", lumsticApp.getPreferences().getOrganizationId());
                                obj.put("access_token", lumsticApp.getPreferences().getAccessToken());
                                mobilId = UUID.randomUUID().toString();
                                obj.put("mobile_id", mobilId);
                                obj.put("answers_attributes", getAnswerJsonArray());
                                jsonStr = obj.toString();
                                Log.e("jsonString", jsonStr);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            new uploadResponse().execute(jsonArray.toString(), jsonStr.toString(), surveys.getId() + "", timestamp, lat + "", lon + "", mobilId);

                        }
                    }

                    ///////////////////////////////////


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

                if (getIntent().hasExtra(IntentConstants.SURVEY)) {
                    responses.setSurveyId(surveys.getId());
                    responses.setStatus("incomplete");
                    Intent intent = new Intent(SurveyDetailsActivity.this, NewResponseActivity.class);
                    intent.putExtra(IntentConstants.SURVEY, (java.io.Serializable) surveys);
                    startActivity(intent);
                }
                dbAdapter.insertDataResponsesTable(responses);
                finish();

            }
        });


    }

    public JSONArray getAnswerJsonArray() {
        for (int j = 0; j < answerses.size(); j++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("question_id", answerses.get(j).getQuestion_id());
                jsonObject.put("updated_at", answerses.get(j).getUpdated_at());
                jsonObject.put("content", answerses.get(j).getContent());
                jsonObject.put("record_id", answerses.get(j).getRecordId());


                try {
                    if ((answerses.get(j).getType().equals("MultiChoiceQuestion")) && (dbAdapter.getChoicesCount(answerses.get(j).getId()) == 0)) {
                        jsonObject.put("option_ids", JSONObject.NULL);
                        jsonObject.remove("content");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if ((answerses.get(j).getType().equals("DropDownQuestion")) || (answerses.get(j).getType().equals("MultiChoiceQuestion")) || (answerses.get(j).getType().equals("RadioQuestion"))) {
                        int recordId = answerses.get(j).getRecordId();
                        if ((answerses.get(j).getContent().equals("")) && (dbAdapter.getChoicesCountWhereAnswerIdIs(answerses.get(j).getId(), recordId) > 0)) {
                            String type = dbAdapter.getQuestionTypeWhereAnswerIdIs(answerses.get(j).getId());
                            if (type.equals("RadioQuestion")) {
                                jsonObject.put("content", dbAdapter.getChoicesWhereAnswerCountIsOne(answerses.get(j).getId()));
                            }
                            if (type.equals("DropDownQuestion")) {
                                jsonObject.put("content", dbAdapter.getChoicesWhereAnswerCountIsOne(answerses.get(j).getId()));
                            }
                            if (type.equals("MultiChoiceQuestion")) {
                                List<Integer> options = new ArrayList<>();
                                options = dbAdapter.getChoicesWhereAnswerCountIsMoreThanOne(answerses.get(j).getId());
                                if (options.size() > 0)
                                    jsonObject.putOpt("option_ids", options);
                                jsonObject.remove("content");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (answerses.get(j).getType().equals("PhotoQuestion")) {
                        String path = Environment.getExternalStorageDirectory().toString() + "/saved_images";
                        Bitmap b = null;
                        String fileName = answerses.get(j).getImage();
                        try {
                            File f = new File(path, fileName);
                            b = BitmapFactory.decodeStream(new FileInputStream(f));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        b.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        jsonObject.put("photo", encoded);
                        jsonObject.put("content", "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public void getMultiRecordCount() {
        for (int k = 0; k < completedResponseIds.size(); k++) {

            int responseId = completedResponseIds.get(k);

            for (int i = 0; i < surveys.getCategories().size(); i++) {
                if (surveys.getCategories().get(i).getType().equals("MultiRecordCategory")) {
                    for (int j = 0; j < surveys.getCategories().get(i).getQuestionsList().size(); j++) {
                        Cursor cursor = dbAdapter.getRecords(surveys.getCategories().get(i).getQuestionsList().get(j).getId(), responseId);

                        cursor.moveToFirst();
                        if (cursor.getCount() > 0) {
                            multiRecordCount++;
                            Log.e("multirecordcount", multiRecordCount + "");
                        }
                    }
                }
            }
        }
    }

    public void generateRecordId() {
        for (int k = 0; k < completedResponseIds.size(); k++) {

            int responseId = completedResponseIds.get(k);

            for (int i = 0; i < surveys.getCategories().size(); i++) {
                if (surveys.getCategories().get(i).getType().equals("MultiRecordCategory")) {
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
                                    new getWebIdFromServer().execute(categoryId);
                                } else {
                                    int recordId = cursor.getInt(cursor.getColumnIndex(DBAdapter.DBhelper.RECORD_ID));
                                    //dynamic url
                                    Integer[] categoryId = {surveys.getCategories().get(i).getId(), answerId, recordId};
                                    new getWebIdFromServerCaseTwo().execute(categoryId);
                                }
                            }
                            cursor.moveToNext();
                        }
                    }
                }
            }
        }
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
        getMenuInflater().inflate(R.menu.survey_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_logout) {
            final Dialog dialog = new Dialog(SurveyDetailsActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
            dialog.setContentView(R.layout.logout_dialog);
            dialog.show();
            Button button = (Button) dialog.findViewById(R.id.okay);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lumsticApp.getPreferences().setAccessToken("");
                    Intent i = new Intent(SurveyDetailsActivity.this, LoginActivity.class);
                    startActivity(i);
                    dialog.dismiss();
                }
            });
            return true;
        }
        if (id == R.id.action_fetch) {
            Intent i = new Intent(SurveyDetailsActivity.this, ActiveSurveyActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //get location
    public Location getLocation() {
        if (null != locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (null != locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
    }

    //check if location service is on
    public boolean checkLocationOn() {
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled) {
            return false;
        } else
            return true;
    }

    public class getWebIdFromServer extends AsyncTask<Integer, Void, Integer> {

        int answerId = 0;

        @Override
        protected Integer doInBackground(Integer... integer) {

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
            List nameValuePairs = new ArrayList();
            //attributes for survey sync
            try {
                httppost.addHeader("access_token", lumsticApp.getPreferences().getAccessToken());
                httppost.setEntity(createStringEntity(jsonObject));
                HttpResponse httpResponse = httpclient.execute(httppost);
                HttpEntity httpEntity = httpResponse.getEntity();
                recordsyncString = EntityUtils.toString(httpEntity);

                Log.e("whatsgoing", "" + EntityUtils.toString(httppost.getEntity()));
                Log.e("recordjsonsyncresponse", recordsyncString);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            int recordId = jsonParser.parseRecordIdResult(recordsyncString);
            if (recordId != 0) {
                Log.e("checkresult", dbAdapter.updateForMultiRecord(answerId, recordId) + "");
                multiRecordAsyncCount++;
                Log.e("Multi", "Multi" + multiRecordAsyncCount);
            } else {
                asynTaskCheck = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

//            int recordId = integer;
//            if(recordId!=0){
//            Log.e("checkresult", dbAdapter.updateForMultiRecord(answerId, recordId) + "");
//            multiRecordAsyncCount++;
//            Log.e("Multi","Multi"+ multiRecordAsyncCount);
//            }
//
//            else{
//               asynTaskCheck=true;
//            }
        }
    }


    public class getWebIdFromServerCaseTwo extends AsyncTask<Integer, Void, Integer> {

        int answerId = 0;
        int webId = 0;

        @Override
        protected Integer doInBackground(Integer... integer) {

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
            List nameValuePairs = new ArrayList();
            //attributes for survey sync
            try {
                httppost.addHeader("access_token", lumsticApp.getPreferences().getAccessToken());
                httppost.setEntity(createStringEntity(jsonObject));
                HttpResponse httpResponse = httpclient.execute(httppost);
                HttpEntity httpEntity = httpResponse.getEntity();
                recordsyncString = EntityUtils.toString(httpEntity);

                Log.e("whatsgoing", "" + EntityUtils.toString(httppost.getEntity()));
                Log.e("recordjsonsyncresponse", recordsyncString);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int recordId = jsonParser.parseRecordIdResult(recordsyncString);
            if (recordId != 0) {
                Log.e("checkresult", dbAdapter.updateForMultiRecord(answerId, recordId) + "");
                multiRecordAsyncCount++;
                Log.e("Multi", "Multi" + multiRecordAsyncCount);
            } else {
                asynTaskCheck = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
//            int recordId = integer;
//            if(recordId!=0){
//                Log.e("checkresult", dbAdapter.updateForMultiRecord(answerId, recordId) + "");
//                multiRecordAsyncCount++;
//                Log.e("Multi","Multi"+ multiRecordAsyncCount);
//            }
//
//            else{
//                asynTaskCheck=true;
//            }
        }
    }


    public class uploadResponse extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... string) {

            String answers_attributes = string[0];
            String response = string[1];
            String survey_id = string[2];
            String updated_at = string[3];
            String lat = string[4];
            String lon = string[5];
            String mob_id = string[6];
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(uploadUrl);
            List nameValuePairs = new ArrayList();
            //attributes for survey sync
            nameValuePairs.add(new BasicNameValuePair("answers_attributes", answers_attributes));
            nameValuePairs.add(new BasicNameValuePair("response", response));
            nameValuePairs.add(new BasicNameValuePair("status", "complete"));
            nameValuePairs.add(new BasicNameValuePair("survey_id", survey_id));
            nameValuePairs.add(new BasicNameValuePair("updated_at", updated_at));
            nameValuePairs.add(new BasicNameValuePair("longitude", lon + ""));
            nameValuePairs.add(new BasicNameValuePair("latitude", lat + ""));
            nameValuePairs.add(new BasicNameValuePair("access_token", lumsticApp.getPreferences().getAccessToken()));
            nameValuePairs.add(new BasicNameValuePair("user_id", lumsticApp.getPreferences().getUserId()));
            nameValuePairs.add(new BasicNameValuePair("organization_id", lumsticApp.getPreferences().getOrganizationId()));
            nameValuePairs.add(new BasicNameValuePair("mobile_id", mob_id));
            try {
                httppost.addHeader("access_token", lumsticApp.getPreferences().getAccessToken());
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse httpResponse = httpclient.execute(httppost);
                HttpEntity httpEntity = httpResponse.getEntity();
                syncString = EntityUtils.toString(httpEntity);
                Log.e("jsonsyncresponse", syncString);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.e("TAG", "check");
            return syncString;
        }

        @Override
        protected void onPostExecute(String s) {
//            progressDialog.dismiss();

            if (jsonParser.parseSyncResult(s)) {
                surveyUploadCount++;
            }


            if (surveyUploadCount == completeCount) {

                Toast.makeText(SurveyDetailsActivity.this, "Responses uploaded successfully:  " + surveyUploadCount + "    Errors:" + (completeCount - surveyUploadCount), Toast.LENGTH_LONG).show();
                dbAdapter.deleteFromResponseTableOnUpload(surveyId);
                completeCount = dbAdapter.getCompleteResponse(surveys.getId());
                completeTv.setText(completeCount + "");
                lumsticApp.getPreferences().setBack_pressed(true);

                progressDialog.dismiss();
                finish();
            } else {
                Toast.makeText(SurveyDetailsActivity.this, "Responses uploaded successfully:  " + surveyUploadCount + "    Errors:" + (completeCount - surveyUploadCount), Toast.LENGTH_LONG).show();
//                dbAdapter.deleteFromResponseTableOnUpload(surveyId);
//                completeCount = dbAdapter.getCompleteResponse(surveys.getId());
//                completeTv.setText(completeCount + "");
                lumsticApp.getPreferences().setBack_pressed(true);
                progressDialog.dismiss();
                finish();
            }

//            //check if all rsponses are uploaded
//            if (uploadCount == completeCount) {

//            }
//            //responses not uploaded
//            else {
//                Toast.makeText(SurveyDetailsActivity.this, "Responses upload unsuccessful", Toast.LENGTH_SHORT).show();
//            }
        }
    }


}
