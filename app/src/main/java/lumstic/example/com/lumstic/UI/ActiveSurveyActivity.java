package lumstic.example.com.lumstic.UI;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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

import lumstic.example.com.lumstic.Adapters.DBAdapter;
import lumstic.example.com.lumstic.Adapters.DashBoardAdapter;
import lumstic.example.com.lumstic.Models.Answers;
import lumstic.example.com.lumstic.Models.Categories;
import lumstic.example.com.lumstic.Models.Options;
import lumstic.example.com.lumstic.Models.Questions;
import lumstic.example.com.lumstic.Models.Surveys;
import lumstic.example.com.lumstic.R;
import lumstic.example.com.lumstic.Utils.IntentConstants;
import lumstic.example.com.lumstic.Utils.JSONParser;
import lumstic.example.com.lumstic.Utils.JsonHelper;
import lumstic.example.com.lumstic.Utils.LumsticApp;


public class ActiveSurveyActivity extends Activity {
    ListView listView;
    LumsticApp lumsticApp;
    LinearLayout uploadContainer;
    List<Surveys> surveysList;
    List<Integer> completedResponseIds;
    List<Answers> answerses;
    String uploadUrl = "https://survey-web-stgng.herokuapp.com/api/responses.json?";
    Surveys surveys;
    int surveyId;
    JSONArray jsonArray;
    String jsonStr = null;
    double lat = 0, lon = 0;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    int completeCount = 0;
    JSONParser jsonParser;
    Button uploadButton;
    int uploadCount=0;
    List<String> jsonSyncResponses;
    JsonHelper jsonHelper;
    boolean surveyFromServer = false;
    ProgressDialog progressDialog;
    String mobilId;
    String syncString = "";
    String timestamp = "";
    DashBoardAdapter dashBoardAdapter;
    DBAdapter dbAdapter;
    // String fetchUrl = "http://192.168.2.16:3000/api/deep_surveys?access_token=";
    String fetchUrl = "https://survey-web-stgng.herokuapp.com/api/deep_surveys?access_token=";
    String jsonFetchString = "";
    private LocationManager locationManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_active_survey);
        getActionBar().setTitle("DashBoard");


        lumsticApp = (LumsticApp) getApplication();
        dbAdapter = new DBAdapter(ActiveSurveyActivity.this);
        uploadContainer = (LinearLayout) findViewById(R.id.upload_container);
        uploadButton=(Button)findViewById(R.id.upload_all);
        completeCount = dbAdapter.getCompleteResponseFull();
        if (dbAdapter.getCompleteResponseFull() == 0) {
            uploadContainer.setVisibility(View.GONE);
        }

        jsonParser = new JSONParser();
        progressDialog = new ProgressDialog(ActiveSurveyActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Fetching Surveys ");
        progressDialog.show();
        new FetchSurvey().execute();




    }

    @Override
    protected void onResume() {
        super.onResume();


        uploadButton=(Button)findViewById(R.id.upload_all);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mobilId = UUID.randomUUID().toString();
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
                    progressDialog = new ProgressDialog(ActiveSurveyActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Sync in Progress");
                    progressDialog.show();
                    new uploadResponse().execute();
                }


            }
        });


        try {
            dashBoardAdapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();}
            completeCount = dbAdapter.getCompleteResponseFull();

        if (dbAdapter.getCompleteResponseFull() != 0) {
            uploadContainer.setVisibility(View.VISIBLE);
        }

        if (dbAdapter.getCompleteResponseFull() == 0) {
            uploadContainer.setVisibility(View.GONE);
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.active_survey, menu);
        return true;
    }

    public void addCategories(Surveys surveys) {

        for (int h = 0; h < surveys.getCategories().size(); h++) {
            Categories categories = surveys.getCategories().get(h);
            long value1 = dbAdapter.insertDataCategoriesTable(categories);
            Log.e("value", value1 + "");
            if (categories.getQuestionsList().size() > 0)
                addQuestionFromCategories(categories);
        }
    }

    public void addQuestions(Surveys surveys) {

        for (int j = 0; j < surveys.getQuestions().size(); j++) {
            Questions question = surveys.getQuestions().get(j);
            long value1 = dbAdapter.insertDataQuestionTable(question);
            Log.e("value", value1 + "");
            if (question.getOptions().size() > 0)
                addOptions(question);
        }
    }

    public void addQuestionFromCategories(Categories categories) {
        for (int m = 0; m < categories.getQuestionsList().size(); m++) {
            Questions question = categories.getQuestionsList().get(m);
            long value1 = dbAdapter.insertDataQuestionTable(question);
            Log.e("value", value1 + "");

            if (question.getOptions().size() > 0)
                addOptions(question);
        }
    }

    public void addOptions(Questions questions) {

        for (int k = 0; k < questions.getOptions().size(); k++) {
            Options options = questions.getOptions().get(k);
            long value1 = dbAdapter.insertDataOptionsTable(options);
            Log.e("value", value1 + "");
            if (options.getQuestions().size() > 0)
                addNestedQuestions(options);


            if (options.getCategories().size() > 0)
                addNestedCategories(options);


        }
    }

    public void addNestedCategories(Options options) {


        for (int d = 0; d < options.getCategories().size(); d++) {
            Categories categories = options.getCategories().get(d);
            long value1 = dbAdapter.insertDataCategoriesTable(categories);
            Log.e("value", value1 + "");
            if (categories.getQuestionsList().size() > 0)
                addQuestionFromCategories(categories);
        }

    }

    public void addNestedQuestions(Options options) {


        for (int l = 0; l < options.getQuestions().size(); l++) {
            Questions question = options.getQuestions().get(l);
            long value1 = dbAdapter.insertDataQuestionTable(question);
            Log.e("value", value1 + "");
            if (question.getOptions().size() > 0)
                addOptions(question);
        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_help) {
            return true;
        }
        if (id == R.id.action_logout) {

            final Dialog dialog = new Dialog(ActiveSurveyActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
            dialog.setContentView(R.layout.logout_dialog);
            dialog.show();
            Button button = (Button) dialog.findViewById(R.id.okay);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lumsticApp.getPreferences().setAccessToken("");
                    Intent i = new Intent(ActiveSurveyActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            });

            return true;
        }

        if (id == R.id.action_fetch) {
//            progressDialog = new ProgressDialog(ActiveSurveyActivity.this);
//            progressDialog.setCancelable(false);
//            progressDialog.setIndeterminate(true);
//            progressDialog.setMessage("Fetching Surveys ");
//            progressDialog.show();
//
//            new FetchSurvey().execute();
//            //dashBoardAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Location getLocation() {
        if (null != locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (null != locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
    }

    public boolean checkLocationOn() {
//
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

    class FetchSurvey extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {

            fetchUrl = fetchUrl + lumsticApp.getPreferences().getAccessToken();

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(fetchUrl);
                HttpResponse httpResponse = httpclient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                jsonFetchString = EntityUtils.toString(httpEntity);
                Log.e("fetchstring", jsonFetchString);


            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }

            return jsonFetchString;
        }

        @Override
        protected void onPostExecute(String s) {
            jsonHelper = new JsonHelper(ActiveSurveyActivity.this);
            surveysList = new ArrayList<Surveys>();
            try {

                lumsticApp.getPreferences().setSurveyData(s);

                surveysList = jsonHelper.tryParsing(lumsticApp.getPreferences().getSurveyData());


                //surveysList = jsonHelper.tryParsing(jsonHelper.getStringFromJson());


            } catch (Exception e) {
                e.printStackTrace();
            }
            listView = (ListView) findViewById(R.id.active_survey_list);
            if (surveysList != null) {
                dashBoardAdapter = new DashBoardAdapter(getApplicationContext(), surveysList);
                progressDialog.dismiss();
                Toast.makeText(ActiveSurveyActivity.this, "Saving surveys to the device", Toast.LENGTH_SHORT).show();
            }
            if (surveysList == null) {
                progressDialog.dismiss();
                Toast.makeText(ActiveSurveyActivity.this, "Please check your wifi or network settings", Toast.LENGTH_SHORT).show();
            }

            try {
                for (int i = 0; i < surveysList.size(); i++) {

                    Surveys surveys = surveysList.get(i);
                    long value = dbAdapter.insertDataSurveysTable(surveys);
                    //Toast.makeText(ActiveSurveyActivity.this, value + "", Toast.LENGTH_SHORT).show();
                    if (surveys.getCategories().size() > 0)
                        addCategories(surveys);
                    if (surveys.getQuestions().size() > 0)
                        addQuestions(surveys);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            listView.setAdapter(dashBoardAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Surveys surveys = surveysList.get(i);
                    Intent intent = new Intent(ActiveSurveyActivity.this, SurveyDetailsActivity.class);
                    intent.putExtra(IntentConstants.SURVEY, surveys);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    public class uploadResponse extends AsyncTask<Void, Void, String> {


        protected String doInBackground(Void... voids) {


            for (int count = 0; count < surveysList.size(); count++) {
                jsonSyncResponses = new ArrayList<>();
                completedResponseIds = new ArrayList<Integer>();
                surveys = surveysList.get(count);
                surveyId = surveys.getId();


                completedResponseIds = dbAdapter.getCompleteResponsesIds(surveyId);
                for (int i = 0; i < completedResponseIds.size(); i++) {
                    answerses = new ArrayList<Answers>();

                    completedResponseIds.get(i);

                    answerses = null;

                    answerses = dbAdapter.getAnswerByResponseId(completedResponseIds.get(i));
                    jsonArray = new JSONArray();
                    for (int j = 0; j < answerses.size(); j++) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("question_id", answerses.get(j).getQuestion_id());
                            jsonObject.put("updated_at", answerses.get(j).getUpdated_at());
                            jsonObject.put("content", answerses.get(j).getContent());

                            try {
                                if ((answerses.get(j).getType().equals("MultiChoiceQuestion")) && (dbAdapter.getChoicesCount(answerses.get(j).getId()) == 0)) {
                                    Log.e("thereisastring", dbAdapter.getChoicesCount(answerses.get(j).getId()) + "cdcdcd");
                                    Log.e("testing", answerses.get(j).getType() + "this is a type");
                                    jsonObject.put("option_ids", JSONObject.NULL);
                                    jsonObject.remove("content");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            try {
                                if ((answerses.get(j).getType().equals("DropDownQuestion")) || (answerses.get(j).getType().equals("MultiChoiceQuestion")) || (answerses.get(j).getType().equals("RadioQuestion"))) {
                                    if ((answerses.get(j).getContent().equals("")) && (dbAdapter.getChoicesCountWhereAnswerIdIs(answerses.get(j).getId()) > 0)) {
                                        Log.e("goingintheloop", "intheloop");
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
                                    Log.e("answers", "dex");
                                    String path = Environment.getExternalStorageDirectory().toString() + "/saved_images";
                                    Bitmap b = null;
                                    String fileName = answerses.get(j).getImage();
                                    //Toast.makeText(SurveyDetailsActivity.this,fileName,Toast.LENGTH_SHORT).show();
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
                        obj.put("mobile_id", mobilId);

                        obj.put("answers_attributes", jsonArray);
                        //responseObj.put("status", "complete");
                        //responseObj.put("survey_id", surveys.getId());
                        //responseObj.put("updated_at", timestamp);
                        //responseObj.put("longitude", lon);
                        //responseObj.put("latitude", lat);
                        //responseObj.put("user_id", lumsticApp.getPreferences().getUserId());
                        //responseObj.put("organization_id", lumsticApp.getPreferences().getOrganizationId());
                        //responseObj.put("mobile_id", mobilId);


                        jsonStr = obj.toString();

                        Log.e("jsonString", jsonStr);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    UUID.randomUUID().toString();
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(uploadUrl);
                    List nameValuePairs = new ArrayList();
                    nameValuePairs.add(new BasicNameValuePair("answers_attributes", jsonArray.toString()));
                    nameValuePairs.add(new BasicNameValuePair("response", jsonStr));
                    nameValuePairs.add(new BasicNameValuePair("status", "complete"));
                    nameValuePairs.add(new BasicNameValuePair("survey_id", surveys.getId() + ""));
                    nameValuePairs.add(new BasicNameValuePair("updated_at", timestamp));
                    nameValuePairs.add(new BasicNameValuePair("longitude", lon + ""));
                    nameValuePairs.add(new BasicNameValuePair("latitude", lat + ""));
                    nameValuePairs.add(new BasicNameValuePair("access_token", lumsticApp.getPreferences().getAccessToken()));
                    nameValuePairs.add(new BasicNameValuePair("user_id", lumsticApp.getPreferences().getUserId()));
                    nameValuePairs.add(new BasicNameValuePair("organization_id", lumsticApp.getPreferences().getOrganizationId()));
                    nameValuePairs.add(new BasicNameValuePair("mobile_id", mobilId));
                    try {
                        httppost.addHeader("access_token", lumsticApp.getPreferences().getAccessToken());
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse httpResponse = httpclient.execute(httppost);
                        HttpEntity httpEntity = httpResponse.getEntity();
                        syncString = EntityUtils.toString(httpEntity);


                        Log.e("jsonsyncresponse", syncString);
                        jsonSyncResponses.add(syncString);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

                           for(int i=0;i<jsonSyncResponses.size();i++){
                if (jsonParser.parseSyncResult(jsonSyncResponses.get(i))) {
                    uploadCount++;
                    dbAdapter.deleteFromResponseTableOnUpload(surveyId);

                } }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if(uploadCount==completeCount){
                dashBoardAdapter.notifyDataSetChanged();
                Toast.makeText(ActiveSurveyActivity.this, "Responses uploaded successfully:  "+uploadCount+"    Errors:0", Toast.LENGTH_LONG).show();
                //completeCount = dbAdapter.getCompleteResponse(surveys.getId());
                //completeTv.setText(completeCount + "");

                //finish();

                completeCount = dbAdapter.getCompleteResponseFull();

            if (dbAdapter.getCompleteResponseFull() != 0) {
                uploadContainer.setVisibility(View.VISIBLE);
            }

            if (dbAdapter.getCompleteResponseFull() == 0) {
                uploadContainer.setVisibility(View.GONE);
            }




            }
        }
    }
}
