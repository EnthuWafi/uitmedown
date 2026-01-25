package com.enth.uitmedown.presentation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.enth.uitmedown.R;
import com.enth.uitmedown.databinding.ActivityAdminMainBinding;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.model.UserRole;
import com.enth.uitmedown.presentation.adapter.UserAdapter;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private SharedPrefManager spm;
    private UserAdapter adapter;
    private List<User> allUsers = new ArrayList<>(); // Keep a master copy for filtering

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 1. Setup Binding
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spm = new SharedPrefManager(this);

        binding.btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        fetchAllUsers();
        setupSearch();
    }

    private void setupRecyclerView() {
        binding.rvUserList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(this, new ArrayList<>(), new UserAdapter.OnUserActionListener() {
            @Override
            public void onStatusChanged(User user, int newStatus) {
                updateUserStatus(user, newStatus);
            }

            @Override
            public void onUserLongClick(User targetUser) {
                User me = spm.getUser();
                if (UserRole.SUPERADMIN.getRoleName().equalsIgnoreCase(me.getRole())) {
                    showForceResetDialog(targetUser);
                } else {
                    Toast.makeText(AdminMainActivity.this, "Only Superadmins can force reset passwords.", Toast.LENGTH_SHORT).show();
                }
            };
        });

        binding.rvUserList.setAdapter(adapter);
    }

    private void showForceResetDialog(User targetUser) {
        // 1. Inflate View
        View view = getLayoutInflater().inflate(R.layout.dialog_admin_reset_password, null);

        TextView tvMsg = view.findViewById(R.id.tvTargetUserMsg);
        TextInputEditText edtNewPass = view.findViewById(R.id.edtNewUserPass);
        TextInputEditText edtAdminPass = view.findViewById(R.id.edtAdminPass);

        tvMsg.setText("Setting new password for: " + targetUser.getUsername());

        new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Reset Password", (dialog, which) -> {
                    String newPass = edtNewPass.getText().toString();
                    String adminPass = edtAdminPass.getText().toString();

                    if (!newPass.isEmpty() && !adminPass.isEmpty()) {
                        performForceReset(targetUser.getEmail(), newPass, adminPass);
                    } else {
                        Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void performForceReset(String targetEmail, String newUserPass, String adminPass) {
        User me = spm.getUser();

        ApiUtils.getUserService().setPasswordByAdmin(
                targetEmail,
                newUserPass,
                me.getEmail(),
                adminPass
        ).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminMainActivity.this, "Password reset successfully.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminMainActivity.this, "Failed. Did you type your admin password correctly?", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(AdminMainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllUsers() {
        String token = spm.getUser().getToken();

        ApiUtils.getUserService().getAllUsers(token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsers = response.body();

                    allUsers.removeIf(u -> u.getId() == spm.getUser().getId());

                    adapter.updateList(allUsers);
                } else {
                    Toast.makeText(AdminMainActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(AdminMainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserStatus(User user, int newStatus) {
        String token = spm.getUser().getToken();
        user.setIsActive(newStatus);

        ApiUtils.getUserService().updateUser(token, user.getId(), user)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            String statusText = (newStatus == 1) ? "activated" : "banned";
                            Toast.makeText(AdminMainActivity.this, "User " + statusText, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminMainActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                            user.setIsActive(newStatus == 1 ? 0 : 1);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(AdminMainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSearch() {
        binding.edtSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        if (query.isEmpty()) {
            adapter.updateList(allUsers);
        } else {
            List<User> filteredList = allUsers.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                            u.getEmail().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.updateList(filteredList);
        }
    }
}