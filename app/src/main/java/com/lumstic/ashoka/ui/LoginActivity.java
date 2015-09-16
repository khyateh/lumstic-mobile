package com.lumstic.ashoka.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.models.UserModel;
import com.lumstic.ashoka.utils.CommonUtil;
import com.lumstic.ashoka.utils.JSONParser;
import com.lumstic.ashoka.utils.NetworkUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LoginActivity extends BaseActivity {

    private String loginUrl = "/api/login";
    private UserModel userModel;
    private ProgressDialog progressDialog;
    private ActionBar actionBar;
    private Button loginButton;
    private EditText emailEditText, passwordEditText;
    private String jsonLoginString = "";
    private String email = null, password = null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialize action bar
        actionBar = getActionBar();
        actionBar.setTitle("Login");
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);


        //initialize views and variables
        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        loginButton = (Button) findViewById(R.id.login_button);

        //on login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();

                //validation of email
                if (!TextUtils.isEmpty(email) && CommonUtil.validateEmail(email)) {
                    lumsticApp.getPreferences().setSurveyData("");

                    if (NetworkUtil.iSConnected(getApplicationContext()) == NetworkUtil.TYPE_CONNECTED) {
                        progressDialog = new ProgressDialog(LoginActivity.this);
                        progressDialog.setCancelable(false);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Logging in");
                        progressDialog.show();

                        lumsticApp.getPreferences().setUsername(email);
                        lumsticApp.getPreferences().setPassword(password);


                        new Login().execute();
                    } else {
                        lumsticApp.showToast("Please check your internet connection");
                    }


                } else {
                    lumsticApp.showToast("Enter Valid Email ");
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //asyn class on new thread to api call progress
    public class Login extends AsyncTask<Void, Void, String> {

        Boolean isAuthorized = false;

        protected String doInBackground(Void... voids) {

            //sends email and password to the server as name value pairs
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(baseUrl + loginUrl);

                List nameValuePairs = new ArrayList();
                nameValuePairs.add(new BasicNameValuePair("username", email));
                nameValuePairs.add(new BasicNameValuePair("password", password));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse httpResponse = httpclient.execute(httppost);

                if (httpResponse.getStatusLine().getStatusCode() == 401) {
                    isAuthorized = false;
                } else {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    jsonLoginString = EntityUtils.toString(httpEntity);
                    isAuthorized = true;
                }


            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonLoginString;
        }

        // response back from server
        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonObjectLogin = new JSONObject(jsonLoginString);
                JSONParser jsonParser = new JSONParser();
                userModel = jsonParser.parseLogin(jsonObjectLogin);
                lumsticApp.getPreferences().setAccessToken(userModel.getAccessToken());
                lumsticApp.getPreferences().setUserId(String.valueOf(userModel.getUserId()));
                lumsticApp.getPreferences().setOrganizationId(String.valueOf(userModel.getOrganisationId()));
                lumsticApp.getPreferences().setAccess_token_created_at(String.valueOf(new Date().getTime()));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if (userModel != null) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Logged In ", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
                startActivity(intent);
                finish();
            }

            if (!isAuthorized) {
                Toast.makeText(LoginActivity.this, "Invalid Username or Password", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }
    }

}
