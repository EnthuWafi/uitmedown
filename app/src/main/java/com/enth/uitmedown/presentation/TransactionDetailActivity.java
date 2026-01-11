package com.enth.uitmedown.presentation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.enth.uitmedown.R;
import com.enth.uitmedown.model.Item;
import com.enth.uitmedown.model.Transaction;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionDetailActivity extends AppCompatActivity {

    private int transactionId;
    private Transaction currentTransaction;
    // UI Elements
    private TextView tvItemTitle, tvItemPrice, tvOtherParty, tvHeaderStatus;
    private LinearLayout layoutSellerActions, layoutMeetingInfo;
    private TextView tvMeetupLoc, tvSellerNote, tvRejectedNotice;
    private EditText edtLocation, edtNote;
    private Button btnAccept, btnReject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);
        if (transactionId == -1) {
            Toast.makeText(this, "Error: No ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        loadTransactionDetails();
    }

    private void initViews() {
        tvItemTitle = findViewById(R.id.tvItemTitle);
        tvItemPrice = findViewById(R.id.tvItemPrice);
        tvOtherParty = findViewById(R.id.tvOtherPartyName);
        tvHeaderStatus = findViewById(R.id.tvHeaderStatus);

        layoutSellerActions = findViewById(R.id.layoutSellerActions);
        layoutMeetingInfo = findViewById(R.id.layoutMeetingInfo);
        tvRejectedNotice = findViewById(R.id.tvRejectedNotice);

        tvMeetupLoc = findViewById(R.id.tvMeetupLoc);
        tvSellerNote = findViewById(R.id.tvSellerNote);

        edtLocation = findViewById(R.id.edtMeetupLocation);
        edtNote = findViewById(R.id.edtSellerNote);

        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
    }

    private void loadTransactionDetails() {
        User user = new SharedPrefManager(this).getUser();
        ApiUtils.getTransactionService().getTransactionById(user.getToken(), transactionId).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) {
                    currentTransaction = response.body();
                    updateUI();
                }
            }
            @Override
            public void onFailure(Call<Transaction> call, Throwable t) { }
        });
    }

    private void updateUI() {
        // 1. Basic Info
        if (currentTransaction.getItem() != null) {
            tvItemTitle.setText(currentTransaction.getItem().getTitle());
        }
        tvItemPrice.setText("RM " + String.format("%.2f", currentTransaction.getAmount()));

        // 2. Identify Roles
        int myId = new SharedPrefManager(this).getUser().getId();
        boolean isSeller = (myId == currentTransaction.getSellerId());
        String status = currentTransaction.getStatus().toUpperCase(); // "PENDING", "ACCEPTED", "REJECTED"

        // 3. Show "Who am I dealing with?"
        if (isSeller) {
            String buyerName = (currentTransaction.getBuyer() != null) ? currentTransaction.getBuyer().getUsername() : "Unknown";
            tvOtherParty.setText("Buyer: " + buyerName);
        } else {
            String sellerName = (currentTransaction.getSeller() != null) ? currentTransaction.getSeller().getUsername() : "Unknown";
            tvOtherParty.setText("Seller: " + sellerName);
        }

        // 4. LOGIC ENGINE: VISIBILITY RULES

        // RULE A: Meeting Info (Visible to ANYONE if Accepted)
        if (status.equals("ACCEPTED")) {
            layoutMeetingInfo.setVisibility(View.VISIBLE);
            layoutSellerActions.setVisibility(View.GONE);
            tvRejectedNotice.setVisibility(View.GONE);

            tvMeetupLoc.setText("Location: " + currentTransaction.getMeetupLocation());
            tvSellerNote.setText("Note: " + currentTransaction.getSellerNote());
        }
        // RULE B: Rejection Notice (Visible to ANYONE if Rejected)
        else if (status.equals("REJECTED")) {
            tvRejectedNotice.setVisibility(View.VISIBLE);
            layoutSellerActions.setVisibility(View.GONE);
            layoutMeetingInfo.setVisibility(View.GONE);
        }
        // RULE C: Pending Actions (Visible ONLY to SELLER)
        else if (status.equals("PENDING")) {
            if (isSeller) {
                layoutSellerActions.setVisibility(View.VISIBLE);

                // Attach Listeners
                btnAccept.setOnClickListener(v -> processDecision("ACCEPTED"));
                btnReject.setOnClickListener(v -> processDecision("REJECTED"));
            } else {
                // I am the Buyer, and it's pending.
                // Show nothing special, maybe just "Waiting for seller..."
                layoutSellerActions.setVisibility(View.GONE);
                tvHeaderStatus.setText("Status: Waiting for Seller");
            }
        }
    }
    private void processDecision(String status) {

        User user = new SharedPrefManager(this).getUser();
        if (status.equals("ACCEPTED")) {
            // Validate Inputs
            String loc = edtLocation.getText().toString().trim();
            String note = edtNote.getText().toString().trim();

            if (loc.isEmpty()) {
                edtLocation.setError("Required");
                return;
            }

            currentTransaction.setMeetupLocation(loc);
            currentTransaction.setSellerNote(note);
        }

        // Send Update
        currentTransaction.setStatus(status);

        ApiUtils.getTransactionService().updateTransaction(user.getToken(),transactionId, currentTransaction).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TransactionDetailActivity.this, "Updated!", Toast.LENGTH_SHORT).show();

                    if (status.equals("ACCEPTED")) {
                        markItemAsSold(currentTransaction.getItemId());
                    } else {
                        finish(); // Close page if rejected
                    }
                }
            }
            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                Toast.makeText(TransactionDetailActivity.this, "Failed to update", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markItemAsSold(int itemId) {
        // Create a temporary item object just to update status
        Item updateItem = new Item();
        updateItem.setStatus("SOLD");

        User user = new SharedPrefManager(this).getUser();
        ApiUtils.getItemService().updateItem(user.getToken(), itemId, updateItem).enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                Toast.makeText(TransactionDetailActivity.this, "Deal Closed!", Toast.LENGTH_LONG).show();
                finish();
            }
            @Override
            public void onFailure(Call<Item> call, Throwable t) { }
        });
    }
}