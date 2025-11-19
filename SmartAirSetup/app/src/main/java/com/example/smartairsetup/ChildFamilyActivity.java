package com.example.smartairsetup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

// 1. Extend from AbstractNavigation instead of AppCompatActivitypublic class ParentHomeActivity extends AbstractNavigation {
public class ChildFamilyActivity extends AbstractNavigation {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_child_family;
    }

    @Override
    protected void onHomeClicked() {
        Intent intent = new Intent(this, ChildHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    protected void onFamilyClicked() {
        //Do nothing as we are on Family Page
    }

    @Override
    protected void onProfileClicked() {
        // TODO: For a Parent, this would go to ParentProfileActivity
        // Intent intent = new Intent(this, ParentProfileActivity.class);
        // startActivity(intent);
    }

    @Override
    protected void onSettingsClicked() {
        // TODO: For a Parent, this would go to ParentSettingsActivity
        // Intent intent = new Intent(this, ParentSettingsActivity.class);
        // startActivity(intent);
    }
}
