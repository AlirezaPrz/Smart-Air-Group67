package com.example.smartairsetup.badges;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartairsetup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ChildBadgesActivity extends AppCompatActivity {
    private ImageView imageBadgePerfectWeek;
    private ImageView imageBadgeTechnique;
    private ImageView imageBadgeLowRescueMonth;
    private TextView textBadgeTechniqueDesc;
    private FirebaseFirestore db;
    public static final String EXTRA_PARENT_ID = "EXTRA_PARENT_ID";
    public static final String EXTRA_CHILD_ID = "EXTRA_CHILD_ID";
    private String parentUid;
    private String childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_badge);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imageBadgePerfectWeek = findViewById(R.id.imageBadgePerfectWeek);
        imageBadgeTechnique = findViewById(R.id.imageBadgeTechnique);
        imageBadgeLowRescueMonth = findViewById(R.id.imageBadgeLowRescueMonth);
        textBadgeTechniqueDesc = findViewById(R.id.textBadgeTechniqueDesc);

        TextView textBackBadges = findViewById(R.id.textBackBadges);
        textBackBadges.setOnClickListener(view -> finish());

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            parentUid = currentUser.getUid();
        }
        else {
            parentUid = getIntent().getStringExtra(EXTRA_PARENT_ID);
        }

        childId = getIntent().getStringExtra(EXTRA_CHILD_ID);
        if (childId == null || childId.isEmpty() ) {
            childId = getIntent().getStringExtra("CHILD_ID");
        }
        if (parentUid == null || childId == null || childId.isEmpty()) {
            Toast.makeText(this, "Problem with parent or child id", Toast.LENGTH_SHORT).show();
            updateBadges(false, false, false);
            return;
        }
        loadBadges();
    }

    private void loadBadges() {
        if (parentUid == null || childId == null) {
            updateBadges(false, false, false);
            return;
        }

        textBadgeTechniqueDesc.setText("Use perfect inhaler technique 10 times.");

        CollectionReference badgesRef = db.collection("users")
                .document(parentUid)
                .collection("children")
                .document(childId)
                .collection("badges");

        badgesRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()== false){
                updateBadges(false, false, false);
                return;
            }
            boolean PerfectWeek = false;
            boolean Technique = false;
            boolean LowRescueMonth = false;

            QuerySnapshot snapshot = task.getResult();
            if (snapshot != null) {
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    String badgeId = doc.getId();

                    Boolean earnedFlag = doc.getBoolean("earned");
                    Long targetLong = doc.getLong("target");
                    Long progressLong = doc.getLong("progress");

                    if ("technique_sessions".equals(badgeId)) {
                        if (targetLong != null) {
                            int target = targetLong.intValue();
                            String desc = "Use perfect inhaler technique " + target + " times.";
                            textBadgeTechniqueDesc.setText(desc);
                        }

                        boolean earned = false;
                        if (earnedFlag != null && earnedFlag) {
                            earned = true;
                        } else if (targetLong != null && progressLong != null && progressLong >= targetLong) {
                            earned = true;
                        }

                        if (earned) {
                            Technique = true;
                        }

                        continue;
                    }

                    boolean earned = false;
                    if (earnedFlag != null && earnedFlag) {
                        earned = true;
                    } else if (targetLong != null && progressLong != null && progressLong >= targetLong) {
                        earned = true;
                    }

                    if (!earned) {
                        continue;
                    }

                    if ("perfect_controller_week".equals(badgeId)) {
                        PerfectWeek = true;
                    } else if ("low_rescue_month".equals(badgeId)) {
                        LowRescueMonth = true;
                    }
                }
            }

            updateBadges(PerfectWeek, Technique, LowRescueMonth);
        });
    }

    private void Badge(){

    }

    private void updateBadges(boolean PerfectWeek,
                              boolean Technique,
                              boolean LowRescueMonth) {

        int greenColor = R.color.button_icon_color;

        if(PerfectWeek == true){
            imageBadgePerfectWeek.setImageTintList(
                    ContextCompat.getColorStateList(this, greenColor)
            );
        }
        else{
            imageBadgePerfectWeek.setImageTintList(null);
        }

        if(Technique == true){
            imageBadgeTechnique.setImageTintList(
                    ContextCompat.getColorStateList(this, greenColor)
            );
        }
        else{
            imageBadgeTechnique.setImageTintList(null);
        }

        if(LowRescueMonth == true){
            imageBadgeLowRescueMonth.setImageTintList(
                    ContextCompat.getColorStateList(this, greenColor)
            );
        }
        else{
            imageBadgeLowRescueMonth.setImageTintList(null);
        }
    }

}
