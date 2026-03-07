package com.example.emergencyalertapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView sideNav = findViewById(R.id.navigation_view);
        bottomNav = findViewById(R.id.bottom_navigation);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
            else drawerLayout.openDrawer(GravityCompat.START);
        });

        sideNav.setNavigationItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();
            if (id == R.id.nav_home_main) f = new HomeFragment();
            else if (id == R.id.nav_calculator) f = new CalculatorFragment();
            else if (id == R.id.nav_converter) f = new ConverterFragment();
            else if (id == R.id.nav_timer) f = new TimerFragment();
            else if (id == R.id.nav_time_picker) f = new TimePickerFragment();
            else if (id == R.id.nav_intent) startActivity(new Intent(this, IntentDemoActivity.class));

            if (f != null) replace(f);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) f = new HomeFragment();
            else if (id == R.id.nav_contacts) f = new ContactsFragment();
            else if (id == R.id.nav_history) f = new HistoryFragment();
            if (f != null) replace(f);
            return true;
        });

        if (savedInstanceState == null) {
            replace(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void replace(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();
    }
}