package com.enth.uitmedown.presentation;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.enth.uitmedown.R;
import com.enth.uitmedown.databinding.ActivityRegisterBinding;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                handleSubmit();
            }
        });
    }

    private void handleSubmit() {
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();

        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("Creating Account...");

        // 3. Call API
        ApiUtils.getUserService().register(email, password).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    // Success!
                    Toast.makeText(RegisterActivity.this, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show();
                    finish(); // Go back to Login Activity
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration Failed: Email might be taken", Toast.LENGTH_LONG).show();
                    resetButton();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private boolean validateInputs() {
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();
        String confirmPassword = binding.edtConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.setError("Valid email is required");
            return false;
        }

        if (password.length() < 6) {
            binding.edtPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.edtConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void resetButton() {
        binding.btnRegister.setEnabled(true);
        binding.btnRegister.setText("Register");
    }
}