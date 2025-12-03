package com.example.smartairsetup.parent_home_ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.smartairsetup.navigation.AbstractNavigation;
import com.example.smartairsetup.login.AddChildActivity;
import com.example.smartairsetup.R;
import com.example.smartairsetup.child_home_ui.ChildHomeActivity;
import com.example.smartairsetup.onboarding.OnboardingActivity;
import com.example.smartairsetup.triage.RedFlagsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ParentFamilyActivity extends AbstractNavigation {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LinearLayout familyListContainer;
    private View currentConfirmationView;
    private View currentSelectedRow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        familyListContainer = findViewById(R.id.familyListContainer);
        Button addMemberButton = findViewById(R.id.addMemberButton);

        addMemberButton.setOnClickListener(view -> {
            Intent intent = new Intent(ParentFamilyActivity.this, AddChildActivity.class);
            startActivity(intent);
        });

        loadChildrenFromFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildrenFromFirestore();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_parent_family;
    }

    private void loadChildrenFromFirestore() {
        if(mAuth.getCurrentUser()==null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String parentUid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(parentUid)
                .collection("children")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    familyListContainer.removeAllViews();
                    currentConfirmationView = null;
                    currentSelectedRow = null;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String childId = doc.getId();
                        String name = doc.getString("name");
                        addFamilyRow(childId, name);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load children.", Toast.LENGTH_SHORT).show();
                });
    }

    private void addFamilyRow(String childId, String name) {
        View rowView = getLayoutInflater().inflate(
                R.layout.item_family_member,
                familyListContainer,
                false
        );

        TextView nameText=rowView.findViewById(R.id.textName);
        TextView roleText=rowView.findViewById(R.id.textRole);

        nameText.setText(name);
        roleText.setText("Child");

        rowView.setOnClickListener(view -> showConfirmationForChild(childId, name, rowView));

        familyListContainer.addView(rowView);
    }

    private void showConfirmationForChild(String childId, String name, View rowView) {
        if (currentSelectedRow != null && currentSelectedRow != rowView) {
            currentSelectedRow.setBackgroundColor(Color.TRANSPARENT);
        }

        rowView.setBackgroundColor(Color.parseColor("#DDDDDD"));
        currentSelectedRow = rowView;

        if (currentConfirmationView != null) {
            ViewGroup parent = (ViewGroup) currentConfirmationView.getParent();
            if (parent != null){
                parent.removeView(currentConfirmationView);
            }
            currentConfirmationView = null;
        }

        LinearLayout confirmLayout = new LinearLayout(this);
        confirmLayout.setOrientation(LinearLayout.VERTICAL);
        confirmLayout.setBackgroundColor(Color.parseColor("#EEF5FF"));

        float density = getResources().getDisplayMetrics().density;
        int paddingHorizontal = (int)(16*density+0.5f);
        int paddingVerticalTop = (int)(8*density + 0.5f);
        int paddingVerticalBottom = (int)(16 * density + 0.5f);

        confirmLayout.setPadding(paddingHorizontal, paddingVerticalTop, paddingHorizontal, paddingVerticalBottom);

        TextView message = new TextView(this);
        message.setText("You will be redirected to " + name);
        message.setTextSize(14);

        Button goButton = new Button(this);
        goButton.setText("Go to child page");

        confirmLayout.addView(message);
        confirmLayout.addView(goButton);

        int rowIndex = familyListContainer.indexOfChild(rowView);
        if (rowIndex == -1) {
            familyListContainer.addView(confirmLayout);
        }
        else{
            familyListContainer.addView(confirmLayout, rowIndex + 1);
        }

        currentConfirmationView = confirmLayout;

        goButton.setOnClickListener(view -> {
            String parentUid;

            if (mAuth.getCurrentUser() != null) {
                parentUid = mAuth.getCurrentUser().getUid();
            }
            else {
                parentUid = null;
            }

            if (parentUid == null) {
                Toast.makeText(this, "Parent UID is not available.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("childAccounts")
                    .whereEqualTo("childDocId", childId)
                    .whereEqualTo("parentUid", parentUid)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            boolean firstTimeFound = false;
                            for (DocumentSnapshot doc:querySnapshot) {
                                Boolean firstTime = doc.getBoolean("firstTime");
                                if (firstTime != null && firstTime) {
                                    firstTimeFound = true;
                                    doc.getReference().update("firstTime", false)
                                            .addOnFailureListener(e -> Log.e("ParentFamilyActivity", "Failed to update firstTime", e));
                                    Intent intent = new Intent(ParentFamilyActivity.this, OnboardingActivity.class);
                                    intent.putExtra("PARENT_UID", parentUid);
                                    intent.putExtra("CHILD_ID", childId);
                                    intent.putExtra("firstTime", true);
                                    startActivity(intent);
                                    finish();
                                    return;
                                }
                            }
                            Intent intent = new Intent(ParentFamilyActivity.this, ChildHomeActivity.class);
                            intent.putExtra("CHILD_ID", childId);
                            intent.putExtra("CHILD_NAME", name);
                            startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(ParentFamilyActivity.this, ChildHomeActivity.class);
                            intent.putExtra("CHILD_ID", childId);
                            intent.putExtra("CHILD_NAME", name);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error checking child account.", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    protected void onHomeClicked() {
        startActivity(new Intent(this, ParentHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    @Override
    protected void onFamilyClicked() {
        // Already on Family screen
    }

    @Override
    protected void onEmergencyClicked() {
        Intent intent = new Intent(ParentFamilyActivity.this, RedFlagsActivity.class);
        String parentUid = null;
        if (mAuth.getCurrentUser() != null) {
            parentUid = mAuth.getCurrentUser().getUid();
        }
        intent.putExtra("PARENT_UID", parentUid);
        startActivity(intent);
    }

    @Override
    protected void onSettingsClicked() {
        startActivity(new Intent(this, ParentSettingsActivity.class));
    }
}
