package lumstic.example.com.lumstic.Utils;

import android.app.Activity;
import android.app.ProgressDialog;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

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
}