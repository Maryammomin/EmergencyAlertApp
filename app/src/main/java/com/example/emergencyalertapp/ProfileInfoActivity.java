package com.example.emergencyalertapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class ProfileInfoActivity extends AppCompatActivity {
    private TextInputEditText etName, etAge, etMobile;
    private RadioGroup rgGender;
    private String DB_URL = "https://emergencyalertapp-a4f91-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etMobile = findViewById(R.id.etMobile);
        rgGender = findViewById(R.id.rgGender);

        findViewById(R.id.btnNextProfile).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            int selectedId = rgGender.getCheckedRadioButtonId();
            String gender = (selectedId != -1) ? ((RadioButton)findViewById(selectedId)).getText().toString() : "";

            if (name.isEmpty() || age.isEmpty() || mobile.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                saveToDB(name, age, mobile, gender);
            }
        });
    }

    private void saveToDB(String n, String a, String m, String g) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", n);
        map.put("age", a);
        map.put("mobile", m);
        map.put("gender", g);

        FirebaseDatabase.getInstance("https://emergencyalertapp-a4f91-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users").child(uid)
                .updateChildren(map)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(ProfileInfoActivity.this, EmergencyContactActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}