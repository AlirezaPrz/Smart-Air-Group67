package com.example.smartairsetup;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button buttonNewUser;
    private Button buttonExistingUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonNewUser = findViewById(R.id.buttonNewUser);
        buttonExistingUser = findViewById(R.id.buttonExistingUser);

        buttonNewUser.setOnClickListener(v -> {
            // For now, I have it so that it just goes straight to SignUp.
            // Zack, you can then insert your onboarding stuff here
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        buttonExistingUser.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}