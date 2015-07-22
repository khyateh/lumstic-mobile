package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
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
import lumstic.ashoka.com.lumstic.Adapters.DashBoardAdapter;
import lumstic.ashoka.com.lumstic.Models.Answers;
import lumstic.ashoka.com.lumstic.Models.Surveys;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.IntentConstants;
import lumstic.ashoka.com.lumstic.Utils.JSONParser;
import lumstic.ashoka.com.lumstic.Utils.JsonHelper;
import lumstic.ashoka.com.lumstic.Utils.LumsticApp;
import lumstic.ashoka.com.lumstic.Views.RobotoBlackButton;


public class DashBoardActivity extends Activity {

    private LumsticApp lumsticApp;
    private ActionBar actionBar;

    private JsonHelper jsonHelper;
    private DBAdapter dbAdapter;
    private Surveys surveys;
    private DashBoardAdapter dashBoardAdapter;
    private LocationManager locationManager;
    private ProgressDialog progressDialog;

    private JSONArray jsonArray;
    private JSONParser jsonParser;

    private ListView listView;
    private LinearLayout uploadContainer;
    private RobotoBlackButton uploadButton;

    private List<Integer> completedResponseIds;
    private List<Answers> answerses;
    private List<String> jsonSyncResponses;
    private List<Surveys> surveysList;

    private int surveyId;
    private int completeCount = 0;
    private int uploadCount = 0;
    private String timestamp = "";
    private String mobilId;
    private String baseUrl = "";

    private String uploadUrl = "/api/responses.json?";
    private String jsonStr = null;
    private String syncString = "";
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private double lat = 0, lon = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        //setting up action bar
        actionBar = getActionBar();
        actionBar.setTitle("Dashboard");
        dbAdapter = new DBAdapter(DashBoardActivity.this);
        lumsticApp = (LumsticApp) getApplication();
        if(lumsticApp.getPreferences().getBaseUrl()==null){
            baseUrl=DashBoardActivity.this.getResources().getString(R.string.server_url);
        }
        else
            baseUrl=lumsticApp.getPreferences().getBaseUrl();
        uploadUrl=baseUrl+uploadUrl;
        surveysList = new ArrayList<Surveys>();
        jsonHelper = new JsonHelper(DashBoardActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!lumsticApp.getPreferences().getSurveyData().equals("")) {
            setContentView(R.layout.test);
            uploadContainer = (LinearLayout) findViewById(R.id.upload_container);
            uploadContainer.setVisibility(View.GONE);
            uploadButton = (RobotoBlackButton) findViewById(R.id.upload_all);
            completeCount = dbAdapter.getCompleteResponseFull();
            jsonParser = new JSONParser();
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
                        progressDialog = new ProgressDialog(DashBoardActivity.this);
                        progressDialog.setCancelable(false);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Sync in Progress");
                        progressDialog.show();
                        uploadContainer.setVisibility(View.GONE);
                        new uploadResponse().execute();
                    }
                }
            });
            surveysList = jsonHelper.tryParsing(lumsticApp.getPreferences().getSurveyData());
            listView = (ListView) findViewById(R.id.active_survey_list);
            if (surveysList != null) {
                dashBoardAdapter = new DashBoardAdapter(getApplicationContext(), surveysList);
                listView.setAdapter(dashBoardAdapter);
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
            try {
                dashBoardAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
            completeCount = dbAdapter.getCompleteResponseFull();
            if (dbAdapter.getCompleteResponseFull() != 0) {
                uploadContainer.setVisibility(View.VISIBLE);
            }
            if (dbAdapter.getCompleteResponseFull() == 0) {
                uploadContainer.setVisibility(View.GONE);
            }
        }
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

    //if location is on
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dash_board, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_fetch) {
            Intent i = new Intent(DashBoardActivity.this, ActiveSurveyActivity.class);
            startActivity(i);

            return true;
        }
        if (id == R.id.action_logout) {
            final Dialog dialog = new Dialog(DashBoardActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
            dialog.setContentView(R.layout.logout_dialog);
            dialog.show();
            Button button = (Button) dialog.findViewById(R.id.okay);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lumsticApp.getPreferences().setAccessToken("");
                    Intent i = new Intent(DashBoardActivity.this, LoginActivity.class);
                    startActivity(i);
                    dialog.dismiss();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                for (int i = 0; i < jsonSyncResponses.size(); i++) {
                    if (jsonParser.parseSyncResult(jsonSyncResponses.get(i))) {
                        uploadCount++;
                        dbAdapter.deleteFromResponseTableOnUpload(surveyId);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (uploadCount == completeCount) {
                dashBoardAdapter.notifyDataSetChanged();
                Toast.makeText(DashBoardActivity.this, "Responses uploaded successfully:  " + uploadCount + "    Errors:0", Toast.LENGTH_LONG).show();
                uploadCount = 0;
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
