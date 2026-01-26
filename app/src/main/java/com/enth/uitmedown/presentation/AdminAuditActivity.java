package com.enth.uitmedown.presentation;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.enth.uitmedown.databinding.ActivityAdminAuditBinding; // We will create this XML next
import com.enth.uitmedown.model.Transaction;
import com.enth.uitmedown.presentation.adapter.OrderAdapter;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAuditActivity extends AppCompatActivity {

    private ActivityAdminAuditBinding binding;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdminAuditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Header
        binding.btnBack.setOnClickListener(v -> finish());

        // List Setup
        binding.rvAudit.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(AdminAuditActivity.this, new ArrayList<>(), null, true);
        binding.rvAudit.setAdapter(adapter);

        loadTransactions();
    }

    private void loadTransactions() {
        String token = new SharedPrefManager(this).getUser().getToken();

        ApiUtils.getTransactionService().getAllTransactions(token).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Transaction> list = response.body();
                    adapter.updateList(list);
                    calculateStats(list);
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(AdminAuditActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateStats(List<Transaction> list) {
        double totalVolume = 0;
        int completedCount = 0;

        for (Transaction t : list) {
            totalVolume += t.getAmount();
            if ("ACCEPTED".equalsIgnoreCase(t.getStatus())) {
                completedCount++;
            }
        }

        binding.tvTotalVolume.setText("RM " + String.format("%.2f", totalVolume));
        binding.tvTotalCount.setText(completedCount + " Completed / " + list.size() + " Total");
    }
}