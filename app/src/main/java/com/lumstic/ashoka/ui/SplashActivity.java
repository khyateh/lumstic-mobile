package com.lumstic.ashoka.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.lumstic.ashoka.R;
import com.lumstic.ashoka.utils.CommonUtil;


public class SplashActivity extends BaseActivity {


    int splashScreenDelay = 2000;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (CommonUtil.isLoggedIn(appController)) {
                    Intent i = new Intent(SplashActivity.this, DashBoardActivity.class);
                    startActivity(i);
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, splashScreenDelay);


    }

}

