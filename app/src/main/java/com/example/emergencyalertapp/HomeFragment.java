package com.example.emergencyalertapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.util.Map;

public class HomeFragment extends Fragment {

    private int tapCount = 0;
    private final Handler tapHandler = new Handler();
    private ArrayList<String> contactNumbers = new ArrayList<>();
    private TextView tvUser;
    private FusedLocationProviderClient fusedLocationClient;

    // Your specific Asia-Southeast URL
    private final String DB_URL = "https://emergencyalertapp-95004-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the high-end UI layout
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvUser = v.findViewById(R.id.tvUser);
        View sosButton = v.findViewById(R.id.sosButton);
        View btnShareLocation = v.findViewById(R.id.btnShareLocation);

        // 1. Initialize Location Services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 2. Start the Pulse Animation on the SOS Button
        Animation pulse = AnimationUtils.loadAnimation(getContext(), R.anim.pulse);
        sosButton.startAnimation(pulse);

        // 3. Load User Profile and Contacts
        loadData();

        // 4. SOS Button Logic (1 tap = SMS + Loc | 3 taps = SMS + Loc + Call)
        sosButton.setOnClickListener(view -> {
            tapCount++;
            vibrateDevice(50); // Small vibration on every touch

            tapHandler.removeCallbacksAndMessages(null);
            tapHandler.postDelayed(() -> {
                if (tapCount == 1) {
                    fetchLocationAndTriggerSOS(false); // Normal Emergency
                } else if (tapCount >= 3) {
                    fetchLocationAndTriggerSOS(true);  // High Emergency
                }
                tapCount = 0;
            }, 1000); // 1-second window to detect multiple taps
        });

        // 5. Share Location Button Logic (Normal sharing)
        btnShareLocation.setOnClickListener(view -> {
            vibrateDevice(100);
            fetchLocationAndTriggerSOS(false);
        });

        // 6. Logout Logic
        v.findViewById(R.id.btnLogout).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return v;
    }

    private void loadData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot s) {
                        if (s.exists()) {
                            // Set Greeting Name
                            if (s.child("name").exists()) {
                                tvUser.setText("Hi, " + s.child("name").getValue().toString());
                            }

                            // Clear and reload contact numbers
                            contactNumbers.clear();
                            DataSnapshot contactsSnap = s.child("emergencyContacts");
                            for (DataSnapshot ds : contactsSnap.getChildren()) {
                                if (ds.getValue() instanceof Map) {
                                    contactNumbers.add(String.valueOf(((Map)ds.getValue()).get("number")));
                                } else {
                                    contactNumbers.add(ds.getValue(String.class));
                                }
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void fetchLocationAndTriggerSOS(boolean isHigh) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location Permission Required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            String locationLink = "Location Unavailable";
            if (location != null) {
                locationLink = "My Current Location: https://www.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
            }
            executeSOS(isHigh, locationLink);
        });
    }

    private void executeSOS(boolean isHigh, String locationText) {
        if (contactNumbers.isEmpty()) {
            Toast.makeText(getContext(), "Add contacts first!", Toast.LENGTH_SHORT).show();
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        String msg = "EMERGENCY! I need help immediately! " + locationText;
        StringBuilder recipientNames = new StringBuilder();

        // 1. Send SMS to all contacts
        for (String num : contactNumbers) {
            try {
                smsManager.sendTextMessage(num, null, msg, null, null);

                // Get name for History log
                String name = getContactName(num);
                recipientNames.append(name != null ? name : num).append(", ");
            } catch (Exception e) { e.printStackTrace(); }
        }

        String finalRecipients = recipientNames.toString().trim().replaceAll(", $", "");

        // 2. High Emergency: Pulse Vibrate and CALL first contact
        if (isHigh) {
            vibrateHighEmergency();
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + contactNumbers.get(0)));
                startActivity(callIntent);
            }
        } else {
            vibrateDevice(200); // Confirmation vibrate for SMS
        }

        // 3. Log to History DB
        saveToHistory(isHigh ? "SMS + CALL" : "SMS Only", finalRecipients);
        Toast.makeText(getContext(), "Emergency Alert Sent!", Toast.LENGTH_SHORT).show();
    }

    private String getContactName(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return null;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        try (Cursor cursor = requireContext().getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getString(0);
        } catch (Exception e) { return null; }
        return null;
    }

    private void saveToHistory(String type, String recipients) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String time = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());
        HashMap<String, String> log = new HashMap<>();
        log.put("type", type);
        log.put("time", time);
        log.put("recipients", recipients);
        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(uid).child("history").push().setValue(log);
    }

    private void vibrateDevice(int duration) {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else { v.vibrate(duration); }
        }
    }

    private void vibrateHighEmergency() {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            long[] pattern = {0, 400, 200, 400};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else { v.vibrate(pattern, -1); }
        }
    }
}