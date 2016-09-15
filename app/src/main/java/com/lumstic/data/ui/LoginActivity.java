package com.lumstic.data.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.lumstic.data.R;
import com.lumstic.data.models.UserModel;
import com.lumstic.data.utils.CommonUtil;
import com.lumstic.data.utils.JSONParser;
import com.lumstic.data.utils.NetworkUtil;
import com.lumstic.data.views.RobotoLightEditText;
import com.lumstic.data.views.RobotoRegularButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


public class LoginActivity extends BaseActivity {

    private String loginUrl = "/api/login";
    private UserModel userModel;
    private ProgressDialog progressDialog;
    private ActionBar actionBar;
    private RobotoRegularButton loginButton;
    private RobotoLightEditText emailEditText, passwordEditText;
    private String jsonLoginString = "";
    private String email = "", password = null;

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
        emailEditText = (RobotoLightEditText) findViewById(R.id.email_edit_text);
        passwordEditText = (RobotoLightEditText) findViewById(R.id.password_edit_text);
        loginButton = (RobotoRegularButton) findViewById(R.id.login_button);

        //on login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();

                //validation of email
                if (!TextUtils.isEmpty(email) && CommonUtil.validateEmail(email)) {
                    appController.getPreferences().setSurveyData("");

                    if (NetworkUtil.iSConnected(getApplicationContext()) == NetworkUtil.TYPE_CONNECTED) {
                        progressDialog = new ProgressDialog(LoginActivity.this);
                        progressDialog.setCancelable(false);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Logging in");
                        progressDialog.show();

                        appController.getPreferences().setUsername(email);
                        appController.getPreferences().setPassword(password);


                        new Login().execute();


                    } else {
                        appController.showToast("Please check your internet connection");
                    }


                } else {
                    appController.showToast("Enter Valid Email ");
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

            //JSONArray jsonArray = null;
            HttpURLConnection conn = null;
            String jsString = null;
            try {

                String urlString = String.format("%s%s?username=%s&password=%s", baseUrl, loginUrl, email, password);

                URL url = new URL(urlString);
                isAuthorized = false;

                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                //workaround for no auth challenges found
                try{
                    int resp = conn.getResponseCode();
                }
                catch(Exception e){
                    int resp = conn.getResponseCode();
                }

                InputStream is = new BufferedInputStream(conn.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                StringBuilder responseBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = br.readLine()) != null) responseBuilder.append(inputStr);

                jsString = responseBuilder.toString();
                isAuthorized = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                conn.disconnect();
            }

            return jsString;


        }

        // response back from server
        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonObjectLogin = new JSONObject(result);
                JSONParser jsonParser = new JSONParser();
                userModel = jsonParser.parseLogin(jsonObjectLogin);
                appController.getPreferences().setAccessToken(userModel.getAccessToken());
                appController.getPreferences().setUserId(String.valueOf(userModel.getUserId()));
                appController.getPreferences().setOrganizationId(String.valueOf(userModel.getOrganisationId()));
                appController.getPreferences().setAccessTokenCreatedAt(String.valueOf(new Date().getTime()));
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
            }else {
                if (!isAuthorized){
                    Toast.makeText(LoginActivity.this, "Invalid Username or Password", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
                else{
                    Toast.makeText(LoginActivity.this, "An error occurred during login", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        }
    }

}
