package com.enth.uitmedown.presentation;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.enth.uitmedown.databinding.ActivityTransactionDetailBinding; // Generated automatically
import com.enth.uitmedown.model.Item;
import com.enth.uitmedown.model.Notification;
import com.enth.uitmedown.model.Transaction;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionDetailActivity extends AppCompatActivity {

    private ActivityTransactionDetailBinding binding; // Binding Class
    private int transactionId;
    private Transaction currentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 1. Inflate Binding
        binding = ActivityTransactionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);
        if (transactionId == -1) {
            Toast.makeText(this, "Error: No ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Load Data
        loadTransactionDetails();
    }

    private void loadTransactionDetails() {
        User user = new SharedPrefManager(this).getUser();
        ApiUtils.getTransactionService().getTransactionById(user.getToken(), transactionId).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentTransaction = response.body();
                    updateUI();
                } else {
                    Toast.makeText(TransactionDetailActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                Toast.makeText(TransactionDetailActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        // --- 1. Populate Basic Info ---
        if (currentTransaction.getItem() != null) {
            binding.tvItemTitle.setText(currentTransaction.getItem().getTitle());
        }
        binding.tvItemPrice.setText("RM " + String.format("%.2f", currentTransaction.getAmount()));

        String status = currentTransaction.getStatus().toUpperCase();
        binding.tvHeaderStatus.setText("Status: " + status);

        // --- 2. Identify Roles ---
        int myId = new SharedPrefManager(this).getUser().getId();
        boolean isSeller = (myId == currentTransaction.getSellerId());

        if (isSeller) {
            String buyerName = (currentTransaction.getBuyer() != null) ? currentTransaction.getBuyer().getUsername() : "Unknown";
            binding.tvOtherPartyName.setText("Buyer: " + buyerName);
        } else {
            String sellerName = (currentTransaction.getSeller() != null) ? currentTransaction.getSeller().getUsername() : "Unknown";
            binding.tvOtherPartyName.setText("Seller: " + sellerName);
        }

        // --- 3. VISIBILITY LOGIC ---

        // Hide everything first, then show what's needed
        binding.layoutSellerActions.setVisibility(View.GONE);
        binding.layoutMeetingInfo.setVisibility(View.GONE);
        binding.tvRejectedNotice.setVisibility(View.GONE);

        if (status.equals("ACCEPTED")) {
            // SHOW MEETING INFO
            binding.layoutMeetingInfo.setVisibility(View.VISIBLE);
            binding.tvMeetupLoc.setText("Location: " + currentTransaction.getMeetupLocation());
            binding.tvSellerNote.setText("Note: " + currentTransaction.getSellerNote());

        } else if (status.equals("REJECTED")) {
            // SHOW REJECT NOTICE
            binding.tvRejectedNotice.setVisibility(View.VISIBLE);

        } else if (status.equals("PENDING")) {
            // PENDING STATE
            if (isSeller) {
                // Seller can act
                binding.layoutSellerActions.setVisibility(View.VISIBLE);

                binding.btnAccept.setOnClickListener(v -> processDecision("ACCEPTED"));
                binding.btnReject.setOnClickListener(v -> processDecision("REJECTED"));
            } else {
                // Buyer waiting...
                binding.tvHeaderStatus.setText("Status: Waiting for Seller response");
            }
        }
    }

    private void processDecision(String newStatus) {
        User user = new SharedPrefManager(this).getUser();

        if (newStatus.equals("ACCEPTED")) {
            // Validate Inputs
            String loc = binding.edtMeetupLocation.getText().toString().trim();
            String note = binding.edtSellerNote.getText().toString().trim();

            if (loc.isEmpty()) {
                binding.edtMeetupLocation.setError("Location is required");
                return;
            }

            currentTransaction.setMeetupLocation(loc);
            currentTransaction.setSellerNote(note);
        }

        // Update local object status
        currentTransaction.setStatus(newStatus);

        // Send to Server
        ApiUtils.getTransactionService().updateTransaction(user.getToken(), transactionId, currentTransaction).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TransactionDetailActivity.this, "Updated!", Toast.LENGTH_SHORT).show();

                    if (newStatus.equals("ACCEPTED")) {
                        markItemAsSold(currentTransaction.getItemId());
                    } else {
                        finish(); // Close if rejected
                    }

                    // Refresh UI to show the new state (Accepted info)
                    updateUI();
                }
            }
            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                Toast.makeText(TransactionDetailActivity.this, "Failed to update", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markItemAsSold(int itemId) {
        // Only update the status field
        Item updateItem = new Item();
        updateItem.setStatus("SOLD");

        User user = new SharedPrefManager(this).getUser();
        ApiUtils.getItemService().updateItem(user.getToken(), itemId, updateItem).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                Toast.makeText(TransactionDetailActivity.this, "Item marked as SOLD", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Call<Item> call, Throwable t) { }
        });
    }

    private void sendNotificationToBuyer(Transaction transaction) {
        // 1. Prepare Notification
        Notification notif = new Notification();
        notif.setSenderId(transaction.getSellerId());
        notif.setReceiverId(transaction.getBuyerId()); // Send to BUYER
        notif.setTransactionId(transaction.getTransactionId());
        notif.setEventId("SALE_ACCEPTED");
        notif.setTitle("Offer Accepted!");
        notif.setIsRead(0);

        // 2. Send via API
        User user = new SharedPrefManager(this).getUser();
        ApiUtils.getNotificationService().createNotification(user.getToken(), notif)
                .enqueue(new Callback<Notification>() {
                    @Override
                    public void onResponse(Call<Notification> call, Response<Notification> response) {
                        // Log.d("NOTIF", "Sent to buyer");
                    }
                    @Override
                    public void onFailure(Call<Notification> call, Throwable t) { }
                });
    }
}