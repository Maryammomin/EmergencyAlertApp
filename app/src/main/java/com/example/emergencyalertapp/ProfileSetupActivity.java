package com.example.emergencyalertapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ProfileSetupActivity extends AppCompatActivity {
    private LinearLayout contactsContainer;
    private ArrayList<EditText> contactFields = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        contactsContainer = findViewById(R.id.contactsContainer);
        ImageButton btnAdd = findViewById(R.id.btnAddContact);

        // Add the first field by default
        addContactField();

        btnAdd.setOnClickListener(v -> {
            if (contactFields.size() < 10) {
                addContactField();
            } else {
                Toast.makeText(this, "Maximum 10 contacts reached", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnFinish).setOnClickListener(v -> saveAndFinish());
    }

    private void addContactField() {
        EditText et = new EditText(this);
        et.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150));
        et.setHint("Contact Number " + (contactFields.size() + 1));
        et.setHintTextColor(getResources().getColor(R.color.gray_text_dark));
        et.setTextColor(getResources().getColor(R.color.black_text));
        et.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        et.setBackgroundResource(android.R.drawable.editbox_background);

        contactsContainer.addView(et);
        contactFields.add(et);
    }

    private void saveAndFinish() {
        // Here you would save name, age, gender, and contactFields to SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}