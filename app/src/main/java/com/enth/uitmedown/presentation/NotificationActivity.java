package com.enth.uitmedown.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.enth.uitmedown.R;
import com.enth.uitmedown.model.Notification;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.presentation.adapter.NotificationAdapter;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rvNotifs = findViewById(R.id.rvNotifications);
        rvNotifs.setLayoutManager(new LinearLayoutManager(this));

        loadNotifications();
    }

    private void loadNotifications() {
        SharedPrefManager spm = new SharedPrefManager(this);
        User user = spm.getUser();

        ApiUtils.getNotificationService().getNotificationsByReceiverId(user.getToken(), user.getId()).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Setup Adapter
                    NotificationAdapter adapter = new NotificationAdapter(
                            NotificationActivity.this,
                            response.body(),
                            notification -> {
                                // ON CLICK LOGIC
                                if ("BUY_REQUEST".equals(notification.getEventId())) {
                                    Intent intent = new Intent(NotificationActivity.this, TransactionDetailActivity.class);
                                    intent.putExtra("TRANSACTION_ID", notification.getTransactionId());
                                    startActivity(intent);
                                }
                            }
                    );
                    rvNotifs.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "Error loading notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }
}