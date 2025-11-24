package com.example.smartairsetup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ChildBadgesActivity extends AppCompatActivity {

    private TextView textBackBadges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_badge);

        textBackBadges = findViewById(R.id.textBackBadges);

        textBackBadges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =
                        new Intent(ChildBadgesActivity.this, ChildHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
