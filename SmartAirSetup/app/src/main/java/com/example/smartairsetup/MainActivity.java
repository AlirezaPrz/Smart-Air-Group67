package com.example.smartairsetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private TextView textViewForgotPassword;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonSignIn;
    private TextView textViewError;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        textViewError = findViewById(R.id.textViewError);

        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePasswordReset();
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignIn();
            }
        });
    }

    private void handleSignIn() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();

        // Basic validation
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        // Call Firebase
        signInWithFirebase(email, password);
    }

    private void signInWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success
                            textViewError.setVisibility(View.GONE);
                            goToDashboard();
                        } else {
                            // Sign in failed
                            String message = "Authentication failed";
                            if (task.getException() != null) {
                                message = task.getException().getMessage();
                            }
                            textViewError.setText(message);
                            textViewError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void handlePasswordReset() {
        String email = editTextEmail.getText().toString().trim();

        // Require a valid email
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Enter your email to reset password");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(MainActivity.this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                MainActivity.this,
                                "Password reset email sent. Check your inbox.",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        String message = "Failed to send reset email";
                        if (task.getException() != null) {
                            message = task.getException().getMessage();
                        }
                        textViewError.setText(message);
                        textViewError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void goToDashboard() {
        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

        // TODO: we have to DashboardActivity with the real next screen
        // Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        // startActivity(intent);
        // finish();
    }
}