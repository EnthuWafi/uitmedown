package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.enth.uitmedown.R;
import com.enth.uitmedown.model.Item;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.presentation.adapter.ItemAdapter;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import com.enth.uitmedown.utils.NavigationUtils;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyListingsActivity extends AppCompatActivity {

    private RecyclerView rvListings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_listings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Setup Navigation
        NavigationUtils.setupBottomNav(this);

        // 2. Setup Recycler View (Grid Mode)
        rvListings = findViewById(R.id.rvMyListings);
        rvListings.setLayoutManager(new GridLayoutManager(this, 2));

        loadMyItems();
    }

    private void loadMyItems() {
        User user = new SharedPrefManager(this).getUser();

        ApiUtils.getItemService().getItemsBySellerId(user.getToken(), user.getId()).enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // We reuse the standard ItemAdapter
                    // If you want special buttons (Edit/Delete), you'd need a specific adapter later
                    ItemAdapter adapter = new ItemAdapter(MyListingsActivity.this, response.body());
                    rvListings.setAdapter(adapter);
                } else {
                    Toast.makeText(MyListingsActivity.this, "Failed to load items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Toast.makeText(MyListingsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}