package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.enth.uitmedown.databinding.ActivityItemDetailBinding; // Generated Binding Class
import com.enth.uitmedown.model.DeleteResponse;
import com.enth.uitmedown.model.Item;
import com.enth.uitmedown.model.Notification;
import com.enth.uitmedown.model.Transaction;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.remote.RetrofitClient;
import com.enth.uitmedown.sharedpref.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailActivity extends AppCompatActivity {

    private ActivityItemDetailBinding binding; // View Binding
    private Item item;
    private SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 1. Inflate Binding
        binding = ActivityItemDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spm = new SharedPrefManager(this);

        // 2. Get Item from Intent
        // Note: Make sure "ITEM" matches exactly what you passed in Adapter
        if (getIntent().getSerializableExtra("ITEM") != null) {
            item = (Item) getIntent().getSerializableExtra("ITEM");
        } else {
            Toast.makeText(this, "Error loading item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        updateUI();
        setupLogic();
    }

    private void updateUI() {
        binding.tvDetailTitle.setText(item.getTitle());
        binding.tvDetailPrice.setText("RM " + item.getPrice());
        binding.tvDetailDesc.setText(item.getDescription());

        binding.tvSellerName.setText(item.getSeller().getUsername());

        if (item.getFile() != null && item.getFile().getFile() != null) {
            String serverPath = item.getFile().getFile();

            String fullUrl = RetrofitClient.BASE_URL + serverPath;

            Glide.with(this).load(fullUrl).into(binding.imgDetail);
        }
    }

    private void setupLogic() {
        int myUserId = spm.getUser().getId();
        boolean isOwner = (myUserId == item.getSellerId());
        boolean isSold = "sold".equalsIgnoreCase(item.getStatus());

        // HIDE ALL INITIALLY
        binding.btnRequestBuy.setVisibility(View.GONE);
        binding.layoutSellerActions.setVisibility(View.GONE);
        binding.btnItemSold.setVisibility(View.GONE);

        if (isSold) {
            // Case 1: Item is Sold
            binding.btnItemSold.setVisibility(View.VISIBLE);
        } else if (isOwner) {
            // Case 2: I am the Owner -> Show Edit/Delete
            binding.layoutSellerActions.setVisibility(View.VISIBLE);

            binding.btnDelete.setOnClickListener(v -> deleteItem());
            binding.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateItemActivity.class);
                intent.putExtra("EDIT_ITEM", item);
                startActivity(intent);
            });
        } else {
            // Case 3: I am a Buyer -> Show Buy Button
            binding.btnRequestBuy.setVisibility(View.VISIBLE);
            binding.btnRequestBuy.setOnClickListener(v -> submitBuyRequest());
        }
    }

    private void submitBuyRequest() {
        User user = spm.getUser();
        int myUserId = user.getId();

        if (myUserId == item.getSellerId()) {
            Toast.makeText(this, "You cannot buy your own item!", Toast.LENGTH_SHORT).show();
            return;
        }

        Transaction newDeal = new Transaction();
        newDeal.setItemId(item.getItemId());
        newDeal.setBuyerId(myUserId);
        newDeal.setSellerId(item.getSellerId());
        newDeal.setAmount(item.getPrice());
        newDeal.setStatus("pending");

        ApiUtils.getTransactionService().createTransaction(user.getToken(), newDeal).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ItemDetailActivity.this, "Request Sent!", Toast.LENGTH_LONG).show();

                    Transaction createdTransaction = response.body();
                    if (createdTransaction != null) {
                        createNotificationForSeller(createdTransaction);
                    }
                    finish();
                } else {
                    Toast.makeText(ItemDetailActivity.this, "Request Failed (Already requested?)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                Toast.makeText(ItemDetailActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteItem() {
        User user = spm.getUser();

        ApiUtils.getItemService().deleteItem(user.getToken(), item.getItemId()).enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ItemDetailActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ItemDetailActivity.this, "Delete failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<DeleteResponse> call, Throwable t) {
                Toast.makeText(ItemDetailActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationForSeller(@NonNull Transaction transaction) {
        User me = spm.getUser();

        Notification notif = new Notification();
        notif.setSenderId(me.getId());
        notif.setReceiverId(item.getSellerId());
        notif.setTitle("New Buy Request");
        notif.setEventId("BUY_REQUEST");
        notif.setTransactionId(transaction.getTransactionId());

        ApiUtils.getNotificationService().createNotification(me.getToken(), notif).enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {}
            @Override
            public void onFailure(Call<Notification> call, Throwable t) {}
        });
    }
}