package com.lumstic.data.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lumstic.data.R;
import com.lumstic.data.adapters.DBAdapter;
import com.lumstic.data.utils.CommonUtil;
import com.lumstic.data.utils.JSONParser;
import com.lumstic.data.utils.NetworkUtil;
import com.lumstic.data.views.LumsticTextChangeListener;
import com.lumstic.data.views.RobotoLightEditText;
import com.lumstic.data.views.RobotoRegularButton;
import com.lumstic.data.views.RobotoRegularTextView;

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


public class SettingsActivity extends BaseActivity {

    RelativeLayout errorContainer;
    ProgressDialog progressDialog;
    Dialog forgotPasswordDialog;
    String forgotPasswordURL = "/api/password_resets";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(true);


        progressDialog = new ProgressDialog(SettingsActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);


        errorContainer = (RelativeLayout) findViewById(R.id.email_error_container);

        findViewById(R.id.tv_forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPasswordDialog = new Dialog(SettingsActivity.this);
                forgotPasswordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
                forgotPasswordDialog.setContentView(R.layout.view_forgot_password_dialog);
                forgotPasswordDialog.show();

                final RobotoLightEditText etEmail = (RobotoLightEditText) forgotPasswordDialog.findViewById(R.id
                        .email_edit_text);


                RobotoRegularButton button = (RobotoRegularButton) forgotPasswordDialog.findViewById(R.id.request_password);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (NetworkUtil.iSConnected(getApplicationContext()) == NetworkUtil.TYPE_CONNECTED) {
                            if (!TextUtils.isEmpty(etEmail.getText().toString()) && CommonUtil.validateEmail(etEmail.getText().toString())) {
                                progressDialog.setMessage("Requesting....");
                                progressDialog.show();
                                new RequestPassword().execute(userBaseUrl + forgotPasswordURL, etEmail.getText().toString());
                            } else {
                                appController.showToast("Enter Valid Email ");
                            }
                        } else {
                            appController.showToast("Please check your internet connection");
                        }
                    }
                });


            }
        });
        findViewById(R.id.tv_remote_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog mainDialog = new Dialog(SettingsActivity.this);
                mainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
                mainDialog.setContentView(R.layout.view_remote_server_settings_dialog);
                mainDialog.show();

                final RobotoLightEditText etURL = (RobotoLightEditText) mainDialog.findViewById(R.id.server_address);
                if (appController.getPreferences().getBaseUrl() != null) {
                    etURL.setText(appController.getPreferences().getBaseUrl());
                } else {
                    etURL.setText(getResources().getString(R.string.server_url));
                }

                RobotoRegularButton button = (RobotoRegularButton) mainDialog.findViewById(R.id.save);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
                        alertDialogBuilder.setTitle("Change of Server")
                                .setMessage("This will clear the database and log you out, Are you sure?")
                                .setCancelable(false)
                                .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int id) {
                                                appController.getPreferences().setBaseUrl(etURL.getText().toString());
                                                BaseActivity.baseUrl = etURL.getText().toString();
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


            }
        });
        findViewById(R.id.tv_user_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog mainDialog = new Dialog(SettingsActivity.this);
                mainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
                mainDialog.setContentView(R.layout.view_remote_server_settings_dialog);
                mainDialog.show();
                ((RobotoRegularTextView) mainDialog.findViewById(R.id.tv_title)).setText("USER SERVER LOCATION ");
                final RobotoLightEditText etURL = (RobotoLightEditText) mainDialog.findViewById(R.id.server_address);
                if (appController.getPreferences().getUserBaseUrl() != null) {
                    etURL.setText(appController.getPreferences().getUserBaseUrl());
                } else {
                    etURL.setText(getResources().getString(R.string.user_server_url));
                }

                RobotoRegularButton button = (RobotoRegularButton) mainDialog.findViewById(R.id.save);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        appController.getPreferences().setUserBaseUrl(etURL.getText().toString());
                        BaseActivity.userBaseUrl = etURL.getText().toString();
                        mainDialog.dismiss();
                    }
                });


            }
        });

        //TODO JYOTHI Dec21/2016 --> location fetch waiting time configurable

        RobotoLightEditText locWaitingTime = (RobotoLightEditText)findViewById(R.id.gps_waiting_time);
        locWaitingTime.setText(String.valueOf(appController.getPreferences().getLocWaitingTime()));
       // locWaitingTime.setFilters(new InputFilter[]{ new LumSticMinMaxInputFilter("1", "600")});
        locWaitingTime.addTextChangedListener(new LumsticTextChangeListener<RobotoLightEditText>(locWaitingTime){
        @Override
        public void onTextChanged(RobotoLightEditText target, Editable s) {
         //   CommonUtil.printmsg("In onTextChanged for time limit");
         String timeLimit = target.getText().toString();


            if (timeLimit != null) {
                timeLimit = timeLimit.trim();

                try {
                   int localValue = Integer.parseInt(timeLimit);

                    if (localValue > CommonUtil.LUMSTIC_LOC_WAITING_MAX_TIME || localValue < CommonUtil.LUMSTIC_LOC_WAITING_MIN_TIME) {
                        CommonUtil.showValidationDialog(target,timeLimit.length(),CommonUtil.LUMSTIC_LOC_WAITING_MIN_TIME,CommonUtil.LUMSTIC_LOC_WAITING_MAX_TIME,SettingsActivity.this);
                        target.setText(String.valueOf(appController.getPreferences().getLocWaitingTime()));
                        return;
                    }else{
                        appController.getPreferences().setLocWaitTime(localValue);
                    }
                } catch (NumberFormatException e) {

                }

            }


        }


        });



    }



    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class RequestPassword extends AsyncTask<String, Void, String> {
        String jsonPasswordString;

        protected String doInBackground(String... params) {


            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(params[0]);
                List nameValuePairs = new ArrayList();
                nameValuePairs.add(new BasicNameValuePair("email", params[1]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse httpResponse = httpclient.execute(httppost);
                HttpEntity httpEntity = httpResponse.getEntity();
                jsonPasswordString = EntityUtils.toString(httpEntity);
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
            return jsonPasswordString;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObjectForgotPassword = new JSONObject(jsonPasswordString);
                JSONParser jsonParser = new JSONParser();
                boolean proceed = jsonParser.parseForgotPassword(jsonObjectForgotPassword);
                if (proceed) {
                    Toast.makeText(SettingsActivity.this, "We have sent you a password reset link", Toast.LENGTH_LONG).show();
                    forgotPasswordDialog.dismiss();
                } else {
                    errorContainer.setVisibility(View.VISIBLE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
        }
    }

}

