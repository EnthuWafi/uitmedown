package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestsActivity extends AppCompatActivity {

    private RecyclerView rvSelling;
    private SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_requests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spm = new SharedPrefManager(this);

        NavigationUtils.setupBottomNav(this);

        rvSelling = findViewById(R.id.rvSelling);
        rvSelling.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIncomingRequests();
    }

    private void loadIncomingRequests() {
        User user = spm.getUser();

        ApiUtils.getTransactionService().getTransactionsBySellerId(user.getToken(), user.getId())
                .enqueue(new Callback<List<Transaction>>() {
                    @Override
                    public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                        if (response.isSuccessful()) {
                            List<Transaction> transactions = response.body();

                            // If 204, body is null. So we create an empty list manually.
                            if (transactions == null) {
                                transactions = new java.util.ArrayList<>();
                            }

                            // 2. Set the adapter
                            OrderAdapter adapter = new OrderAdapter(RequestsActivity.this, transactions, transaction -> {
                                // REDIRECT TO DETAILS
                                Intent intent = new Intent(RequestsActivity.this, TransactionDetailActivity.class);
                                intent.putExtra("TRANSACTION_ID", transaction.getTransactionId());
                                startActivity(intent);
                            });
                            rvSelling.setAdapter(adapter);

                            // Show "No Requests" text if empty
                            if (transactions.isEmpty()) {
                                Toast.makeText(RequestsActivity.this, "No active requests.", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // This handles actual errors (404, 500, etc.)
                            Toast.makeText(RequestsActivity.this, "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Transaction>> call, Throwable t) {
                        Toast.makeText(RequestsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}