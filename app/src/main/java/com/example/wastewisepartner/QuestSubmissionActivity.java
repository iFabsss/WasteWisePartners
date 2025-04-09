package com.example.wastewisepartner;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;
import java.util.Map;

public class QuestSubmissionActivity extends AppCompatActivity {

    private Button scanQr_btn, confirmQuestSubmission_btn;
    private EditText questId_et, playerUsername_et;
    private TextView questTitle_tv, questDescription_tv, questPoints_tv;
    private LinearLayout materials_container;
    private ImageButton backBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    String questId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quest_submission);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        scanQr_btn = findViewById(R.id.scanQr_btn);
        confirmQuestSubmission_btn = findViewById(R.id.confirmQuestSubmission_btn);
        questTitle_tv = findViewById(R.id.questTitle_tv);
        questDescription_tv = findViewById(R.id.questDescription_tv);
        questPoints_tv = findViewById(R.id.questPoints_tv);
        materials_container = findViewById(R.id.materials_container);
        questId_et = findViewById(R.id.questId_et);
        playerUsername_et = findViewById(R.id.playerUsername_et);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Back button
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        questId_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String inputQuestId = s.toString().trim();
                if (!inputQuestId.isEmpty()) {
                    loadQuestDetails(inputQuestId);
                } else {
                    clearQuestDetails();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        // Scan QR
        scanQr_btn.setOnClickListener(v -> scanQuestCode());

        // Confirm Quest Submission
        confirmQuestSubmission_btn.setOnClickListener(v -> {
            String username = playerUsername_et.getText().toString();
            String questPointsStr = questPoints_tv.getText().toString();
            int questPoints = Integer.parseInt(questPointsStr);

            // Update user's points
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                            Long prevPoints = document.getLong("points");
                            int updatedPoints = (prevPoints != null ? prevPoints.intValue() : 0) + questPoints;

                            // Update points
                            document.getReference().update("points", updatedPoints);

                            Toast.makeText(this, "Points added to user!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching user", Toast.LENGTH_SHORT).show();
                    });

            // Increment quest takers
            db.collection("quests")
                    .document(questId_et.getText().toString())
                    .update("currentQuestTakers", FieldValue.increment(1));
        });
    }
    private void clearQuestDetails() {
        questTitle_tv.setText("");
        questDescription_tv.setText("");
        questPoints_tv.setText("");
        materials_container.removeAllViews();
    }


    private void loadQuestDetails(String questId) {
        db.collection("quests").document(questId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                questTitle_tv.setText(documentSnapshot.getString("questTitle"));
                questDescription_tv.setText(documentSnapshot.getString("description"));
                questPoints_tv.setText(String.valueOf(documentSnapshot.getLong("points").intValue()));

                materials_container.removeAllViews();
                HashMap<String, Object> materials = (HashMap<String, Object>) documentSnapshot.get("materials");

                for (Map.Entry<String, Object> entry : materials.entrySet()) {
                    String material = entry.getKey();
                    int quantity = ((Long) entry.getValue()).intValue(); // Ensure type safety

                    View materialView = getLayoutInflater().inflate(R.layout.material_item2_layout, null);
                    TextView materialTextView = materialView.findViewById(R.id.material_item);
                    materialTextView.setText("- " + material + ": " + quantity);
                    materials_container.addView(materialView);
                }
            }
        });
    }

    private void scanQuestCode() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan a Quest QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(
            new ScanContract(), result -> {
                if (result.getContents() != null) {
                    questId = result.getContents();
                    questId_et.setText(questId);
                    loadQuestDetails(questId);
                }
            }
    );
}
