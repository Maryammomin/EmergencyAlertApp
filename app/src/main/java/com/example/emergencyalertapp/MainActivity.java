package com.example.emergencyalertapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permissions Check
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE}, 1);
        }

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);

        // Default Fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        nav.setSelectedItemId(R.id.nav_home);

        nav.setOnItemSelectedListener(item -> {
            Fragment f = null;
            if (item.getItemId() == R.id.nav_home) f = new HomeFragment();
            else if (item.getItemId() == R.id.nav_contacts) f = new ContactsFragment();
            else if (item.getItemId() == R.id.nav_history) f = new HistoryFragment();

            if (f != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();
            }
            return true;
        });
    }
}