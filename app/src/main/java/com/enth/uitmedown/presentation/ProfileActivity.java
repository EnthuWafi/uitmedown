package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.enth.uitmedown.R;
import com.enth.uitmedown.databinding.ActivityProfileBinding;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.RetrofitClient;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import com.enth.uitmedown.utils.NavigationUtils;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Setup Nav
        NavigationUtils.setupBottomNav(this);

        // 2. Load User Info
        SharedPrefManager spm = new SharedPrefManager(this);
        User user = spm.getUser();

        TextView tvName = binding.tvUsername;
        TextView tvEmail = binding.tvEmail;

        tvName.setText(user.getUsername());
        tvEmail.setText(user.getEmail());

        if (user.getPictureFile() != null && user.getPictureFile().getFile() != null) {
            String serverPath = user.getPictureFile().getFile();

            String fullUrl = RetrofitClient.BASE_URL + serverPath;

            Glide.with(this).load(fullUrl).into(binding.imgProfile);
        }

        // 3. EDIT PROFILE LOGIC
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("USER_DATA", user);
            startActivity(intent);
        });

        if ("admin".equalsIgnoreCase(user.getRole()) || "superadmin".equalsIgnoreCase(user.getRole())) {
            binding.btnAdminDashboard.setVisibility(View.VISIBLE);
            binding.btnAdminDashboard.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminMainActivity.class));
            });
        } else {
            binding.btnAdminDashboard.setVisibility(View.GONE);
        }

        // Option A: Buying
        binding.btnMyOrders.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MyOrdersActivity.class));
        });

        // Option B: Selling
        binding.btnSalesRequests.setOnClickListener(v -> {

            startActivity(new Intent(ProfileActivity.this, RequestsActivity.class));
        });

        // Logout
        Button btnLogout = binding.btnLogout;
        btnLogout.setOnClickListener(v -> {
            spm.logout();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear stack
            startActivity(intent);
        });
    }
}