package com.example.smartairsetup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText signupEmail;
    private EditText signupPassword;
    private RadioGroup radioGroupRole;
    private Button buttonSignup;
    private TextView signupError;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signupEmail = findViewById(R.id.signupEmail);
        signupPassword = findViewById(R.id.signupPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        buttonSignup = findViewById(R.id.buttonSignup);
        signupError = findViewById(R.id.signupError);

        buttonSignup.setOnClickListener(v -> handleSignup());
    }

    private void handleSignup() {
        String email = signupEmail.getText().toString().trim();
        String password = signupPassword.getText().toString();
        String role = getSelectedRole();

        // basic validation (similar to login)
        if (TextUtils.isEmpty(email)) {
            signupEmail.setError("Email is required");
            signupEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.setError("Enter a valid email");
            signupEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            signupPassword.setError("Password must be at least 6 characters");
            signupPassword.requestFocus();
            return;
        }
        if (role == null) {
            signupError.setText("Select a role");
            signupError.setVisibility(View.VISIBLE);
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        saveUserRole(uid, role);
                    } else {
                        String message = "Signup failed";
                        if (task.getException() != null) {
                            message = task.getException().getMessage();
                        }
                        signupError.setText(message);
                        signupError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private String getSelectedRole() {
        int checkedId = radioGroupRole.getCheckedRadioButtonId();
        if (checkedId == R.id.radioChild) return "child";
        if (checkedId == R.id.radioParent) return "parent";
        if (checkedId == R.id.radioProvider) return "provider";
        return null;
    }

    private void saveUserRole(String uid, String role) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("role", role);

        db.collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener(unused -> {
                    // after signup, send them to their specific page
                    goToRoleHome(role);
                })
                .addOnFailureListener(e -> {
                    signupError.setText("Failed to save role: " + e.getMessage());
                    signupError.setVisibility(View.VISIBLE);
                });
    }

    private void goToRoleHome(String role) {
        // TODO: replace these with real Activities for when Rohat creates them
        Intent intent = new Intent(this, LoginActivity.class);
        /*
        switch (role) {
            case "child":
                intent = new Intent(this, ChildHomeActivity.class);
                break;
            case "parent":
                intent = new Intent(this, ParentHomeActivity.class);
                break;
            case "provider":
                intent = new Intent(this, ProviderHomeActivity.class);
                break;
            default:
                intent = new Intent(this, LoginActivity.class);
        }*/
        startActivity(intent);
        finish();
    }
}
