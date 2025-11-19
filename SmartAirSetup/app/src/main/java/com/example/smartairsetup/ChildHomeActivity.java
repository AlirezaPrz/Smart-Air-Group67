
package com.example.smartairsetup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

// 1. Extend from AbstractNavigation instead of AppCompat Activity public class ParentHomeActivity extends AbstractNavigation {
public class ChildHomeActivity extends AbstractNavigation {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void setupShortcutClicks() {
        // go to Calculate PEF
        View.OnClickListener calculateListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ParentHomeActivity.this, CalculatePefActivity.class);
                //startActivity(intent);

            }
        };

        // go to  Set Personal Best
        View.OnClickListener setPbListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(ParentHomeActivity.this, SetPersonalBestActivity.class);
                //startActivity(intent);
            }
        };
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_child_home;
    }

    @Override
    protected void onHomeClicked() {
        //Do nothing as we are on Home Page
    }

    @Override
    protected void onFamilyClicked() {
        Intent intent = new Intent(this, ChildFamilyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
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
