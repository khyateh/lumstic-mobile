package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
    private String mobilId;
    private String syncString = "";
    private String jsonStr = null;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private double lat = 0, lon = 0;
    private int completeCount = 0, incompleteCount = 0;
    private int surveyId = 0;

    private List<Questions> questionsList;
    private List<Answers> answerses;
    private List<Integer> completedResponseIds;
    private List<String> jsonSyncResponses;

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
                    new uploadResponse().execute();
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

        if (id == R.id.action_fetch) {
            Intent i = new Intent(SurveyDetailsActivity.this, ActiveSurveyActivity.class);
            startActivity(i);
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

    public class uploadResponse extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... voids) {
            jsonSyncResponses = new ArrayList<>();
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
                                jsonObject.put("option_ids", JSONObject.NULL);
                                jsonObject.remove("content");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            if ((answerses.get(j).getType().equals("DropDownQuestion")) || (answerses.get(j).getType().equals("MultiChoiceQuestion")) || (answerses.get(j).getType().equals("RadioQuestion"))) {
                                if ((answerses.get(j).getContent().equals("")) && (dbAdapter.getChoicesCountWhereAnswerIdIs(answerses.get(j).getId()) > 0)) {
                                    String type = dbAdapter.getQuestionTypeWhereAnswerIdIs(answerses.get(j).getId());
                                    if (type.equals("RadioQuestion")) {
                                        jsonObject.put("content", dbAdapter.getChoicesWhereAnswerCountIsOne(answerses.get(j).getId()));
                                    }
                                    if (type.equals("DropDownQuestion")) {
                                        jsonObject.put("content", dbAdapter.getChoicesWhereAnswerCountIsOne(answerses.get(j).getId()));
                                    }
                                    if (type.equals("MultiChoiceQuestion")) {
                                        List<Integer> options =  new ArrayList<>();
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
                    obj.put("answers_attributes", jsonArray);
                    jsonStr = obj.toString();
                    Log.e("jsonString", jsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                UUID.randomUUID().toString();
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(uploadUrl);
                List nameValuePairs = new ArrayList();
                //attributes for survey sync
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
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            int uploadCount = 0;
            for (int i = 0; i < jsonSyncResponses.size(); i++) {
                if (jsonParser.parseSyncResult(jsonSyncResponses.get(i))) {
                    uploadCount++;
                }
            }
            //check if all rsponses are uploaded
            if (uploadCount == completeCount) {
                Toast.makeText(SurveyDetailsActivity.this, "Responses uploaded successfully:  " + completedResponseIds.size() + "    Errors:0", Toast.LENGTH_LONG).show();
                dbAdapter.deleteFromResponseTableOnUpload(surveyId);
                completeCount = dbAdapter.getCompleteResponse(surveys.getId());
                completeTv.setText(completeCount + "");
            }
            //responses not uploaded
            else
                Toast.makeText(SurveyDetailsActivity.this, "Responses upload unsuccessful", Toast.LENGTH_SHORT).show();

        }
    }
}