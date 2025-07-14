package com.example.revehsi;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;

public class HomeFragment extends Fragment {

    private Button btnCap;
    private EditText edtFarmer;

    private EditText edtsample;
    private EditText edtEmail;
    private EditText edtMobile;
    private EditText edtVillage;
    private EditText edtNotes;
    private FrameLayout overlayLoader;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Toast.makeText(requireContext(), "HomeFragment Loaded!", Toast.LENGTH_SHORT).show();

        btnCap = view.findViewById(R.id.btncap);
        edtFarmer = view.findViewById(R.id.edtname);
        edtsample = view.findViewById(R.id.edtsample);
        edtEmail = view.findViewById(R.id.edtemail);
        edtMobile = view.findViewById(R.id.edtmobile);
        edtVillage = view.findViewById(R.id.edtvillage);
        edtNotes = view.findViewById(R.id.edtnotes);
        overlayLoader = view.findViewById(R.id.overlay_loader);

        // Filter: Farmer Name → Only alphabets
        InputFilter onlyAlphabetsFilter = new InputFilter() {
            public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
                for (int i = start; i < end; ++i) {
                    if (!Character.isLetter(src.charAt(i)) && !Character.isSpaceChar(src.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        edtFarmer.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});

        // Filter: Mobile Number → Only digits, max length 10
        edtMobile.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        edtMobile.setKeyListener(android.text.method.DigitsKeyListener.getInstance("0123456789"));

        // Email validation on focus change
        edtEmail.setOnFocusChangeListener((v1, hasFocus) -> {
            if (!hasFocus && !Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()) {
                edtEmail.setError("Enter valid email");
            }
        });
        btnCap.setOnClickListener(v -> captureData());
        return view;
    }

    private void captureData() {
        String farmer = edtFarmer.getText().toString().trim();
        String sample = edtsample.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String mobile = edtMobile.getText().toString().trim();
        String village = edtVillage.getText().toString().trim();
        String notes = edtNotes.getText().toString().trim();

        if (farmer.isEmpty() || sample.isEmpty() || email.isEmpty() || mobile.isEmpty() || village.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Invalid email");
            return;
        }

        if (mobile.length() != 10) {
            edtMobile.setError("Enter 10-digit mobile number");
            return;
        }

        CircularProgressIndicator circularProgress = requireView().findViewById(R.id.circularProgressIndicator);
        TextView progressText = requireView().findViewById(R.id.progressText);
        overlayLoader.setVisibility(View.VISIBLE);
        btnCap.setEnabled(false);

        // These will hold latest sensor values
        final String[] latestTemp = {""};
        final String[] latestPh = {""};
        final String[] latestN = {""};
        final String[] latestP = {""};
        final String[] latestK = {""};

        new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                int progress = i * 10;

                try {
                    Thread.sleep(500); // Simulate sensor delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // TODO: Replace this with actual sensor data reading logic
                latestTemp[0] = String.format("%.1f", 30 + Math.random() * 5);
                latestPh[0] = String.format("%.1f", 6 + Math.random());
                latestN[0] = String.valueOf((int) (40 + Math.random() * 20));
                latestP[0] = String.valueOf((int) (20 + Math.random() * 10));
                latestK[0] = String.valueOf((int) (20 + Math.random() * 10));

                int finalProgress = progress;
                requireActivity().runOnUiThread(() -> {
                    circularProgress.setProgress(finalProgress);
                    progressText.setText(finalProgress + "%");
                });
            }

            requireActivity().runOnUiThread(() -> {
                overlayLoader.setVisibility(View.GONE);
                btnCap.setEnabled(true);

                navigateToSummary(farmer, sample, email, mobile, village, notes,
                        latestTemp[0], latestPh[0], latestN[0], latestP[0], latestK[0]);
            });

        }).start();
    }
    private void navigateToSummary(String farmer, String sample, String email, String mobile, String village, String notes,
                                   String temp, String ph, String n, String p, String k) {

        Bundle bundle = new Bundle();
        bundle.putString(SummaryFragment.ARG_FARMER, farmer);
        bundle.putString(SummaryFragment.ARG_SAMPLE, sample);
        bundle.putString(SummaryFragment.ARG_EMAIL, email);
        bundle.putString(SummaryFragment.ARG_MOBILE, mobile);
        bundle.putString(SummaryFragment.ARG_VILLAGE, village);
        bundle.putString(SummaryFragment.ARG_NOTES, notes);
        bundle.putString(SummaryFragment.ARG_TEMP, temp);
        bundle.putString(SummaryFragment.ARG_PH, ph);
        bundle.putString(SummaryFragment.ARG_N, n);
        bundle.putString(SummaryFragment.ARG_P, p);
        bundle.putString(SummaryFragment.ARG_K, k);

        SummaryFragment summaryFragment = new SummaryFragment();
        summaryFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, summaryFragment)
                .addToBackStack(null)
                .commit();
    }

}
