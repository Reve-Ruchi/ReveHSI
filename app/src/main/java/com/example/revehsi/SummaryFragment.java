package com.example.revehsi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SummaryFragment extends Fragment {

    private TextView tvFarmer, tvSample, tvEmail, tvMobile, tvVillage, tvNotes;
    private TextView tvTemperature, tvPH, tvNitrogen, tvPhosphorus, tvPotassium;
    private Button btnBackHome, btnSubmit;

    public static final String ARG_FARMER = "farmer Name";
    public static final String ARG_SAMPLE = "sample Name";
    public static final String ARG_EMAIL = "email";
    public static final String ARG_MOBILE = "mobile";
    public static final String ARG_VILLAGE = "village";
    public static final String ARG_NOTES = "notes";
    public static final String ARG_TEMP = "temp";
    public static final String ARG_PH = "ph";
    public static final String ARG_N = "n";
    public static final String ARG_P = "p";
    public static final String ARG_K = "k";

    public SummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout only
        // return inflater.inflate(R.layout.fragment_summary, container, false);

        View view = inflater.inflate(R.layout.fragment_summary, container, false);

      // Find buttons from layout
        btnBackHome = view.findViewById(R.id.btnBackHome);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnSubmit.setEnabled(false);

       // Handle "Back to Home" button
        btnBackHome.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        });

       // Handle "Submit" button
        btnSubmit.setOnClickListener(v -> {
            // Optional toast message
            Toast.makeText(requireContext(), "Data Submitted Successfully!", Toast.LENGTH_SHORT).show();

            // Navigate to SubmissionSuccessFragment
            SubmissionSuccessFragment successFragment = new SubmissionSuccessFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, successFragment)
                    .addToBackStack(null)
                    .commit();
        });


        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize all views
        tvFarmer = view.findViewById(R.id.tvFarmer);
        tvSample = view.findViewById(R.id.tvSample);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvMobile = view.findViewById(R.id.tvMobile);
        tvVillage = view.findViewById(R.id.tvVillage);
        tvNotes = view.findViewById(R.id.tvNotes);

        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvPH = view.findViewById(R.id.tvPH);
        tvNitrogen = view.findViewById(R.id.tvNitrogen);
        tvPhosphorus = view.findViewById(R.id.tvPhosphorus);
        tvPotassium = view.findViewById(R.id.tvPotassium);

        // Set data from arguments
        Bundle args = getArguments();
        if (args != null) {
            tvFarmer.setText("Farmer Name: " + args.getString(ARG_FARMER, ""));
            tvSample.setText("Sample Name: " + args.getString(ARG_SAMPLE, ""));
            tvEmail.setText("Email: " + args.getString(ARG_EMAIL, ""));
            tvMobile.setText("Mobile: " + args.getString(ARG_MOBILE, ""));
            tvVillage.setText("Village: " + args.getString(ARG_VILLAGE, ""));
            tvNotes.setText("Notes: " + args.getString(ARG_NOTES, ""));

            tvTemperature.setText("Temperature: " + args.getString(ARG_TEMP, "") + " °C");
            tvPH.setText("PH: " + args.getString(ARG_PH, ""));
            tvNitrogen.setText("Nitrogen (N): " + args.getString(ARG_N, ""));
            tvPhosphorus.setText("Phosphorus (P): " + args.getString(ARG_P, ""));
            tvPotassium.setText("Potassium (K): " + args.getString(ARG_K, ""));

            // ✅ Validate and toggle button
            validateSummaryAndToggleButton();

        }
    }
    private void validateSummaryAndToggleButton() {
        String farmerName = tvFarmer.getText().toString().trim();
        String sampleId = tvSample.getText().toString().trim();
        String email = tvEmail.getText().toString().trim();
        String mobile = tvMobile.getText().toString().trim();
        String village = tvVillage.getText().toString().trim();
        String notes = tvNotes.getText().toString().trim();

        // ✅ Check if all are filled properly
        if (!farmerName.endsWith(":") &&
                !sampleId.endsWith(":") &&
                !email.endsWith(":") &&
                !mobile.endsWith(":") &&
                !village.endsWith(":") &&
                !notes.endsWith(":")) {

            btnSubmit.setEnabled(true);
        } else {
            btnSubmit.setEnabled(false);
        }
    }

}
