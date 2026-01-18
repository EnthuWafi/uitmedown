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
import com.enth.uitmedown.model.MyNotificationResponse;
import com.enth.uitmedown.model.Notification;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.presentation.adapter.MyNotificationAdapter;
import com.enth.uitmedown.remote.ApiUtils;
import com.enth.uitmedown.sharedpref.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvNotifs = findViewById(R.id.rvNotifications);
        rvNotifs.setLayoutManager(new LinearLayoutManager(this));

        loadNotifications();
    }

    private void loadNotifications() {
        SharedPrefManager spm = new SharedPrefManager(this);
        User user = spm.getUser();

        ApiUtils.getNotificationService().getNotificationsByReceiverId(user.getToken(), user.getId()).enqueue(new Callback<List<MyNotificationResponse>>() {
            @Override
            public void onResponse(Call<List<MyNotificationResponse>> call, Response<List<MyNotificationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MyNotificationAdapter adapter = new MyNotificationAdapter(
                            NotificationActivity.this,
                            response.body(),
                            (notification, position) -> {

                                if ("BUY_REQUEST".equals(notification.getEventId())) {
                                    Intent intent = new Intent(NotificationActivity.this, TransactionDetailActivity.class);
                                    intent.putExtra("TRANSACTION_ID", notification.getTransactionId());
                                    startActivity(intent);
                                }

                                notification.setIsRead(1);

                                if (rvNotifs.getAdapter() != null) {
                                    rvNotifs.getAdapter().notifyItemChanged(position);
                                }

                                updateNotificationRead(notification);

                            }
                    );
                    rvNotifs.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<MyNotificationResponse>> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "Error loading notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNotificationRead(MyNotificationResponse notification) {
        SharedPrefManager spm = new SharedPrefManager(this);
        User user = spm.getUser();

        ApiUtils.getNotificationService().updateNotificationStatus(
                user.getToken(), notification.getNotificationId(),
                1).enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {
                if (response.isSuccessful()){
                    Toast.makeText(NotificationActivity.this, "Notification marked as read", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Notification> call, Throwable t) { }
        });
    }
}