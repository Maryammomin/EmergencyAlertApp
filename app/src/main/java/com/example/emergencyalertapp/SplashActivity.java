package com.example.emergencyalertapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay for 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // FIXED: Changed SplashActivity.java to SplashActivity.this
                Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                startActivity(intent);

                // Close SplashActivity so user can't go back to it
                finish();
            }
        }, 3000);
    }
}