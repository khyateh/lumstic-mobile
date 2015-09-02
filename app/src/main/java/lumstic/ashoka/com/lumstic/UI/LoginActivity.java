package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lumstic.ashoka.com.lumstic.Adapters.DBAdapter;
import lumstic.ashoka.com.lumstic.Models.UserModel;
import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.CommonUtil;
import lumstic.ashoka.com.lumstic.Utils.JSONParser;
import lumstic.ashoka.com.lumstic.Utils.NetworkUtil;

public class LoginActivity extends BaseActivity {

    private String loginUrl = "/api/login";
    private UserModel userModel;
    private ProgressDialog progressDialog;
    private ActionBar actionBar;
    private TextView forgotPassword;
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
        forgotPassword = (TextView) findViewById(R.id.forgot_password);

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

        //on user forgot password
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
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

            final Dialog mainDialog = new Dialog(LoginActivity.this);
            mainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
            mainDialog.setContentView(R.layout.settings_dialog);
            mainDialog.show();

            final EditText etURL = (EditText) mainDialog.findViewById(R.id.server_address);
            if (lumsticApp.getPreferences().getBaseUrl() != null) {
                etURL.setText(lumsticApp.getPreferences().getBaseUrl());
            } else {
                etURL.setText(getResources().getString(R.string.server_url));
            }

            Button button = (Button) mainDialog.findViewById(R.id.save);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                    alertDialogBuilder.setTitle("Change of Server")
                            .setMessage("This will clear the database and log you out, Are you sure?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            lumsticApp.getPreferences().setBaseUrl(etURL.getText().toString());
                                            baseUrl = etURL.getText().toString();
                                            dialog.dismiss();
                                            mainDialog.dismiss();
                                            getApplicationContext().deleteDatabase(DBAdapter.DBhelper.DATABASE_NAME);
                                        }
                                    });
                    alertDialogBuilder.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    mainDialog.dismiss();
                                }
                            });
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();


                }
            });

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
            } catch (IOException e) {
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
                lumsticApp.getPreferences().setAccessToken(userModel.getAccess_token());
                lumsticApp.getPreferences().setUserId(String.valueOf(userModel.getUser_id()));
                lumsticApp.getPreferences().setOrganizationId(String.valueOf(userModel.getOrganisation_id()));
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
