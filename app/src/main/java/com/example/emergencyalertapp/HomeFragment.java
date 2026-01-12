package com.example.emergencyalertapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private int tapCount = 0;
    private ArrayList<String> contacts = new ArrayList<>();
    private TextView tvUser;
    private String DB_URL = "https://emergencyalertapp-a4f91-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvUser = v.findViewById(R.id.tvUser);

        // 1. Load User Name and Contacts from Firebase
        loadData();

        // 2. SOS Button Tap Logic
        v.findViewById(R.id.sosButton).setOnClickListener(view -> {
            tapCount++;

            // Wait for 1 second to see if the user taps again
            new Handler().postDelayed(() -> {
                if (tapCount == 1) {
                    // Normal Emergency: SMS Only
                    triggerAlert(false);
                } else if (tapCount >= 3) {
                    // High Emergency: SMS + Call
                    triggerAlert(true);
                }
                tapCount = 0; // Reset count after processing
            }, 1000);
        });

        // 3. Logout Button Logic
        v.findViewById(R.id.btnLogout).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return v;
    }

    private void triggerAlert(boolean isHighEmergency) {
        if (contacts.isEmpty()) {
            Toast.makeText(getContext(), "No emergency contacts found! Please add them in the Contacts tab.", Toast.LENGTH_LONG).show();
            return;
        }

        // SMS Permissions check (Safety)
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "SMS Permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "EMERGENCY! I need help. Please contact me immediately!";
        SmsManager smsManager = SmsManager.getDefault();

        // Send SMS to all contacts
        for (String number : contacts) {
            try {
                smsManager.sendTextMessage(number, null, message, null, null);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to send SMS to " + number, Toast.LENGTH_SHORT).show();
            }
        }

        // If High Emergency (3+ taps), make a call to the first contact
        if (isHighEmergency) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + contacts.get(0)));
                startActivity(callIntent);
            } else {
                Toast.makeText(getContext(), "Call Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }

        // Save this event to the History Database
        saveToHistory(isHighEmergency ? "SMS + CALL" : "SMS Only");

        String resultMsg = isHighEmergency ? "High Emergency Alert Sent!" : "Emergency SMS Sent!";
        Toast.makeText(getContext(), resultMsg, Toast.LENGTH_LONG).show();
    }

    private void saveToHistory(String alertType) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get current system time formatted nicely
        String currentTime = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());

        HashMap<String, String> historyLog = new HashMap<>();
        historyLog.put("type", alertType);
        historyLog.put("time", currentTime);

        // Save to Users -> UID -> history
        FirebaseDatabase.getInstance(DB_URL)
                .getReference("Users").child(uid).child("history")
                .push() // Creates a unique ID for each log
                .setValue(historyLog);
    }

    private void loadData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Add Listener to fetch User data from your Asia Firebase Server
        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Update the "Hi, [Name]" text
                            if (snapshot.hasChild("name")) {
                                tvUser.setText("Hi, " + snapshot.child("name").getValue().toString());
                            }

                            // Load the contacts into our list for the SOS function
                            contacts.clear();
                            DataSnapshot contactSnap = snapshot.child("emergencyContacts");
                            for (DataSnapshot ds : contactSnap.getChildren()) {
                                String phone = ds.getValue(String.class);
                                if (phone != null) contacts.add(phone);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if(getContext() != null)
                            Toast.makeText(getContext(), "Error loading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}