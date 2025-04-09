package com.example.wastewisepartner;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.ai.client.generativeai.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddQuestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddQuestsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddQuestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddQuestsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddQuestsFragment newInstance(String param1, String param2) {
        AddQuestsFragment fragment = new AddQuestsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    private EditText et_commissionerName, et_questDescription, et_questTitle, et_questTakersNum;
    private CheckBox checkAnonymous;
    private Button post_btn;
    private Boolean isAnonymous;
    private FirebaseAuth mAuth;
    private final Map<String, Integer> pointCriteria = new HashMap<>();

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
        View view = inflater.inflate(R.layout.fragment_add_quests, container, false);
        mAuth = FirebaseAuth.getInstance();

        // Now, find views after the layout is inflated
        et_commissionerName = view.findViewById(R.id.commissionerName_et);
        et_questDescription = view.findViewById(R.id.questDescription_et);
        et_questTitle = view.findViewById(R.id.questTitle_et);

        et_questTakersNum = view.findViewById(R.id.questTakersNum_np);

        checkAnonymous = view.findViewById(R.id.checkAnonymous);

        LinearLayout materialContainer = view.findViewById(R.id.materials_container);
        ImageButton addMaterial_btn = view.findViewById(R.id.addMaterial_btn);

        addMaterial_btn.setOnClickListener(v -> addMaterial(materialContainer));

        post_btn = view.findViewById(R.id.post_btn);

        // Handle initial state of views based on checkboxes
        updateViewStates();

        // Add listeners for dynamic state changes
        checkAnonymous.setOnCheckedChangeListener((buttonView, isChecked) -> updateViewStates());



        post_btn.setOnClickListener(v -> postQuest());

        pointCriteria.put("Plastic Bottles", 7);
        pointCriteria.put("Cans", 8);
        pointCriteria.put("Paper", 5);
        pointCriteria.put("Cardboard", 6);
        pointCriteria.put("Newspaper", 4);
        pointCriteria.put("Other Plastic Materials", 5);
        pointCriteria.put("Electronics", 8);
        pointCriteria.put("Textiles", 8);
        pointCriteria.put("Intact Glass Item", 5);


        // Return the inflated view
        return view;
    }

    private void addMaterial(LinearLayout materialContainer) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View materialView = inflater.inflate(R.layout.material_dropdown_layout, materialContainer, false);

        Spinner spinnerMaterials = materialView.findViewById(R.id.spinner_materials);
        CheckBox checkboxItem = materialView.findViewById(R.id.checkbox_item);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Plastic Bottles", "Cans", "Paper", "Cardboard", "Newspaper", "Other Plastic Materials", "Electronics", "Textiles", "Intact Glass Item", "Other"});
        spinnerMaterials.setAdapter(adapter);

        LinearLayout parentLayout = (LinearLayout) spinnerMaterials.getParent();
        LinearLayout root = (LinearLayout) parentLayout.getParent();

        // Spinner item change listener
        spinnerMaterials.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private EditText otherEt;
            private NumberPicker newNp;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                parentLayout.removeView(newNp);
                root.removeView(otherEt);

                if (selected.equals("Other")) {
                    otherEt = new EditText(requireContext());
                    // Set layout parameters: width=0, height=wrap_content, weight=1
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    otherEt.setLayoutParams(params);
                    otherEt.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.box_with_line));
                    otherEt.setHint("Please specify (e.g. Straw - 1)");
                    otherEt.setInputType(InputType.TYPE_CLASS_TEXT);

                    int paddingInPx = (int) (10 * getResources().getDisplayMetrics().density + 0.5f);

                    otherEt.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
                    otherEt.setTextSize(16);

                    root.addView(otherEt);
                } else {
                    // Re-add NumberPicker
                    newNp = new NumberPicker(requireContext());

                    int sizeInPx = (int) (40 * getResources().getDisplayMetrics().density + 0.5f);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            sizeInPx, // width = 40dp
                            sizeInPx
                    );
                    newNp.setLayoutParams(params);
                    newNp.setMinValue(1);
                    newNp.setMaxValue(100);
                    newNp.setValue(1);
                    newNp.setPadding(5, 5, 5, 5);
                    newNp.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.box_with_line));
                    //newNp.setTextSize(16);
                    parentLayout.addView(newNp);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        materialContainer.addView(materialView);
    }

    Map<String, Object> quest;
    int totalOtherPoints;
    String outletName;
    private void postQuest() {
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        quest = new HashMap<>();
        Boolean hasOther = false;
        String questTakersNumStr = et_questTakersNum.getText().toString().trim();
        int questTakersNum = questTakersNumStr.isEmpty() ? 1 : Integer.parseInt(questTakersNumStr);

        String commissionerName = isAnonymous ? "Anonymous" : et_commissionerName.getText().toString().trim();
        String questDescription = et_questDescription.getText().toString().trim();
        String questTitle = et_questTitle.getText().toString().trim();

        if (commissionerName.isEmpty()){
            et_commissionerName.setError("Name is required");
            return;
        }

        if (questTitle.isEmpty()){
            et_questTitle.setError("Title is required");
            return;
        }

        if (questDescription.isEmpty()){
            et_questDescription.setError("Description is required");
            return;
        }

        LinearLayout materialContainer = requireView().findViewById(R.id.materials_container);
        Map<String, Integer> materials = new HashMap<>();

        for (int i = 0; i < materialContainer.getChildCount(); i++){
            View materialView = materialContainer.getChildAt(i);
            Spinner spinnerMaterials = materialView.findViewById(R.id.spinner_materials);
            CheckBox checkboxItem = materialView.findViewById(R.id.checkbox_item);

            if (!checkboxItem.isChecked()) continue;

            String material = spinnerMaterials.getSelectedItem().toString();
            ViewGroup parent = (ViewGroup) spinnerMaterials.getParent();

            if (material.equals("Other")) {
                // Get the EditText below materialView
                ViewGroup grandParent = (ViewGroup) parent.getParent();
                EditText otherEt = (EditText) grandParent.getChildAt(grandParent.getChildCount() - 1);
                String otherText = otherEt.getText().toString().trim();

                if (!otherText.isEmpty()) {
                    String[] parts = otherText.split("-");
                    if (parts.length == 2) {
                        String customMaterial = parts[0].trim();
                        int quantity = Integer.parseInt(parts[1].trim());
                        materials.put(customMaterial, quantity);
                    } else {
                        materials.put(otherText, 1); // fallback
                    }
                    hasOther = true;
                }

            } else {
                // Material with NumberPicker
                for (int j = 0; j < parent.getChildCount(); j++) {
                    View child = parent.getChildAt(j);
                    if (child instanceof NumberPicker) {
                        int quantity = ((NumberPicker) child).getValue();
                        materials.put(material, quantity);
                        break;
                    }
                }
            }
        }

        int totalPoints = getTotalPoints(materials, pointCriteria);

        if (hasOther) {
            StringBuilder othersStr = new StringBuilder("Other materials and their quantity: ");
            for (Map.Entry<String, Integer> entry : materials.entrySet()) {
                if (!pointCriteria.containsKey(entry.getKey())) {
                    othersStr.append(entry.getKey()).append(" - ").append(entry.getValue()).append(", ");
                }
            }
            getOtherTotalPoints(othersStr.toString(), totalPoints, materials); // Async call handles Firestore
        } else {
            saveQuest(totalPoints, materials); // No "Other" — proceed directly
        }
    }

    private void updateViewStates() {
        // Disable commissioner name if the anonymous checkbox is checked
        if (checkAnonymous.isChecked()) {
            et_commissionerName.setEnabled(false);
            isAnonymous = true;
        } else {
            et_commissionerName.setEnabled(true);
            isAnonymous = false;
        }
    }

    private int getTotalPoints(Map<String, Integer> materials, Map<String, Integer> pointCriteria){
        int totalPoints = 0;
        for (Map.Entry<String, Integer> entry : materials.entrySet()){
            String material = entry.getKey();
            int quantity = entry.getValue();
            totalPoints += quantity * pointCriteria.getOrDefault(material, 0);
        }
        return totalPoints*2;
    };


    private void getOtherTotalPoints(String otherStr, int baseTotalPoints, Map<String, Integer> materials) {
        String criteria = "Plastic Bottles - 7, Cans - 8, Paper - 5, Cardboard - 6, Newspaper - 4, Other Plastic Materials - 5, Electronics - 8, Textiles - 8, Intact Glass Item - 5";
        String getTotalPrompt =
                "You are an AI that calculates points for recyclable materials. " +
                        "You need to determine the appropriate point/s for the other materials in correlation to the materials criteria. " +
                        "The criteria is below:\n" + criteria + "\n\n" +
                        "Given the following submitted other materials and their quantities: " + otherStr + ", calculate the total points of the submitted other materials. " +
                        "Consider whether they are raw or custom, and their creativity and value. " +
                        "If raw materials, be sure to make it correlated to the points present in the criteria. " +
                        "Respond with a single integer representing the total points. Do not include any explanations or other text.";

        String apiKey = "AIzaSyC11vMdCkATaqed4ol7wiQdpovFudH3Kr0";
        GenerativeModel gmodel = new GenerativeModel("gemini-2.0-flash-lite", apiKey);
        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(gmodel);

        Content content = new Content.Builder()
                .addText(getTotalPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = modelFutures.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    int otherPoints = Integer.parseInt(result.getText().trim());
                    int totalPoints = baseTotalPoints + otherPoints;

                    saveQuest(totalPoints, materials); // <-- Move Firestore saving here
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Invalid AI response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(requireContext(), "AI failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, MoreExecutors.directExecutor());
    }

    private void saveQuest(int totalPoints, Map<String, Integer> materials) {
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("outlets")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        outletName = documentSnapshot.getString("outletName");

                        // Reconstruct quest object now that we have everything
                        String commissionerName = isAnonymous ? "Anonymous" : et_commissionerName.getText().toString().trim();
                        String questDescription = et_questDescription.getText().toString().trim();
                        String questTitle = et_questTitle.getText().toString().trim();
                        String questTakersNumStr = et_questTakersNum.getText().toString().trim();
                        int questTakersNum = questTakersNumStr.isEmpty() ? 1 : Integer.parseInt(questTakersNumStr);

                        Map<String, Object> quest = new HashMap<>();
                        quest.put("questTitle", questTitle);
                        quest.put("commissionerName", commissionerName);
                        quest.put("description", questDescription);
                        quest.put("maxQuestTakers", questTakersNum);
                        quest.put("currentQuestTakers", 0);
                        quest.put("materials", materials);
                        quest.put("points", totalPoints);
                        quest.put("postedBy", outletName);
                        quest.put("outletID", user.getUid());
                        quest.put("timestamp", Timestamp.now());

                        String questId = db.collection("quests").document().getId();
                        db.collection("quests").document(questId).set(quest)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(requireContext(), "Quest posted successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Failed to post quest: " + e, Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }
}