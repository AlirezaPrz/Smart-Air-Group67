package com.example.smartairsetup;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

/**
 * This abstract class serves as a true template for all Activities with a bottom navigation menu.
 * It handles view finding and delegates ALL click events to the subclasses, making it role-agnostic.
 */
public abstract class AbstractNavigation extends AppCompatActivity implements View.OnClickListener {

    protected ImageButton homeButton, familyButton, profileButton, settingsButton;
    protected TextView homeText, familyText, profileText, settingsText;

    // --- METHODS TO BE IMPLEMENTED BY SUBCLASSES ---

    @LayoutRes
    protected abstract int getLayoutResourceId();

    // The following methods force each subclass to define its own navigation logic.
    protected abstract void onHomeClicked();
    protected abstract void onFamilyClicked();
    protected abstract void onProfileClicked();
    protected abstract void onSettingsClicked();

    // --- ACTIVITY LIFECYCLE ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        setupViews();
        setupNavigationClickListeners();
    }

    // --- SETUP METHODS ---

    private void setupViews() {
        homeButton = findViewById(R.id.homeButton);
        homeText = findViewById(R.id.homeText);
        familyButton = findViewById(R.id.familyButton);
        familyText = findViewById(R.id.familyText);
        profileButton = findViewById(R.id.profileButton);
        profileText = findViewById(R.id.profileText);
        settingsButton = findViewById(R.id.settingsButton);
        settingsText = findViewById(R.id.settingsText);
    }

    private void setupNavigationClickListeners() {
        homeButton.setOnClickListener(this);
        familyButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
    }

    /**
     * This method is called when any of the navigation buttons are clicked.
     * It now delegates the action to the corresponding abstract method.
     */
    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.homeButton) {
            onHomeClicked();
        } else if (viewId == R.id.familyButton) {
            onFamilyClicked();
        } else if (viewId == R.id.profileButton) {
            onProfileClicked();
        } else if (viewId == R.id.settingsButton) {
            onSettingsClicked();
        }
    }
}
