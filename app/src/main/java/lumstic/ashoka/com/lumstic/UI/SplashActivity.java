package lumstic.ashoka.com.lumstic.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.CommonUtil;


public class SplashActivity extends BaseActivity {


    int SPLASH_SCREEN_DELAY = 2000;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (CommonUtil.isLoggedIn(lumsticApp)) {
                    Intent i = new Intent(SplashActivity.this, DashBoardActivity.class);
                    startActivity(i);
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, SPLASH_SCREEN_DELAY);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_splash, menu);
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

