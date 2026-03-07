package com.example.emergencyalertapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;

public class TimePickerFragment extends Fragment {

    private TextView tvSelectedTime;
    private MaterialButton btnPickTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_time_picker, container, false);

        tvSelectedTime = v.findViewById(R.id.tvSelectedTime);
        btnPickTime = v.findViewById(R.id.btnPickTime);

        btnPickTime.setOnClickListener(view -> openTimePicker());

        return v;
    }

    private void openTimePicker() {
        // Set default time to current system time
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create Time Picker Dialog (24-hour format set to true)
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, selectedMinute) -> {
                    // Format time with leading zeros (e.g., 09:05)
                    String time = String.format("%02d:%02d", hourOfDay, selectedMinute);
                    tvSelectedTime.setText(time);
                }, hour, minute, true);

        timePickerDialog.show();
    }
}