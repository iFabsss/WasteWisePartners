package com.example.wastewisepartner;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    FirebaseUser user;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    TextView outletNameTv, outletEmailTv, outletUidTv;
    RelativeLayout logoutBtn;
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        outletNameTv = findViewById(R.id.outletName_tv);
        outletEmailTv = findViewById(R.id.outletEmail_tv);
        outletUidTv = findViewById(R.id.outletUid_tv);

        logoutBtn = findViewById(R.id.logoutBtn);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            finish();
        });

        db.collection("outlets").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String outletName = documentSnapshot.getString("outletName");
                String outletEmail = user.getEmail();
                String outletUid = user.getUid();

                outletNameTv.setText(outletName);
                outletEmailTv.setText(outletEmail);
                outletUidTv.setText(outletUid);
            }
        });

        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            mAuth.signOut();
            finishAffinity();
        });
    }
}