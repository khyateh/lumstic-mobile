package lumstic.example.com.lumstic.Utils;

import android.app.Application;
import android.widget.Toast;

import lumstic.example.com.lumstic.api.ApiRequestHelper;

public class LumsticApp  extends Application {


    private ApiRequestHelper apiRequestHelper;
    private Logger logger;
    private Preferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        doInit();
    }

    private void doInit() {
        apiRequestHelper = ApiRequestHelper.init(this);
        logger = Logger.init(this);
        preferences = new Preferences(this);
    }

    public synchronized ApiRequestHelper getApiRequestHelper() {
        return apiRequestHelper;
    }

    public synchronized Logger getLogger() {
        return logger;
    }

    public void showToast(String message) {
        if (null != message)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int stringResId) {
        String message = getString(stringResId);
        if (null != message)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public synchronized Preferences getPreferences() {
        return preferences;
    }
}