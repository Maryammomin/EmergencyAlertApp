package com.example.emergencyalertapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class IntentDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_demo);

        // 1. Implicit Intent: Opening an external browser to UNICEF
        findViewById(R.id.btnImplicit).setOnClickListener(v -> {
            String url = "https://www.unicef.org";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // 2. Explicit Intent: Navigation back to MainActivity
        findViewById(R.id.btnExplicit).setOnClickListener(v -> {
            Intent intent = new Intent(IntentDemoActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close this activity
        });
    }
}