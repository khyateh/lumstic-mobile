package com.lumstic.data.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.lumstic.data.R;
import com.lumstic.data.adapters.DBAdapter;
import com.lumstic.data.models.Answers;
import com.lumstic.data.models.UserModel;
import com.lumstic.data.ui.LoginActivity;
import com.lumstic.data.views.RobotoRegularButton;

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
    //Http Verbs
    public static final String VERB_PUT = "PUT";
    public static final String VERB_POST = "POST";
    public static final String VERB_GET = "GET";
    public static final String VERB_PATCH = "PATCH";
    public static final String VERB_DELETE = "DELETE";

    public static final String YES = "Yes";
    public static final String NO = "No";

    public static final String DELETE_RESPONSE_DIALOG_TITLE = "Delete Response";
    public static final String DELETE_RESPONSE_DIALOG_MSG = "Are you sure you want to delete this response from the device?";

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
    public static final String SURVEY_MIDLINE_SEPARATOR_TEXT = "Midline Surveys";
    //category type
    public static final String CATEGORY_TYPE_MULTI_RECORD = "MultiRecordCategory";

    public static final String APP_IMAGE_DIR = "Lumstic";
    public static final boolean IS_CHILD_VIEW = true;
    public static final boolean IS_PARENT_VIEW = false;

    public static final String SURVEY_STATUS_COMPLETE = "complete";
    public static final String SURVEY_STATUS_INCOMPLETE = "incomplete";
    //TODO JYOTHI DEC 6
    public static final String LUMSTIC_BLANK = "";

    private CommonUtil() {
    }

    public static boolean validateEmail(String email) {
        Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }


    public static String getValidLatitude(AppController appController) {
        String latitude = appController.getPreferences().getLatitude();

        if (latitude == null) {
            latitude = "18.54194666666656";
        }

        return latitude;
    }

    public static String getValidLongitude(AppController appController) {
        String longitude = appController.getPreferences().getLongitude();

        if (longitude == null) {
            longitude = "73.8291466666657";
        }

        return longitude;
    }

    public static Boolean isLoggedIn(AppController appController) {

        try {
            if (!appController.getPreferences().getAccessToken().equals("")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void logout(final Activity localActivity, final AppController appController) {
        final Dialog dialog = new Dialog(localActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.logout_dialog);
        dialog.show();
        RobotoRegularButton button = (RobotoRegularButton) dialog.findViewById(R.id.okay);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appController.getPreferences().setAccessToken("");
                Intent i = new Intent(localActivity, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                localActivity.startActivity(i);
                dialog.dismiss();
                localActivity.finish();
            }
        });
    }

    private static void expireCurrentSession(AppController appController) {
        appController.getPreferences().setAccessTokenCreatedAt(null);
        appController.getPreferences().setAccessToken(null);
    }

    public static void reValidateToken(AppController appController, String url) {
        Log.e("TAG", "Revalidating Token");
        UserModel userModel;
        String jsonLoginString = null;
        //sends email and password to the server as name value pairs
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);

            List nameValuePairs = new ArrayList();
            nameValuePairs.add(new BasicNameValuePair("username", appController.getPreferences().getUsername()));
            nameValuePairs.add(new BasicNameValuePair("password", appController.getPreferences().getPassword()));
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
            appController.getPreferences().setAccessToken(userModel.getAccessToken());
            appController.getPreferences().setUserId(String.valueOf(userModel.getUserId()));
            appController.getPreferences().setOrganizationId(String.valueOf(userModel.getOrganisationId()));
            appController.getPreferences().setAccessTokenCreatedAt(String.valueOf(new Date().getTime()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static boolean isTokenExpired(AppController appController) {
        boolean isExpired = false;
        if (appController.getPreferences().getAccessToken() != null) {
            long diffInMs = new Date().getTime() - Long.valueOf(appController.getPreferences().getAccessTokenCreatedAt());
            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
            if (diffInSec > 35000) {
                //expire current session
                expireCurrentSession(appController);
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
                jsonObject.put("question_id", answers.get(j).getQuestionId());
                jsonObject.put("updated_at", answers.get(j).getUpdatedAt());
                jsonObject.put("content", answers.get(j).getContent());
                jsonObject.put("record_id", answers.get(j).getRecordId());
                jsonObject.put("localId", answers.get(j).getId());


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
                                if (!options.isEmpty()) {
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
                        Bitmap b = null;
                        String fileName = answers.get(j).getImage();
                        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES), APP_IMAGE_DIR);
                        try {

                            File f = new File(mediaStorageDir, fileName);
//                            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//                            bitmapOptions.inJustDecodeBounds = true;
//                            FileInputStream fis = new FileInputStream(f);
//                            BitmapFactory.decodeStream(fis, null, bitmapOptions);
//                            fis.close();
//                            int REQUIRED_SIZE = 1000;
//                            int scale = 2;
//                            while (bitmapOptions.outWidth / scale / 2 >= REQUIRED_SIZE && bitmapOptions
//                                    .outHeight / scale / 2 >= REQUIRED_SIZE) {
//                                scale *= 2;
//                            }
//                            BitmapFactory.Options op = new BitmapFactory.Options();
//                            op.inSampleSize = scale;
//                            fis = new FileInputStream(f);
//                            b = BitmapFactory.decodeStream(fis, null, op);
//                            fis.close();
                            b = BitmapFactory.decodeStream(new FileInputStream(f));

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        b.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        //TODO code change to escape base64 image from utf8 encoding during upload
                        String encoded = "\""+Base64.encodeToString(byteArray, Base64.NO_WRAP)+"\"";
                       // CommonUtil.printmsg("Encoded base 64 image::"+encoded);
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
                ////TODO jyothi dec 1 2016
               /* String contentInJsonObj = (String) jsonObject.get("content");
                CommonUtil.printmsg("Answer content::"+contentInJsonObj+"\n");*/
                // todo end jyothi dec 1
                ansJsonObject.putOpt("" + j, jsonObject);
                String contentInJsonObj = (String) jsonObject.get("content");
               // CommonUtil.printmsg("Answer content::Answer type"+contentInJsonObj+" "+answers.get(j).getType()+"\n");
            } catch (Exception e) {

            }
        }
        return ansJsonObject;
    }

    public static long getCurrentTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

   /* public static void printmsg(String msg){
        System.out.println(msg);
    }*/


}