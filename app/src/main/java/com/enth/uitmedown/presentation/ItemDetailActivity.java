package com.enth.uitmedown.presentation;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.enth.uitmedown.R;
import com.enth.uitmedown.model.Item;
import com.enth.uitmedown.model.Transaction;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailActivity extends AppCompatActivity {

    private Item item; // The item we are looking at

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // 1. Get the Item from the Intent
        if (getIntent().getSerializableExtra("ITEM_DATA") != null) {
            item = (Item) getIntent().getSerializableExtra("ITEM_DATA");
        } else {
            Toast.makeText(this, "Error loading item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Setup UI
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvDesc = findViewById(R.id.tvDetailDesc);
        ImageView imgDetail = findViewById(R.id.imgDetail);
        Button btnBuy = findViewById(R.id.btnRequestBuy);

        tvTitle.setText(item.getTitle());
        tvPrice.setText("RM " + item.getPrice());
        tvDesc.setText(item.getDescription());

        if (item.getImageUrl() != null) {
            Glide.with(this).load(item.getImageUrl()).into(imgDetail);
        }

        // 3. Buy Button Logic
        btnBuy.setOnClickListener(v -> {
            submitBuyRequest();
        });
    }

    private void submitBuyRequest() {
        SharedPrefManager spm = new SharedPrefManager(this);
        int myUserId = spm.getUser().getId();

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
        newDeal.setPrice(item.getPrice());

        // Send to Server
        ApiUtils.getTransactionService().createTransaction(spm.getUser().getToken(), newDeal).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ItemDetailActivity.this, "Request Sent!", Toast.LENGTH_LONG).show();
                    // Optional: Send Notification API call here inside this success block
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
}