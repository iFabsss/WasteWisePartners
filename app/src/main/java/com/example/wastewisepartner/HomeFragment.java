package com.example.wastewisepartner;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private FirebaseAuth mAuth;
    private String userID;
    private LinearLayout questContainer;

    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        questContainer = view.findViewById(R.id.quest_container);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userID = user.getUid();

        db.collection("quests")
                .whereEqualTo("outletID", userID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                View questOverview = inflater.inflate(R.layout.quest_overview_layout, questContainer, false);
                                TextView questTitle = questOverview.findViewById(R.id.questTitle);
                                TextView questPoints = questOverview.findViewById(R.id.questPoints);
                                TextView progressNum = questOverview.findViewById(R.id.progressNum_tv);
                                LinearProgressIndicator linearProgress = questOverview.findViewById(R.id.linearProgress_bar);
                                TextView questDescription = questOverview.findViewById(R.id.questDescription);

                                String title = documentSnapshot.getString("questTitle");
                                int points = documentSnapshot.getLong("points").intValue();
                                int numTakers = documentSnapshot.getLong("currentQuestTakers").intValue();
                                int maxTakers = documentSnapshot.getLong("maxQuestTakers").intValue();
                                String description = documentSnapshot.getString("description");
                                String commissionerName = documentSnapshot.getString("commissionerName");
                                HashMap<String, Object> materials = (HashMap<String, Object>) documentSnapshot.get("materials");

                                progressNum.setText(numTakers + "/" + maxTakers);
                                linearProgress.setMax(maxTakers);
                                linearProgress.setProgress(numTakers);

                                questTitle.setText(title);
                                questPoints.setText(String.valueOf(points));
                                questDescription.setText(description);

                                questOverview.setOnClickListener(v -> {
                                    View questView = inflater.inflate(R.layout.quest_layout, container, false);
                                    TextView questViewTitle = questView.findViewById(R.id.questTitle_tv);
                                    TextView questViewDescription = questView.findViewById(R.id.questDescription_tv);
                                    TextView questViewCommissionerName = questView.findViewById(R.id.questCommissionerName_tv);
                                    TextView questViewTotalPoints = questView.findViewById(R.id.totalPoints_tv);
                                    LinearLayout questViewMaterialsContainer = questView.findViewById(R.id.materials_container);
                                    LinearProgressIndicator questViewLinearProgress = questView.findViewById(R.id.linearProgress_bar);
                                    TextView questViewProgressNum = questView.findViewById(R.id.progressNum_tv);
                                    ImageButton questViewBackBtn = questView.findViewById(R.id.backBtn);

                                    questViewTitle.setText(title);
                                    questViewDescription.setText(description);
                                    questViewCommissionerName.setText(commissionerName);
                                    questViewTotalPoints.setText("Total Points: " + points);
                                    questViewLinearProgress.setMax(maxTakers);
                                    questViewLinearProgress.setProgress(numTakers);
                                    questViewProgressNum.setText(numTakers + "/" + maxTakers);

                                    for (Map.Entry<String, Object> entry : materials.entrySet()) {
                                        String material = entry.getKey();
                                        int quantity = ((Long) entry.getValue()).intValue();
                                        View materialItem = inflater.inflate(R.layout.material_item_layout, questViewMaterialsContainer, false);
                                        TextView materialTextView = materialItem.findViewById(R.id.material_item);
                                        materialTextView.setText("- " + material + ": " + quantity);
                                        questViewMaterialsContainer.addView(materialItem);
                                    }

                                    FrameLayout overlayContainer = ((MainActivity) requireActivity()).getOverlayContainer();
                                    overlayContainer.removeAllViews(); // remove previous if any
                                    overlayContainer.addView(questView);
                                    overlayContainer.setVisibility(View.VISIBLE);

                                    questViewBackBtn.setOnClickListener(v1 -> {
                                        overlayContainer.removeAllViews();
                                        overlayContainer.setVisibility(View.GONE);
                                    });

                                });

                                questContainer.addView(questOverview);
                            }
                        })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error getting quests", e);
                });

        return view;
    }
}