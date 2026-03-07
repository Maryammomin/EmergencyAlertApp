package com.example.emergencyalertapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

public class ProfileInfoActivity extends AppCompatActivity {
    private TextInputEditText etName, etDOB, etMobile;
    private RadioGroup rgGender;
    private AutoCompleteTextView spinnerState;
    private SwitchMaterial switchTerms;
    private LinearProgressIndicator progressBar;
    private MaterialButton btnNext;

    private final String DB_URL = "https://emergencyalertapp-95004-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        etName = findViewById(R.id.etName);
        etDOB = findViewById(R.id.etDOB);
        etMobile = findViewById(R.id.etMobile);
        rgGender = findViewById(R.id.rgGender);
        spinnerState = findViewById(R.id.spinnerState);
        switchTerms = findViewById(R.id.switchTerms);
        progressBar = findViewById(R.id.setupProgress);
        btnNext = findViewById(R.id.btnNextProfile);

        // 1. Setup State Dropdown
        String[] states = {"Andhra Pradesh", "Bihar", "Delhi", "Goa", "Gujarat", "Karnataka", "Maharashtra", "Punjab", "Rajasthan", "Tamil Nadu", "Uttar Pradesh", "West Bengal"};
        spinnerState.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, states));

        // 2. Setup Date Picker with Future Date Blocking
        etDOB.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
                etDOB.setText(d + "/" + (m + 1) + "/" + y);
                updateProgress();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

            // This line prevents selecting future dates
            dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
            dpd.show();
        });

        // 3. Setup Progress Watchers
        setupWatchers();

        // 4. Handle Next Button
        btnNext.setOnClickListener(v -> {
            if (switchTerms.isChecked()) {
                saveDataAndGoNext();
            } else {
                Toast.makeText(this, "Please accept Terms & Conditions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupWatchers() {
        TextWatcher tw = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { updateProgress(); }
            public void afterTextChanged(Editable s) {}
        };
        etName.addTextChangedListener(tw);
        etMobile.addTextChangedListener(tw);
        rgGender.setOnCheckedChangeListener((g, i) -> updateProgress());
        spinnerState.setOnItemClickListener((p, v, pos, id) -> updateProgress());
        switchTerms.setOnCheckedChangeListener((b, c) -> updateProgress());
    }

    private void updateProgress() {
        int count = 0;
        if (!etName.getText().toString().isEmpty()) count++;
        if (!etDOB.getText().toString().isEmpty()) count++;
        if (!etMobile.getText().toString().isEmpty()) count++;
        if (rgGender.getCheckedRadioButtonId() != -1) count++;
        if (!spinnerState.getText().toString().isEmpty()) count++;
        if (switchTerms.isChecked()) count++;
        progressBar.setProgress((int) (count * 8.33), true);
    }

    private void saveDataAndGoNext() {
        String name = etName.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String state = spinnerState.getText().toString().trim();
        int genderId = rgGender.getCheckedRadioButtonId();

        if (name.isEmpty() || dob.isEmpty() || mobile.isEmpty() || state.isEmpty() || genderId == -1) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        btnNext.setEnabled(false);
        btnNext.setText("Saving...");

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("dob", dob);
        map.put("mobile", mobile);
        map.put("state", state);
        map.put("gender", ((RadioButton)findViewById(genderId)).getText().toString());

        // USE COMPLETE LISTENER FOR GUARANTEED NAVIGATION
        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid).updateChildren(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // EXPLICIT NAVIGATION
                        Intent intent = new Intent(ProfileInfoActivity.this, EmergencyContactActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        btnNext.setEnabled(true);
                        btnNext.setText("NEXT");
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                        Toast.makeText(this, "Database Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}