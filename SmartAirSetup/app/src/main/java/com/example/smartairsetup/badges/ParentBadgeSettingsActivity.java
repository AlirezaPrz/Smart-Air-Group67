package com.example.smartairsetup.badges;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartairsetup.R;
import com.example.smartairsetup.parent_home_ui.ParentHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParentBadgeSettingsActivity extends AppCompatActivity {

    private Spinner spinnerChildren;
    private Button backButton;
    private EditText editTechniqueSessions;
    private EditText editLowRescueDays;
    private Button buttonSaveBadgeSettings;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayAdapter<String> childrenAdapter;
    private final List<String> childNames = new ArrayList<>();
    private final List<String> childIDs = new ArrayList<>();

    private String selectedChildID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_badge_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        spinnerChildren = findViewById(R.id.spinnerChildren);
        backButton = findViewById(R.id.backButton);
        editTechniqueSessions = findViewById(R.id.editTechniqueSessions);
        editLowRescueDays = findViewById(R.id.editLowRescueDays);
        buttonSaveBadgeSettings = findViewById(R.id.buttonSaveBadgeSettings);

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(ParentBadgeSettingsActivity.this, ParentHomeActivity.class);
            startActivity(intent);
            finish();
        });

        childrenAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                childNames
        );
        childrenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChildren.setAdapter(childrenAdapter);

        spinnerChildren.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parentView,
                    View view,
                    int index,
                    long id
            ) {
                if((index >= 0 )&& (index < childIDs.size())){
                    selectedChildID = childIDs.get(index);
                    loadBadgeSettings(selectedChildID);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedChildID = null;
            }
        });
        buttonSaveBadgeSettings.setOnClickListener(view -> saveBadges());

        loadChildren();
    }

    private void loadChildren() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be signed in as a parent.", Toast.LENGTH_SHORT).show();
            spinnerChildren.setEnabled(false);
            return;
        }

        String parentUid = currentUser.getUid();

        CollectionReference childrenRef = db.collection("users")
                .document(parentUid)
                .collection("children");

        childrenRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    childNames.clear();
                    childIDs.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");
                        if (name == null || name.trim().isEmpty()) {
                            name = "(Unnamed child)";
                        }
                        childNames.add(name);
                        childIDs.add(doc.getId());
                    }

                    childrenAdapter.notifyDataSetChanged();

                    if (childIDs.isEmpty()) {
                        spinnerChildren.setEnabled(false);
                        Toast.makeText(
                                ParentBadgeSettingsActivity.this,
                                "No children found for this parent.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    else {
                        spinnerChildren.setEnabled(true);
                        selectedChildID = childIDs.get(0);
                        spinnerChildren.setSelection(0);
                        loadBadgeSettings(selectedChildID);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            ParentBadgeSettingsActivity.this,
                            "Failed to load children: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                    spinnerChildren.setEnabled(false);
                });
    }

    private void loadBadgeSettings(String childID) {
        if (TextUtils.isEmpty(childID)) {
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be signed in as a parent.", Toast.LENGTH_SHORT).show();
            return;
        }

        String parentUid = currentUser.getUid();

        CollectionReference badgesRef = db.collection("users")
                .document(parentUid)
                .collection("children")
                .document(childID)
                .collection("badges");

        badgesRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(
                        ParentBadgeSettingsActivity.this,
                        "Failed to load badge goals.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            int techniqueTarget = 10;
            int lowRescueTarget = 4;

            for (DocumentSnapshot doc : task.getResult()) {
                String badgeID = doc.getId();
                Long targetLong = doc.getLong("target");

                if (targetLong == null) {
                    continue;
                }

                int target = targetLong.intValue();

                if ("technique_sessions".equals(badgeID)) {
                    techniqueTarget = target;
                }
                else if ("low_rescue_month".equals(badgeID)) {
                    lowRescueTarget = target;
                }
            }

            editTechniqueSessions.setText(String.valueOf(techniqueTarget));
            editLowRescueDays.setText(String.valueOf(lowRescueTarget));
        });
    }

    private void saveBadges() {
        String techniqueText = editTechniqueSessions.getText().toString().trim();
        String lowRescueText = editLowRescueDays.getText().toString().trim();

        if (TextUtils.isEmpty(techniqueText)) {
            editTechniqueSessions.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(lowRescueText)) {
            editLowRescueDays.setError("Required");
            return;
        }

        int techniqueTarget;
        int lowRescueTarget;

        try {
            techniqueTarget = Integer.parseInt(techniqueText);
            lowRescueTarget = Integer.parseInt(lowRescueText);
        }
        catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((techniqueTarget<= 0) || (lowRescueTarget < 0)) {
            Toast.makeText(
                    this,
                    "Values must be bigger than 0",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be signed in as a parent.", Toast.LENGTH_SHORT).show();
            return;
        }

        String parentUid = currentUser.getUid();

        CollectionReference badgesData = db.collection("users")
                .document(parentUid)
                .collection("children")
                .document(selectedChildID)
                .collection("badges");

        Map<String, Object> techniqueData = new HashMap<>();
        Map<String, Object> lowRescueData = new HashMap<>();

        techniqueData.put("target", techniqueTarget);
        lowRescueData.put("target", lowRescueTarget);

        badgesData.document("technique_sessions").set(techniqueData);
        badgesData.document("low_rescue_month").set(lowRescueData)
                .addOnSuccessListener(unused -> Toast.makeText(
                                ParentBadgeSettingsActivity.this,
                                "Badge goals saved for this child.",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e -> Toast.makeText(
                                ParentBadgeSettingsActivity.this,
                                "Failed to save badge goals.",
                                Toast.LENGTH_SHORT).show()
                );
    }
}
