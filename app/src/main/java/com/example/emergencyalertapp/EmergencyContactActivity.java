package com.example.emergencyalertapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class EmergencyContactActivity extends AppCompatActivity {
    private LinearLayout contactContainer;
    private String DB_URL = "https://emergencyalertapp-95004-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        contactContainer = findViewById(R.id.contactContainer);
        addNewField();

        findViewById(R.id.btnAddField).setOnClickListener(v -> addNewField());

        findViewById(R.id.btnFinish).setOnClickListener(v -> {
            ArrayList<String> numbers = new ArrayList<>();
            for (int i = 0; i < contactContainer.getChildCount(); i++) {
                View row = contactContainer.getChildAt(i);
                TextInputEditText et = row.findViewById(R.id.etEmergencyNumber);
                String n = et.getText().toString().trim();
                if (!n.isEmpty()) numbers.add(n);
            }

            if (numbers.isEmpty()) {
                Toast.makeText(this, "Add at least one number", Toast.LENGTH_SHORT).show();
            } else {
                saveContacts(numbers);
            }
        });
    }

    private void addNewField() {
        View view = getLayoutInflater().inflate(R.layout.item_emergency_contact, null);
        contactContainer.addView(view);
    }

    private void saveContacts(ArrayList<String> contacts) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid).child("emergencyContacts")
                .setValue(contacts)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Setup Finished!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}