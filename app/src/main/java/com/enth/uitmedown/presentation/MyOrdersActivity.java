package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.enth.uitmedown.R;
import com.enth.uitmedown.model.Transaction;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.presentation.adapter.OrderAdapter;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import com.enth.uitmedown.utils.NavigationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyOrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Setup UI
        rvOrders = findViewById(R.id.rvMyOrders);
        tvEmpty = findViewById(R.id.tvEmptyState);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        // 2. Setup Bottom Nav
        NavigationUtils.setupBottomNav(this);

        // 3. Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        SharedPrefManager spm = new SharedPrefManager(this);
        User user = spm.getUser();

        ApiUtils.getTransactionService().getTransactionsByBuyerId(user.getToken(), user.getId()).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Transaction> list = response.body();

                    if (list.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvOrders.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvOrders.setVisibility(View.VISIBLE);

                        OrderAdapter adapter = new OrderAdapter(MyOrdersActivity.this, list, transaction -> {
                            // REDIRECT TO DETAILS
                            Intent intent = new Intent(MyOrdersActivity.this, TransactionDetailActivity.class);
                            intent.putExtra("TRANSACTION_ID", transaction.getTransactionId());
                            startActivity(intent);
                        });

                        rvOrders.setAdapter(adapter);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                // ...
            }
        });
    }
}