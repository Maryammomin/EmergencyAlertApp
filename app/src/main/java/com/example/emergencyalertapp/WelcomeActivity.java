package com.example.emergencyalertapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        findViewById(R.id.btnGoSignUp).setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        findViewById(R.id.btnGoSignIn).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }
}