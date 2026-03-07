package com.example.emergencyalertapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignupActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPass;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etRegEmail);
        etPass = findViewById(R.id.etRegPass);

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            if (email.isEmpty() || pass.length() < 6) {
                Toast.makeText(this, "Min 6 characters required", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    startActivity(new Intent(this, ProfileInfoActivity.class));
                    finish();
                } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(this, "User already exists. Use Login.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Link to Login Page
        findViewById(R.id.tvLoginLink).setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }
}