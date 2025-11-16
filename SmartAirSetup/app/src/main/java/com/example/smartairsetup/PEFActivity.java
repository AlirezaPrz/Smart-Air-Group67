package com.example.smartairsetup;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class PEFActivity extends AppCompatActivity {

    Button chooseChildButton;
    EditText dailyPEFInput;
    EditText preMedicationPB;
    EditText postMedicationPB;

    final IntegerDataParse pefParser = new IntegerDataParse();
    ChildStorage pefStorage = new ChildStorage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pef);

        chooseChildButton = findViewById(R.id.chooseChildButton);
        dailyPEFInput = findViewById(R.id.dailyPEFInput);
        preMedicationPB = findViewById(R.id.preMedicationPB);
        postMedicationPB = findViewById(R.id.postMedicationPB);
        Button saveButton = findViewById(R.id.savePBButton);

        ChildDiaglog selector = new ChildDiaglog(this);

        chooseChildButton.setOnClickListener(v -> {
            selector.showSelectionDialog(chooseChildButton);
        });

        saveButton.setOnClickListener(v -> savePEF());
    }

    void savePEF() {
        String selectedChild = chooseChildButton.getText().toString();

        int dailyPEF = pefParser.parsePEF(dailyPEFInput);
        int prePEF = pefParser.parsePEF(preMedicationPB);
        int postPEF = pefParser.parsePEF(postMedicationPB);

        if (selectedChild.equals(getString(R.string.choose_child))) {
            Toast.makeText(this, "Please choose a child", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dailyPEF == 0) {
            Toast.makeText(this, "Please enter the daily PEF", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageChild entry = new StorageChild(dailyPEF, prePEF, postPEF);
        pefStorage.save(selectedChild, entry);

        Toast.makeText(this,
                selectedChild + "'s PEF has successfully been saved",
                Toast.LENGTH_SHORT).show();
    }
}
