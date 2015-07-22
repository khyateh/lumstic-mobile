package lumstic.ashoka.com.lumstic.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;

import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.LumsticApp;


public class SplashActivity extends Activity {

    private LumsticApp lumsticApp;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        lumsticApp = (LumsticApp) getApplication();
        //set timer for splash screen
        TimerTask task = new TimerTask() {

            @Override
            public void run() {

                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

        };
        Timer t = new Timer();
        t.schedule(task, 2000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
