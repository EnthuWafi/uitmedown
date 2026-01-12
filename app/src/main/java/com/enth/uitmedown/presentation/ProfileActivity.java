package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.enth.uitmedown.R;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import com.enth.uitmedown.utils.NavigationUtils;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Setup Nav
        NavigationUtils.setupBottomNav(this);

        // 2. Load User Info
        SharedPrefManager spm = new SharedPrefManager(this);
        User user = spm.getUser();

        TextView tvName = findViewById(R.id.tvUsername);
        TextView tvEmail = findViewById(R.id.tvEmail);

        if (user != null) {
            tvName.setText(user.getUsername());
            tvEmail.setText(user.getEmail());

        }

        // 3. Button Logic

        // Option A: Buying
        findViewById(R.id.btnMyOrders).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MyOrdersActivity.class));
        });

        // Option B: Selling
        findViewById(R.id.btnSalesRequests).setOnClickListener(v -> {

            startActivity(new Intent(ProfileActivity.this, RequestsActivity.class));
        });

        // Logout
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            spm.logout();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear stack
            startActivity(intent);
        });
    }
}