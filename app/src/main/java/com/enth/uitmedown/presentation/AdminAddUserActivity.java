package com.enth.uitmedown.presentation;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.enth.uitmedown.R;
import com.enth.uitmedown.databinding.ActivityAdminAddUserBinding;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.model.UserRole;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddUserActivity extends AppCompatActivity {

    private ActivityAdminAddUserBinding binding;
    private SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAdminAddUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spm = new SharedPrefManager(this);

        setupUI();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnAddUser.setOnClickListener(v -> {
            if (validateInputs()) {
                createUser();
            }
        });
    }

    private void createUser() {
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();
        String selectedRole = binding.spinnerRole.getSelectedItem().toString();

        binding.btnAddUser.setEnabled(false);
        binding.btnAddUser.setText("Creating...");

        ApiUtils.getUserService().register(email, password).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User newUser = response.body();

                    if (selectedRole.equalsIgnoreCase(UserRole.ADMIN.getRoleName())) {
                        promoteToAdmin(newUser);
                    } else {
                        Toast.makeText(AdminAddUserActivity.this, "User Created Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else {
                    Toast.makeText(AdminAddUserActivity.this, "Failed: Email/Username might be taken", Toast.LENGTH_SHORT).show();
                    resetButton();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(AdminAddUserActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private void promoteToAdmin(User newUser) {
        String myToken = spm.getUser().getToken();

        Map<String, Object> updates = new HashMap<>();
        updates.put("role", UserRole.ADMIN.getRoleName());

        ApiUtils.getUserService().updateUserField(myToken, newUser.getId(), updates)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminAddUserActivity.this, "Admin Account Created!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AdminAddUserActivity.this, "User created, but Admin promotion failed.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(AdminAddUserActivity.this, "Network Error on Promotion", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private boolean validateInputs() {
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.getText().toString()).matches()) {
            binding.edtEmail.setError("Invalid Email");
            return false;
        }
        if (binding.edtPassword.getText().toString().length() < 6) {
            binding.edtPassword.setError("Min 6 chars");
            return false;
        }

        if (binding.spinnerRole.getSelectedItem().toString().equalsIgnoreCase(UserRole.ADMIN.getRoleName()) &&
        !UserRole.SUPERADMIN.getRoleName().equalsIgnoreCase(spm.getUser().getRole())) {
            Toast.makeText(AdminAddUserActivity.this, "Only Superadmins can create admin users!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void resetButton() {
        binding.btnAddUser.setEnabled(true);
        binding.btnAddUser.setText("Create Account");
    }
}