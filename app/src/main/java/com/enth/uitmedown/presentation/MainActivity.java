package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.enth.uitmedown.databinding.ActivityMainBinding;
import com.enth.uitmedown.model.Notification;
import com.enth.uitmedown.presentation.adapter.ItemAdapter;
import com.enth.uitmedown.model.Item;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.remote.ItemService;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import com.enth.uitmedown.utils.GridUtils;
import com.enth.uitmedown.utils.NavigationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ItemService itemService;

    private SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spm = new SharedPrefManager(this);

        NavigationUtils.setupBottomNav(this);

        setupRecyclerView();

        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NotificationActivity.class));
        });

        itemService = ApiUtils.getItemService();

    }

    private void setupRecyclerView() {
        int numberOfColumns = GridUtils.calculateNoOfColumns(this, 180);

        binding.rvItems.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
    }

    /**
     * This method runs every time the Activity comes into view.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
        updateNotificationBadge();
    }

    private void loadItems() {
        // Safety check in case spm/service isn't ready
        if (spm == null || itemService == null) return;

        User user = spm.getUser();

        Call<List<Item>> call = itemService.getAllAvailableItems(user.getToken());

        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Item> items = response.body();

                    // Attach the adapter
                    ItemAdapter adapter = new ItemAdapter(MainActivity.this, items);
                    binding.rvItems.setAdapter(adapter);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load items. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNotificationBadge() {
        User user = spm.getUser();
        if (user == null) return; // Safety check

        Map<String, String> options = new HashMap<>();
        options.put("receiver_id", String.valueOf(user.getId()));
        options.put("is_read", "0");

        ApiUtils.getNotificationService().getNotifications(user.getToken(), options)
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            int unreadCount = response.body().size();

                            if (unreadCount > 0) {
                                binding.tvNotificationBadge.setVisibility(View.VISIBLE);

                                // Cap the number at 99+ so it fits in the circle
                                if (unreadCount > 99) {
                                    binding.tvNotificationBadge.setText("99+");
                                } else {
                                    binding.tvNotificationBadge.setText(String.valueOf(unreadCount));
                                }
                            } else {
                                // Hide if 0
                                binding.tvNotificationBadge.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Notification>> call, Throwable t) {
                        binding.tvNotificationBadge.setVisibility(View.GONE);
                    }
                });
    }

}