package com.example.emergencyalertapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private ArrayList<String> contactNumbers = new ArrayList<>();
    private TextView tvUser;
    private final String DB_URL = "https://emergencyalertapp-a4f91-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tvUser = v.findViewById(R.id.tvUser);
        View sosButton = v.findViewById(R.id.sosButton);

        loadData();

        sosButton.setOnClickListener(view -> {
            tapCount++;
            vibrateDevice(50); // Feedback for tap

            new Handler().postDelayed(() -> {
                if (tapCount == 1) {
                    vibrateDevice(200);
                    triggerEmergency(false);
                } else if (tapCount >= 3) {
                    vibrateHighEmergency();
                    triggerEmergency(true);
                }
                tapCount = 0;
            }, 1000);
        });

        v.findViewById(R.id.btnLogout).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return v;
    }

    private void triggerEmergency(boolean isHighPriority) {
        // SAFETY 1: Check if contacts are actually loaded
        if (contactNumbers == null || contactNumbers.isEmpty()) {
            Toast.makeText(getContext(), "No contacts found! Add them in Contacts tab.", Toast.LENGTH_SHORT).show();
            return;
        }

        // SAFETY 2: Check Permissions again to prevent SecurityException crash
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permission denied: Cannot send SMS", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Use modern SmsManager retrieval
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = requireContext().getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }

            String message = "EMERGENCY! I need help. Please contact me immediately!";
            StringBuilder recipientNames = new StringBuilder();

            for (String num : contactNumbers) {
                if (num != null && !num.isEmpty()) {
                    smsManager.sendTextMessage(num, null, message, null, null);
                    String name = getContactName(num);
                    recipientNames.append(name != null ? name : num).append(", ");
                }
            }

            String finalRecipients = recipientNames.toString().trim().replaceAll(", $", "");

            if (isHighPriority) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + contactNumbers.get(0)));
                    startActivity(callIntent);
                }
            }

            saveToHistory(isHighPriority ? "SMS + CALL" : "SMS Only", finalRecipients);
            Toast.makeText(getContext(), "SOS Sent!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            // SAFETY 3: Catch any unexpected error to stop the app from closing
            Toast.makeText(getContext(), "SOS Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void vibrateDevice(int duration) {
        if (getContext() == null) return;
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else { v.vibrate(duration); }
        }
    }

    private void vibrateHighEmergency() {
        if (getContext() == null) return;
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            long[] pattern = {0, 400, 200, 400};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else { v.vibrate(pattern, -1); }
        }
    }

    private String getContactName(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty() || getContext() == null) return null;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        try (Cursor cursor = requireContext().getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getString(0);
        } catch (Exception e) { return null; }
        return null;
    }

    private void saveToHistory(String type, String recipients) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String time = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());
        HashMap<String, String> log = new HashMap<>();
        log.put("type", type);
        log.put("time", time);
        log.put("recipients", recipients);
        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(user.getUid()).child("history").push().setValue(log);
    }

    private void loadData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseDatabase.getInstance(DB_URL).getReference("Users").child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot s) {
                        if (s.exists() && isAdded()) {
                            Object nameObj = s.child("name").getValue();
                            if (nameObj != null) tvUser.setText("Hi, " + nameObj.toString());

                            contactNumbers.clear();
                            DataSnapshot contactsSnap = s.child("emergencyContacts");
                            for (DataSnapshot ds : contactsSnap.getChildren()) {
                                String num;
                                if (ds.getValue() instanceof Map) {
                                    num = String.valueOf(((Map) ds.getValue()).get("number"));
                                } else {
                                    num = String.valueOf(ds.getValue());
                                }
                                if (num != null && !num.equals("null")) contactNumbers.add(num);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}