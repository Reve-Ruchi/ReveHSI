package com.example.revehsi;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    private EditText edtUsername, edtPassword;
    private TextView tvError, signupText;
    private Button btnLogin;
    private ProgressBar progressBar;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        edtUsername = view.findViewById(R.id.edtUsername);
        edtPassword = view.findViewById(R.id.edtPassword);
        tvError = view.findViewById(R.id.tvError);
        btnLogin = view.findViewById(R.id.btnLogin);
        progressBar = view.findViewById(R.id.progressBar);
        signupText = view.findViewById(R.id.signupText);

        // Login Button Click
        btnLogin.setOnClickListener(v -> {
            tvError.setVisibility(View.GONE); // Hide error initially
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                tvError.setText("Username is required");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            if (TextUtils.isEmpty(password)) {
                tvError.setText("Password is required");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);

            view.postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (username.equals("admin") && password.equals("admin123")) {
                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();


                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new SelectDeviceFragment())
                            .addToBackStack(null)
                            .commit();
                } else {
                    tvError.setText("Invalid username or password");
                    tvError.setVisibility(View.VISIBLE);
                }
            }, 1500);

        });

        signupText.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Signup clicked", Toast.LENGTH_SHORT).show();

            // TODO: Replace this with navigation to SignupFragment
        });
    }
}
