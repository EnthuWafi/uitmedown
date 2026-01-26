package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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
    private List<User> allUsers = new ArrayList<>();

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

        binding.btnAudit.setOnClickListener(v ->
                startActivity(new Intent(this, AdminAuditActivity.class))
        );

        binding.btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AdminAddUserActivity.class))
        );
    }

    private void setupRecyclerView() {
        binding.rvUserList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(this, new ArrayList<>(), new UserAdapter.OnUserActionListener() {
            @Override
            public void onStatusChanged(User user, int newStatus) {
                updateUserStatus(user, newStatus);
            }

            @Override
            public void onUserLongClick(User targetUser, View view) {
                User me = spm.getUser();
                if (UserRole.SUPERADMIN.getRoleName().equalsIgnoreCase(me.getRole())) {
                    showUserActionMenu(targetUser, view);
                } else {
                    Toast.makeText(AdminMainActivity.this, "Access Denied: Superadmin only.", Toast.LENGTH_SHORT).show();
                }
            };
        });

        binding.rvUserList.setAdapter(adapter);
    }

    private void showUserActionMenu(User targetUser, View view) {
        PopupMenu popup = new PopupMenu(this, view);

        // Add Menu Items Programmatically
        // Menu.add(groupId, itemId, order, title)
        popup.getMenu().add(0, 1, 0, "Force Reset Password");

        boolean isTargetAdmin = UserRole.ADMIN.getRoleName().equalsIgnoreCase(targetUser.getRole());
        if (isTargetAdmin) {
            popup.getMenu().add(0, 2, 1, "Demote to User");
        } else {
            popup.getMenu().add(0, 3, 1, "Promote to Admin");
        }
        popup.getMenu().add(0, 4, 0, "Delete User");

        // 3. Handle Clicks
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    showForceResetDialog(targetUser);
                    return true;
                case 2:
                    changeUserRole(targetUser, "user");
                    return true;
                case 3:
                    changeUserRole(targetUser, "admin");
                    return true;
                case 4:
                    deleteUser(targetUser);
                    return true;
                default:
                    return false;
            }
        });

        popup.show();
    }

    private void deleteUser(User targetUser) {
        String token = spm.getUser().getToken();

        ApiUtils.getUserService().deleteUser(token, targetUser.getId()).enqueue(new Callback<User>(){
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminMainActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                    allUsers.remove(targetUser);
                    adapter.updateList(allUsers);
                    adapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(AdminMainActivity.this, "Delete failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(AdminMainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }

        });
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

        Map<String, Object> updates = new HashMap<>();
        updates.put("is_active", newStatus);

        //prevent deactivating greater authority
        if (user.getRole().equalsIgnoreCase(UserRole.SUPERADMIN.getRoleName())) {
            Toast.makeText(AdminMainActivity.this, "Can't deactivate superadmin", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiUtils.getUserService().updateUserField(token, user.getId(), updates)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            String statusText = (newStatus == 1) ? "activated" : "banned";
                            Toast.makeText(AdminMainActivity.this, "User " + statusText, Toast.LENGTH_SHORT).show();
                            user.setIsActive(newStatus);
                        } else {
                            Toast.makeText(AdminMainActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();

                            user.setIsActive(newStatus == 1 ? 0 : 1);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(AdminMainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();

                        user.setIsActive(newStatus == 1 ? 0 : 1);
                        adapter.notifyDataSetChanged();
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
    private void changeUserRole(User targetUser, String newRole) {
        String token = spm.getUser().getToken();

        Map<String, Object> updates = new HashMap<>();
        updates.put("role", newRole);

        ApiUtils.getUserService().updateUserField(token, targetUser.getId(), updates)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminMainActivity.this, "User role updated to " + newRole, Toast.LENGTH_SHORT).show();
                            targetUser.setRole(newRole);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AdminMainActivity.this, "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(AdminMainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}