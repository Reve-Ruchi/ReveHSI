package com.example.revehsi;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SubmissionSuccessFragment extends Fragment {

    public SubmissionSuccessFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout and save the view to a variable
        View view = inflater.inflate(R.layout.fragment_submission_success, container, false);

        // Now find views
        View progress = view.findViewById(R.id.successProgress);
        View checkMark = view.findViewById(R.id.checkMark);
        View doneText = view.findViewById(R.id.doneText);
        View successBox = view.findViewById(R.id.successBox);
        Button btnBackHome = view.findViewById(R.id.btnBackHome);

        // Delay and update UI
        new Handler().postDelayed(() -> {
            progress.setVisibility(View.GONE);
            successBox.setVisibility(View.VISIBLE); // ✅ Show the success box
            checkMark.setVisibility(View.VISIBLE);
            doneText.setVisibility(View.VISIBLE);
        }, 2500);

        // Handle back button click
        btnBackHome.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
