package com.example.emergencyalertapp;

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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class ProfileInfoActivity extends AppCompatActivity {
    private TextInputEditText etName, etAge, etMobile;
    private RadioGroup rgGender;
    private AutoCompleteTextView spinnerState; // Dropdown
    private SwitchMaterial switchTerms;
    private LinearProgressIndicator progressBar;
    private String DB_URL = "https://emergencyalertapp-a4f91-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etMobile = findViewById(R.id.etMobile);
        rgGender = findViewById(R.id.rgGender);
        spinnerState = findViewById(R.id.spinnerState);
        switchTerms = findViewById(R.id.switchTerms);
        progressBar = findViewById(R.id.setupProgress);

        // 1. Setup State List
        String[] states = {"Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal", "Delhi"};
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, states);
        spinnerState.setAdapter(stateAdapter);

        // 2. Progress Watcher
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { calculateProgress(); }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etName.addTextChangedListener(watcher);
        etAge.addTextChangedListener(watcher);
        etMobile.addTextChangedListener(watcher);

        // Listeners for Selection items
        rgGender.setOnCheckedChangeListener((group, id) -> calculateProgress());
        spinnerState.setOnItemClickListener((parent, view, position, id) -> calculateProgress());
        switchTerms.setOnCheckedChangeListener((button, isChecked) -> calculateProgress());

        findViewById(R.id.btnNextProfile).setOnClickListener(v -> {
            if (!switchTerms.isChecked()) {
                Toast.makeText(this, "Please accept Terms & Conditions", Toast.LENGTH_SHORT).show();
            } else {
                saveToDB();
            }
        });
    }

    private void calculateProgress() {
        int count = 0;
        if (!etName.getText().toString().trim().isEmpty()) count++;
        if (!etAge.getText().toString().trim().isEmpty()) count++;
        if (!etMobile.getText().toString().trim().isEmpty()) count++;
        if (rgGender.getCheckedRadioButtonId() != -1) count++;
        if (!spinnerState.getText().toString().isEmpty()) count++; // Check State
        if (switchTerms.isChecked()) count++;

        // Total 6 items to reach 50% progress.
        // 50 / 6 = ~8.33 per item.
        int progress = (int) (count * 8.33);
        progressBar.setProgress(progress, true);
    }

    private void saveToDB() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String state = spinnerState.getText().toString();
        int selectedId = rgGender.getCheckedRadioButtonId();
        String gender = (selectedId != -1) ? ((RadioButton)findViewById(selectedId)).getText().toString() : "";

        if (name.isEmpty() || age.isEmpty() || mobile.isEmpty() || gender.isEmpty() || state.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("age", age);
        map.put("gender", gender);
        map.put("mobile", mobile);
        map.put("state", state); // Save state to Firebase

        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid)
                .updateChildren(map).addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(ProfileInfoActivity.this, EmergencyContactActivity.class));
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(this, "DB Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}