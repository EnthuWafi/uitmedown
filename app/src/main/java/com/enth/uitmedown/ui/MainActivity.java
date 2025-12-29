package com.enth.uitmedown.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.enth.uitmedown.R;
import com.enth.uitmedown.model.Item;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.remote.ItemService;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvItems;
    private ItemAdapter adapter;
    private ItemService itemService;

    SharedPrefManager spm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // greet the user
        // if the user is not logged in we will directly them to LoginActivity
        spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isLoggedIn()) { // no session record
            // forward to Login Page
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        rvItems = findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            // TODO: Navigate to "Add Item" Page
            Toast.makeText(this, "Add Item Clicked", Toast.LENGTH_SHORT).show();
        });

        itemService = ApiUtils.getItemService();
        loadItems();

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this::logoutClicked);
    }

    private void loadItems() {
        User user = spm.getUser();
        Call<List<Item>> call = itemService.getAllItems(user.getToken());

        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Item> items = response.body();

                    // Attach the adapter
                    adapter = new ItemAdapter(MainActivity.this, items);
                    rvItems.setAdapter(adapter);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UiTMeDown", t.toString());
            }
        });
    }

    public void logoutClicked(View view) {

        // implement this
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        // display message
        Toast.makeText(getApplicationContext(),
                "You have successfully logged out.",
                Toast.LENGTH_LONG).show();
        // forward to Login Page
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}