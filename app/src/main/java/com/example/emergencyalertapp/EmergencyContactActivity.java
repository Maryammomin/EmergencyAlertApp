package com.example.emergencyalertapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EmergencyContactActivity extends AppCompatActivity {

    private LinearLayout contactContainer;
    private DatabaseReference mRef;
    private final String DB_URL = "https://emergencyalertapp-95004-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        contactContainer = findViewById(R.id.contactContainer);
        String uid = FirebaseAuth.getInstance().getUid();
        mRef = FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid).child("emergencyContacts");

        // Load existing contacts so we don't overwrite them
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactContainer.removeAllViews();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String number;
                        if (ds.getValue() instanceof Map) {
                            number = String.valueOf(((Map) ds.getValue()).get("number"));
                        } else {
                            number = ds.getValue(String.class);
                        }
                        addNewField(number);
                    }
                }
                addNewField(""); // Always show one blank box
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        findViewById(R.id.btnAddField).setOnClickListener(v -> addNewField(""));
        findViewById(R.id.btnFinish).setOnClickListener(v -> saveToFirebaseAndFinish());
    }

    private void addNewField(String phone) {
        View v = getLayoutInflater().inflate(R.layout.item_emergency_contact, null);
        TextInputEditText etPhone = v.findViewById(R.id.etEmergencyNumber);
        etPhone.setText(phone);
        contactContainer.addView(v);
    }

    private void saveToFirebaseAndFinish() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        for (int i = 0; i < contactContainer.getChildCount(); i++) {
            View v = contactContainer.getChildAt(i);
            TextInputEditText etP = v.findViewById(R.id.etEmergencyNumber);
            String p = etP.getText().toString().trim();

            if(!p.isEmpty()){
                String name = getContactNameFromPhone(p);
                HashMap<String, String> map = new HashMap<>();
                map.put("name", name != null ? name : "Saved Contact");
                map.put("number", p);
                list.add(map);
            }
        }

        if (list.isEmpty()) {
            Toast.makeText(this, "Please add at least one contact", Toast.LENGTH_SHORT).show();
            return;
        }

        mRef.setValue(list).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Setup Complete!", Toast.LENGTH_SHORT).show();

            // --- THE FIX: CLEAR THE STACK ---
            Intent intent = new Intent(EmergencyContactActivity.this, MainActivity.class);

            // These flags prevent the app from going back to the Welcome/Signup pages
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish(); // Closes the EmergencyContactActivity
        });
    }

    private String getContactNameFromPhone(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        try (Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getString(0);
        } catch (Exception e) { return null; }
        return null;
    }
}