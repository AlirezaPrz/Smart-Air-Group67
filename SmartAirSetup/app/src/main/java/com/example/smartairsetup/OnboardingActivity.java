package com.example.smartairsetup;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {
    private ImageView onboardingImage;
    private TextView onboardingHeading;
    private TextView onboardingSubtitle;
    private TextView onboardingDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize views for animation
        onboardingImage = findViewById(R.id.onboardingImage);
        onboardingHeading = findViewById(R.id.onboardingHeading);
        onboardingSubtitle = findViewById(R.id.onboardingSubtitle);
        onboardingDescription = findViewById(R.id.onboardingDescription);

        // Load fade-in animation from XML
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Apply animation to all elements in the layout
        onboardingImage.startAnimation(fadeIn);
        onboardingHeading.startAnimation(fadeIn);
        onboardingSubtitle.startAnimation(fadeIn);
        onboardingDescription.startAnimation(fadeIn);

        // Handle Next button click
        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
