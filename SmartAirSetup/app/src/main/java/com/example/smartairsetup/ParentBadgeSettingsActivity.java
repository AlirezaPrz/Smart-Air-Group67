package com.example.smartairsetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParentBadgeSettingsActivity extends AppCompatActivity {

    // UI
    private Spinner spinnerChildren;
    private Button backButton;
    private EditText editPerfectControllerDays;
    private EditText editTechniqueSessions;
    private EditText editLowRescueDays;
    private Button buttonSaveBadgeSettings;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Spinner data
    private ArrayAdapter<String> childrenAdapter;
    private List<String> childNames = new ArrayList<String>();
    private List<String> childIds = new ArrayList<String>();

    private String selectedChildId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_badge_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views
        spinnerChildren = findViewById(R.id.spinnerChildren);
        backButton = findViewById(R.id.backButton);
        editPerfectControllerDays = findViewById(R.id.editPerfectControllerDays);
        editTechniqueSessions = findViewById(R.id.editTechniqueSessions);
        editLowRescueDays = findViewById(R.id.editLowRescueDays);
        buttonSaveBadgeSettings = findViewById(R.id.buttonSaveBadgeSettings);

        setupBackButton();
        setupChildrenSpinner();
        setupSaveButton();

        loadChildrenForParent();
    }

    // ------------------------------
    // UI Setup
    // ------------------------------

    private void setupBackButton() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        ParentBadgeSettingsActivity.this,
                        ParentHomeActivity.class  // Kendi parent home activity'nin adı neyse bunu değiştir
                );
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupChildrenSpinner() {
        childrenAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                childNames
        );
        childrenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChildren.setAdapter(childrenAdapter);

        spinnerChildren.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                if (position >= 0 && position < childIds.size()) {
                    selectedChildId = childIds.get(position);
                    loadBadgeSettingsForChild(selectedChildId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedChildId = null;
            }
        });
    }

    private void setupSaveButton() {
        buttonSaveBadgeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBadgeSettingsForSelectedChild();
            }
        });
    }

    // ------------------------------
    // Firestore: Children
    // ------------------------------

    private void loadChildrenForParent() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be signed in as a parent.", Toast.LENGTH_SHORT).show();
            return;
        }

        String parentUid = currentUser.getUid();

        /*
         * VARSAYIM:
         * - Tüm kullanıcılar "users" koleksiyonunda.
         * - Çocuk dokümanlarında: "role" = "child", "parentId" = parentUid.
         * Eğer senin yapın farklıysa, buradaki sorguyu kendi path'ine göre değiştir:
         *   Örneğin: db.collection("users").document(parentUid).collection("children")...
         */
        db.collection("users")
                .whereEqualTo("role", "child")
                .whereEqualTo("parentId", parentUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(ParentBadgeSettingsActivity.this,
                                    "Failed to load children.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        childIds.clear();
                        childNames.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String childId = doc.getId();
                            String name = doc.getString("name");

                            if (name == null || name.isEmpty()) {
                                name = childId;
                            }

                            childIds.add(childId);
                            childNames.add(name);
                        }

                        childrenAdapter.notifyDataSetChanged();

                        if (childIds.isEmpty()) {
                            Toast.makeText(ParentBadgeSettingsActivity.this,
                                    "No children found for this parent.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Otomatik ilk çocuğu seç
                            selectedChildId = childIds.get(0);
                            spinnerChildren.setSelection(0);
                            loadBadgeSettingsForChild(selectedChildId);
                        }
                    }
                });
    }

    // ------------------------------
    // Firestore: Badge settings per child
    // ------------------------------

    private void loadBadgeSettingsForChild(String childId) {
        if (TextUtils.isEmpty(childId)) {
            return;
        }

        /*
         * VARSAYIM:
         * - Badge hedefleri her child'ın altında:
         *   users/{childId}/badges/{badgeId}
         *   badgeId:
         *      "perfect_controller_week"
         *      "technique_sessions"
         *      "low_rescue_month"
         *   Her dokümanda: "target" (Number)
         */

        CollectionReference badgesRef = db.collection("users")
                .document(childId)
                .collection("badges");

        badgesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(ParentBadgeSettingsActivity.this,
                            "Failed to load badge goals.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Varsayılan değerler (hiç doküman yoksa)
                int perfectDays = 7;
                int techniqueTarget = 10;
                int lowRescueThreshold = 4;

                for (DocumentSnapshot doc : task.getResult()) {
                    String badgeId = doc.getId();
                    Long targetLong = doc.getLong("target");

                    if (targetLong == null) {
                        continue;
                    }

                    int target = targetLong.intValue();

                    if ("perfect_controller_week".equals(badgeId)) {
                        perfectDays = target;
                    } else if ("technique_sessions".equals(badgeId)) {
                        techniqueTarget = target;
                    } else if ("low_rescue_month".equals(badgeId)) {
                        lowRescueThreshold = target;
                    }
                }

                editPerfectControllerDays.setText(String.valueOf(perfectDays));
                editTechniqueSessions.setText(String.valueOf(techniqueTarget));
                editLowRescueDays.setText(String.valueOf(lowRescueThreshold));
            }
        });
    }

    private void saveBadgeSettingsForSelectedChild() {
        if (TextUtils.isEmpty(selectedChildId)) {
            Toast.makeText(this, "Please select a child.", Toast.LENGTH_SHORT).show();
            return;
        }

        String perfectDaysText = editPerfectControllerDays.getText().toString().trim();
        String techniqueText = editTechniqueSessions.getText().toString().trim();
        String lowRescueText = editLowRescueDays.getText().toString().trim();

        if (TextUtils.isEmpty(perfectDaysText)) {
            editPerfectControllerDays.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(techniqueText)) {
            editTechniqueSessions.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(lowRescueText)) {
            editLowRescueDays.setError("Required");
            return;
        }

        int perfectDays;
        int techniqueTarget;
        int lowRescueThreshold;

        try {
            perfectDays = Integer.parseInt(perfectDaysText);
            techniqueTarget = Integer.parseInt(techniqueText);
            lowRescueThreshold = Integer.parseInt(lowRescueText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (perfectDays <= 0 || techniqueTarget <= 0 || lowRescueThreshold < 0) {
            Toast.makeText(this,
                    "Values must be positive (rescue threshold can be 0 or more).",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference badgesRef = db.collection("users")
                .document(selectedChildId)
                .collection("badges");

        Map<String, Object> perfectData = new HashMap<String, Object>();
        perfectData.put("target", perfectDays);

        Map<String, Object> techniqueData = new HashMap<String, Object>();
        techniqueData.put("target", techniqueTarget);

        Map<String, Object> lowRescueData = new HashMap<String, Object>();
        lowRescueData.put("target", lowRescueThreshold);

        // Üç dokümanı da kaydet
        badgesRef.document("perfect_controller_week").set(perfectData);
        badgesRef.document("technique_sessions").set(techniqueData);
        badgesRef.document("low_rescue_month").set(lowRescueData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ParentBadgeSettingsActivity.this,
                                "Badge goals saved for this child.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ParentBadgeSettingsActivity.this,
                                "Failed to save badge goals.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
