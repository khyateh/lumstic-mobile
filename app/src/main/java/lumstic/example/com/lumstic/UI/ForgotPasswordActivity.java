package lumstic.example.com.lumstic.UI;

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

import lumstic.example.com.lumstic.R;
import lumstic.example.com.lumstic.Utils.CommonUtil;
import lumstic.example.com.lumstic.Utils.JSONParser;
import lumstic.example.com.lumstic.Utils.LumsticApp;

public class ForgotPasswordActivity extends Activity {

    Button requestPasswordButton;
    EditText emailET;
    String jsonPasswordString="";
    private LumsticApp lumsticApp;
    private String accessToken="";
    RelativeLayout errorContainer;


    //  https://user-owner-stgng.herokuapp.com/
    private static String url = "https://user-owner-stgng.herokuapp.com/api/password_resets";
    //  private static String url = "http://192.168.2.16:3000/api/login";

    private ProgressDialog progressDialog;
    private String email = null;


ActionBar actionBar;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        emailET=(EditText)findViewById(R.id.email_edit_text);
        lumsticApp = (LumsticApp) getApplication();
        actionBar= getActionBar();
        actionBar.setTitle("Forgot Password");
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        requestPasswordButton= (Button) findViewById(R.id.request_password);
        errorContainer= (RelativeLayout)findViewById(R.id.email_error_container);
        requestPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                email = emailET.getText().toString();
                progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Processing ");
                progressDialog.show();
                if (!TextUtils.isEmpty(email) && CommonUtil.validateEmail(email)) {

                    new RequestPassword().execute();
                }

                else {
                    lumsticApp.showToast("Enter Valid Email ");


                }

//                Toast.makeText(ForgotPasswordActivity.this,"We have sent you a password reset link",Toast.LENGTH_LONG).show();
//                Intent i = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
//                startActivity(i);
//                finish();
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


            JSONObject jsonObjectForgotPassword= null;
            try {
                jsonObjectForgotPassword = new JSONObject(jsonPasswordString);
                JSONParser jsonParser = new JSONParser();
                boolean proceed=jsonParser.parseForgotPassword(jsonObjectForgotPassword);
                if(proceed){
                    progressDialog.dismiss();
                    Toast.makeText(ForgotPasswordActivity.this,"We have sent you a password reset link",Toast.LENGTH_LONG).show();
                Intent i = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(i);
                finish();

                }
                if(!proceed){
                    progressDialog.dismiss();
                    errorContainer.setVisibility(View.VISIBLE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            JSONObject jsonObjectLogin= null;
//            try {
//                jsonObjectLogin = new JSONObject(jsonLoginString);
//                JSONParser jsonParser = new JSONParser();
//                userModel=jsonParser.parseLogin(jsonObjectLogin);
//                lumsticApp.getPreferences().setAccessToken(userModel.getAccess_token());
//                lumsticApp.getPreferences().setUserId(String.valueOf(userModel.getUser_id()));
//                lumsticApp.getPreferences().setOrganizationId(String.valueOf(userModel.getOrganisation_id()));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            if(userModel!=null){
//
//                progressDialog.dismiss();
//                Toast.makeText(LoginActivity.this,"Logged In ",Toast.LENGTH_LONG).show();
//
//
//                Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
//                startActivity(intent);
//                finish();
//
//
//            }
//

        }
    }
}
