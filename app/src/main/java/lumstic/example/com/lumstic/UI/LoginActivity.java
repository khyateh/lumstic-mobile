package lumstic.example.com.lumstic.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.List;

import lumstic.example.com.lumstic.Models.UserModel;
import lumstic.example.com.lumstic.Utils.JSONParser;
import lumstic.example.com.lumstic.Utils.LumsticApp;
import lumstic.example.com.lumstic.R;
import lumstic.example.com.lumstic.Utils.CommonUtil;

public class LoginActivity extends Activity {

    String jsonLoginString="";
    private       UserModel  userModel;
    private ActionBar actionBar;
    private Button loginButton;
    private TextView fogotPassword;
    private LumsticApp lumsticApp;
    private String accessToken="";

    //https://survey-web-stgng.herokuapp.com/
  //  https://user-owner-stgng.herokuapp.com/
   private static String url = "https://survey-web-stgng.herokuapp.com/api/login";
    //  private static String url = "http://192.168.2.16:3000/api/login";

    private EditText emailEditText, passwordEditText;
    private ProgressDialog progressDialog;
    private String email = null, password = null;
    int ctr;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        lumsticApp = (LumsticApp) getApplication();


        try{
        if(!lumsticApp.getPreferences().getAccessToken().equals(""))
        {
            Intent i = new Intent(LoginActivity.this,DashBoardActivity.class);
            startActivity(i);
            finish();
        }}
        catch (Exception e){
            e.printStackTrace();
        }
        actionBar = getActionBar();
        actionBar.setTitle("Login");
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);


        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);
        loginButton = (Button) findViewById(R.id.login_button);
        fogotPassword = (TextView) findViewById(R.id.forgot_password);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Logging in");
                progressDialog.show();
                if (!TextUtils.isEmpty(email) && CommonUtil.validateEmail(email)) {
                    lumsticApp.getPreferences().setSurveyData("");
                    new Login().execute();


                } else {
                    lumsticApp.showToast("Enter Valid Email ");


                }


            }
        });
        fogotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_help) {
            Intent i = new Intent(LoginActivity.this, HelpActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class Login extends AsyncTask<Void, Void, String> {


        protected String doInBackground(Void... voids) {

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                List nameValuePairs = new ArrayList();
                nameValuePairs.add(new BasicNameValuePair("username", email));
                nameValuePairs.add(new BasicNameValuePair("password", password));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse httpResponse = httpclient.execute(httppost);
                HttpEntity httpEntity = httpResponse.getEntity();
                jsonLoginString = EntityUtils.toString(httpEntity);
                Log.e("datainfo",jsonLoginString);
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
            return jsonLoginString;
        }

        @Override
        protected void onPostExecute(String result) {

            Log.e("isthisworking", jsonLoginString);
            JSONObject jsonObjectLogin= null;
            try {
                jsonObjectLogin = new JSONObject(jsonLoginString);
                JSONParser jsonParser = new JSONParser();
                userModel=jsonParser.parseLogin(jsonObjectLogin);
                lumsticApp.getPreferences().setAccessToken(userModel.getAccess_token());
                lumsticApp.getPreferences().setUserId(String.valueOf(userModel.getUser_id()));
                lumsticApp.getPreferences().setOrganizationId(String.valueOf(userModel.getOrganisation_id()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(userModel!=null){

                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this,"Logged In ",Toast.LENGTH_LONG).show();


                Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
                startActivity(intent);
                finish();


            }


        }
    }

}
