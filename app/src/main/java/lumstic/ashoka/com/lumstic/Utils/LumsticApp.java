package lumstic.ashoka.com.lumstic.Utils;

import android.app.Application;
import android.widget.Toast;


public class LumsticApp  extends Application {



    private Logger logger;
    private Preferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        doInit();
    }

    private void doInit() {

        logger = Logger.init(this);
        preferences = new Preferences(this);
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