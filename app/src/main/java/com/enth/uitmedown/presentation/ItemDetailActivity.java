package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.enth.uitmedown.R;
import com.enth.uitmedown.model.DeleteResponse;
import com.enth.uitmedown.model.Item;
import com.enth.uitmedown.model.Notification;
import com.enth.uitmedown.model.Transaction;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailActivity extends AppCompatActivity {

    private Item item;
    private SharedPrefManager spm;
    private Button btnBuy, btnDelete, btnEdit, btnSold;
    private LinearLayout layoutSellerActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Get Item from Intent
        if (getIntent().getSerializableExtra("ITEM") != null) {
            item = (Item) getIntent().getSerializableExtra("ITEM");
        } else {
            Toast.makeText(this, "Error loading item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupLogic();
    }

    private void initViews() {
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvDesc = findViewById(R.id.tvDetailDesc);
        ImageView imgDetail = findViewById(R.id.imgDetail);

        btnBuy = findViewById(R.id.btnRequestBuy);
        btnDelete = findViewById(R.id.btnDelete);
        btnEdit = findViewById(R.id.btnEdit);
        btnSold = findViewById(R.id.btnItemSold);
        layoutSellerActions = findViewById(R.id.layoutSellerActions);

        tvTitle.setText(item.getTitle());
        tvPrice.setText("RM " + item.getPrice());
        tvDesc.setText(item.getDescription());

        if (item.getImageUrl() != null) {
            Glide.with(this).load(item.getImageUrl()).into(imgDetail);
        }
    }

    private void setupLogic() {
        int myUserId = spm.getUser().getId();
        boolean isOwner = (myUserId == item.getSellerId());
        boolean isSold = "sold".equalsIgnoreCase(item.getStatus());

        // HIDE ALL INITIALLY
        btnBuy.setVisibility(View.GONE);
        layoutSellerActions.setVisibility(View.GONE);
        btnSold.setVisibility(View.GONE);

        if (isSold) {
            // Case 1: Item is Sold
            btnSold.setVisibility(View.VISIBLE);
        } else if (isOwner) {
            // Case 2: I am the Owner (and it's not sold)
            layoutSellerActions.setVisibility(View.VISIBLE);

            btnDelete.setOnClickListener(v -> deleteItem());
            btnEdit.setOnClickListener(v -> {
                // Reuse CreateItemActivity but pass the item to edit
                Intent intent = new Intent(this, CreateItemActivity.class);
                intent.putExtra("EDIT_ITEM", item);
                startActivity(intent);
            });
        } else {
            // I am a Buyer
            btnBuy.setVisibility(View.VISIBLE);
            btnBuy.setOnClickListener(v -> submitBuyRequest());
        }
    }

    private void submitBuyRequest() {
        User user = spm.getUser();
        int myUserId = user.getId();

        // Prevent buying your own item
        if (myUserId == item.getSellerId()) {
            Toast.makeText(this, "You cannot buy your own item!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the Transaction Object
        Transaction newDeal = new Transaction();
        newDeal.setItemId(item.getItemId());
        newDeal.setBuyerId(myUserId);
        newDeal.setSellerId(item.getSellerId());
        newDeal.setAmount(item.getPrice());
        newDeal.setStatus("pending");

        // Send to Server
        ApiUtils.getTransactionService().createTransaction(user.getToken(), newDeal).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ItemDetailActivity.this, "Request Sent!", Toast.LENGTH_LONG).show();
                    //notification
                    // CAPTURE the created transaction (which now includes the ID generated by DB)
                    Transaction createdTransaction = response.body();

                    if (createdTransaction == null) {
                        Toast.makeText(ItemDetailActivity.this, "Failed to create transaction", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createNotificationForSeller(createdTransaction);
                    finish(); // Go back to main menu
                } else {
                    Toast.makeText(ItemDetailActivity.this, "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
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
        int myUserId = user.getId();
        if (myUserId != item.getSellerId()) {
            Toast.makeText(this, "You cannot delete this item!", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiUtils.getItemService().deleteItem(user.getToken(), item.getItemId()).enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                if (response.isSuccessful()) {
                    DeleteResponse resp = response.body();
                    //TODO: idk what to do with resp here

                    Toast.makeText(ItemDetailActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<DeleteResponse> call, Throwable t) { }
        });
    }
    private void createNotificationForSeller(@NonNull Transaction transaction) {
        SharedPrefManager spm = new SharedPrefManager(this);
        User me = spm.getUser();

        Notification notif = new Notification();
        notif.setSenderId(me.getId());
        notif.setReceiverId(item.getSellerId());
        notif.setTitle("New Buy Request");

        notif.setEventId("BUY_REQUEST"); // Matches the ID in your event_types table
        notif.setTransactionId(transaction.getTransactionId()); // Link to the transaction we just made

        // We don't really care about the callback here. If it fails, it fails.
        ApiUtils.getNotificationService().createNotification(spm.getUser().getToken(), notif).enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {
                // Optional logging
            }
            @Override
            public void onFailure(Call<Notification> call, Throwable t) {
                // Optional logging
            }
        });
    }
}