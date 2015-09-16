package com.lumstic.ashoka.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.adapters.DBAdapter;
import com.lumstic.ashoka.models.Answers;
import com.lumstic.ashoka.models.UserModel;
import com.lumstic.ashoka.ui.LoginActivity;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommonUtil {

    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    //Question Type isCategory OR NOT
    public static final int TYPE_QUESTION = 0;
    public static final int TYPE_CATEGORY = 1;
    //Question Type
    public static final String QUESTION_TYPE_SINGLE_LINE_QUESTION = "SingleLineQuestion";
    public static final String QUESTION_TYPE_NUMERIC_QUESTION = "NumericQuestion";
    public static final String QUESTION_TYPE_DATE_QUESTION = "DateQuestion";
    public static final String QUESTION_TYPE_RATING_QUESTION = "RatingQuestion";
    public static final String QUESTION_TYPE_PHOTO_QUESTION = "PhotoQuestion";
    public static final String QUESTION_TYPE_MULTI_CHOICE_QUESTION = "MultiChoiceQuestion";
    public static final String QUESTION_TYPE_RADIO_QUESTION = "RadioQuestion";
    public static final String QUESTION_TYPE_DROPDOWN_QUESTION = "DropDownQuestion";
    public static final String QUESTION_TYPE_MULTI_LINE_QUESTION = "MultilineQuestion";
    //category type
    public static final String CATEGORY_TYPE_MULTI_RECORD = "MultiRecordCategory";


    public static final boolean isChildView = true;
    public static final boolean isParentView = false;

    private CommonUtil() {
    }

    public static boolean validateEmail(String email) {
        Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    public static String getServerNameFromURL(String url) {
        try {
            int from = url.indexOf("//") + 2;
            int to = url.indexOf("/", from);
            if (to < 0) {
                return url.substring(from);
            }
            return url.substring(from, to);
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }

    }

    public static String getNodeNameFromURL(String url) {
        try {
            int firstIndex = url.indexOf("//");
            int from = url.indexOf("/", firstIndex + 2);
            if (url.charAt(from - 1) == '/') {
                return "";
            }
            int to = url.length();
            return url.substring(from, to);
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }
    }

    public static String getPath(Activity activity, Uri uri) {
        if (null != uri) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
            if (null != cursor && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        }
        return null;
    }

    public static String getCSV(List<String> stringList) {
        String temp = "";
        for (String s : stringList)
            temp += s + ",";
        if (temp.length() > 0)
            return temp.substring(0, temp.length() - 1);
        return "";
    }

    public static ProgressDialog getProgressDialog(Activity activity) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        return progressDialog;
    }

    public static String getValidLatitude(LumsticApp lumsticApp) {
        String latitude = lumsticApp.getPreferences().getLatitude();

        if (latitude == null) {
            latitude = "18.54194666666656";
        }

        return latitude;
    }

    public static String getValidLongitude(LumsticApp lumsticApp) {
        String longitude = lumsticApp.getPreferences().getLongitude();

        if (longitude == null) {
            longitude = "73.8291466666657";
        }

        return longitude;
    }

    public static Boolean isLoggedIn(LumsticApp lumsticApp) {

        try {
            if (!lumsticApp.getPreferences().getAccessToken().equals("")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void Logout(final Activity localActivity, final LumsticApp lumsticApp) {
        final Dialog dialog = new Dialog(localActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.logout_dialog);
        dialog.show();
        Button button = (Button) dialog.findViewById(R.id.okay);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lumsticApp.getPreferences().setAccessToken("");
                Intent i = new Intent(localActivity, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                localActivity.startActivity(i);
                dialog.dismiss();
                localActivity.finish();
            }
        });
    }

    private static void expireCurrentSession(LumsticApp lumsticApp) {
        lumsticApp.getPreferences().setAccess_token_created_at(null);
        lumsticApp.getPreferences().setAccessToken(null);
    }

    public static void reValidateToken(LumsticApp lumsticApp, String url) {
        Log.e("TAG", "Revalidating Token");
        UserModel userModel;
        String jsonLoginString = null;
        //sends email and password to the server as name value pairs
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);

            List nameValuePairs = new ArrayList();
            nameValuePairs.add(new BasicNameValuePair("username", lumsticApp.getPreferences().getUsername()));
            nameValuePairs.add(new BasicNameValuePair("password", lumsticApp.getPreferences().getPassword()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse httpResponse = httpclient.execute(httppost);
            HttpEntity httpEntity = httpResponse.getEntity();
            jsonLoginString = EntityUtils.toString(httpEntity);

        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
        JSONObject jsonObjectLogin;
        try {
            jsonObjectLogin = new JSONObject(jsonLoginString);
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
    }

    public static boolean isTokenExpired(LumsticApp lumsticApp) {
        boolean isExpired = false;
        if (lumsticApp.getPreferences().getAccessToken() != null) {
            long diffInMs = new Date().getTime() - Long.valueOf(lumsticApp.getPreferences().getAccess_token_created_at());
            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
            if (diffInSec > 35000) {
                //expire current session
                expireCurrentSession(lumsticApp);
                isExpired = true;
            }
        }

        return isExpired;
    }

    public static JSONObject getAnswerJsonObject(List<Answers> answers, DBAdapter dbAdapter) {
        JSONObject ansJsonObject = new JSONObject();
        for (int j = 0; j < answers.size(); j++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("question_id", answers.get(j).getQuestion_id());
                jsonObject.put("updated_at", answers.get(j).getUpdated_at());
                jsonObject.put("content", answers.get(j).getContent());
                jsonObject.put("record_id", answers.get(j).getRecordId());


                try {
                    if ((answers.get(j).getType().equals(CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION)) && (dbAdapter.getChoicesCount(answers.get(j).getId()) == 0)) {
                        jsonObject.put("option_ids", JSONObject.NULL);
                        jsonObject.remove("content");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if ((answers.get(j).getType().equals(CommonUtil.QUESTION_TYPE_DROPDOWN_QUESTION)) || (answers.get(j).getType().equals(CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION)) || (answers.get(j).getType().equals(CommonUtil.QUESTION_TYPE_RADIO_QUESTION))) {
                        int recordId = answers.get(j).getRecordId();
                        if ((answers.get(j).getContent().equals("")) && (dbAdapter.getChoicesCountWhereAnswerIdIs(answers.get(j).getId()) > 0)) {
                            String type = dbAdapter.getQuestionTypeWhereAnswerIdIs(answers.get(j).getId());
                            if (type.equals(CommonUtil.QUESTION_TYPE_RADIO_QUESTION)) {
                                jsonObject.put("content", dbAdapter.getChoicesWhereAnswerCountIsOne(answers.get(j).getId()));
                            }
                            if (type.equals(CommonUtil.QUESTION_TYPE_DROPDOWN_QUESTION)) {
                                jsonObject.put("content", dbAdapter.getChoicesWhereAnswerCountIsOne(answers.get(j).getId()));
                            }
                            if (type.equals(CommonUtil.QUESTION_TYPE_MULTI_CHOICE_QUESTION)) {
                                List<Integer> options = dbAdapter.getChoicesWhereAnswerCountIsMoreThanOne(answers.get(j).getId());
                                if (options.size() > 0) {
                                    JSONArray localJsonArray = new JSONArray();
                                    for (int i = 0; i < options.size(); i++) {
                                        localJsonArray.put(options.get(i));
                                    }
                                    jsonObject.putOpt("option_ids", localJsonArray);
                                }
                                jsonObject.remove("content");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (answers.get(j).getType().equals(CommonUtil.QUESTION_TYPE_PHOTO_QUESTION)) {
                        String path = Environment.getExternalStorageDirectory().toString() + "/saved_images";
                        Bitmap b = null;
                        String fileName = answers.get(j).getImage();
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
            try {
                ansJsonObject.putOpt("" + j, jsonObject);
            } catch (Exception e) {

            }
        }
        return ansJsonObject;
    }

    public static long getCurrentTimeStamp() {
        return (System.currentTimeMillis() / 1000);
    }


}