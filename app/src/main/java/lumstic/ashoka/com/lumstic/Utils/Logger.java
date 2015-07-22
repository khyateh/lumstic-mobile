package lumstic.ashoka.com.lumstic.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import lumstic.ashoka.com.lumstic.R;

public class Logger {
    public static String TAG = "Lumstic";
    private static Logger instance;
	private LumsticApp application;
	boolean debugEnabled;
    private LumsticApp lumsticApp;
    private ProgressDialog progressDialog;
    public static synchronized Logger init(LumsticApp application) {
        if(null == instance) {
            instance = new Logger(application);
        }
        return instance;
    }
    private Logger(LumsticApp application) {
		super();
		this.setApplication(application);
        debugEnabled = this.application.getResources().getBoolean(R.bool.isDebugEnabled);
	}
	public void debug(String msg) {
		if (debugEnabled && msg!=null) {
			Log.d(TAG, msg);
		}}
	public void debug(String msg, Throwable t) {
		if (debugEnabled && msg!=null) {
			Log.d(TAG, msg, t);
		}
	}

	public void debug(Throwable t) {
		if (debugEnabled) {
			Log.d(TAG, "Exception:", t);
		}
	}

	public void debug(String tag, String msg) {
		if (debugEnabled && msg!=null) {
			Log.d(tag, msg);
		}
	}
	
	public void warn(String msg) {
		Log.w(TAG, msg);
	}

	public void info(String msg) {
		Log.i(TAG, msg);
	}

	public void error(String msg) {
		Log.e(TAG, msg);
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

    public Context getApplication() {
		return application;
	}

    public void setApplication(LumsticApp application) {
		this.application = application;
	}
}
