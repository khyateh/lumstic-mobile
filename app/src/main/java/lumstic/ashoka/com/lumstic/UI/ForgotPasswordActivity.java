package lumstic.ashoka.com.lumstic.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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

import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.CommonUtil;
import lumstic.ashoka.com.lumstic.Utils.JSONParser;
import lumstic.ashoka.com.lumstic.Utils.LumsticApp;

public class ForgotPasswordActivity extends Activity {

    private String baseUrl = "";
    private  String url = "/api/password_resets";
    private ProgressDialog progressDialog;
    private LumsticApp lumsticApp;
    private Button requestPasswordButton;
    private EditText emailET;
    private RelativeLayout errorContainer;
    private String accessToken = "";
    private String jsonPasswordString = "";
    private String email = null;

    private ActionBar actionBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //setting up action bar
        actionBar = getActionBar();
        actionBar.setTitle("Forgot Password");
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);

        lumsticApp = (LumsticApp) getApplication();

        if(lumsticApp.getPreferences().getBaseUrl()==null){
            baseUrl=ForgotPasswordActivity.this.getResources().getString(R.string.server_url);
        }
        else
            baseUrl=lumsticApp.getPreferences().getBaseUrl();
        url=baseUrl+url;

        //setting up views
        emailET = (EditText) findViewById(R.id.email_edit_text);
        requestPasswordButton = (Button) findViewById(R.id.request_password);
        errorContainer = (RelativeLayout) findViewById(R.id.email_error_container);

        //on request password clicked
        requestPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = emailET.getText().toString();
                //show progress dialog
                progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Processing ");
                progressDialog.show();
                if (!TextUtils.isEmpty(email) && CommonUtil.validateEmail(email)) {
                    new RequestPassword().execute();
                } else {
                    lumsticApp.showToast("Enter Valid Email ");
                    progressDialog.dismiss();
                }
            }
        });

    }

    public class RequestPassword extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... voids) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                List nameValuePairs = new ArrayList();
                nameValuePairs.add(new BasicNameValuePair("email", email));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse httpResponse = httpclient.execute(httppost);
                HttpEntity httpEntity = httpResponse.getEntity();
                jsonPasswordString = EntityUtils.toString(httpEntity);
                Log.e("datainfo", jsonPasswordString);
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
            return jsonPasswordString;
        }

        @Override
        protected void onPostExecute(String result) {
            JSONObject jsonObjectForgotPassword = null;
            progressDialog.dismiss();
            try {
                jsonObjectForgotPassword = new JSONObject(jsonPasswordString);
                JSONParser jsonParser = new JSONParser();
                boolean proceed = jsonParser.parseForgotPassword(jsonObjectForgotPassword);
                if (proceed) {
                    progressDialog.dismiss();
                    Toast.makeText(ForgotPasswordActivity.this, "We have sent you a password reset link", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
                if (!proceed) {
                    progressDialog.dismiss();
                    errorContainer.setVisibility(View.VISIBLE);
                }
                else{

                    Toast.makeText(ForgotPasswordActivity.this,"Network error",Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
