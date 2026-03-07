package com.example.emergencyalertapp;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class TimerFragment extends Fragment {

    private TextInputEditText etSeconds;
    private TextView tvTimer;
    private MaterialButton btnStart, btnReset;
    private CountDownTimer countDownTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timer, container, false);

        etSeconds = v.findViewById(R.id.etSeconds);
        tvTimer = v.findViewById(R.id.tvTimer);
        btnStart = v.findViewById(R.id.btnStart);
        btnReset = v.findViewById(R.id.btnReset);

        btnStart.setOnClickListener(view -> startTimer());
        btnReset.setOnClickListener(view -> resetTimer());

        return v;
    }

    private void startTimer() {
        String input = etSeconds.getText().toString();

        if (input.isEmpty() || input.equals("0")) {
            Toast.makeText(getContext(), "Please enter valid seconds", Toast.LENGTH_SHORT).show();
            return;
        }

        long millisInFuture = Long.parseLong(input) * 1000;

        btnStart.setEnabled(false);
        etSeconds.setEnabled(false);
        btnStart.setAlpha(0.5f);

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("DONE");
                tvTimer.setTextColor(getResources().getColor(R.color.pink_main));
                btnStart.setEnabled(true);
                etSeconds.setEnabled(true);
                btnStart.setAlpha(1.0f);

                playAlarmSound();
                triggerVibration();
                Toast.makeText(getContext(), "Time is up!", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void playAlarmSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (notification == null) {
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            Ringtone r = RingtoneManager.getRingtone(requireContext().getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void triggerVibration() {
        Vibrator v = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(1000);
            }
        }
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        tvTimer.setText("0");
        tvTimer.setTextColor(getResources().getColor(R.color.black));
        etSeconds.setText("");
        btnStart.setEnabled(true);
        etSeconds.setEnabled(true);
        btnStart.setAlpha(1.0f);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}