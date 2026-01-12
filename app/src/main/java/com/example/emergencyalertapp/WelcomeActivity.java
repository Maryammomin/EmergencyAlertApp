package com.example.emergencyalertapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Match IDs from your XML
        MaterialButton btnSignUp = findViewById(R.id.btnGoSignUp);
        MaterialButton btnSignIn = findViewById(R.id.btnGoSignIn);

        // Navigate to Signup Page
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Navigate to Signin (Login) Page
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}