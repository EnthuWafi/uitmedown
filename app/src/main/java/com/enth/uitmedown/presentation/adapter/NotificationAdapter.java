package com.enth.uitmedown.presentation.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.enth.uitmedown.R;
import com.enth.uitmedown.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{
    private List<Notification> notificationList;
    private Context context;

    private OnItemClickListener listener;

    // Interface to handle clicks in the Activity
    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList, OnItemClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getEvent().getTemplateText());

        // Optional: formatting date
        // holder.tvDate.setText(DateUtils.getRelativeTime(notification.getCreatedAt()))
        holder.tvDate.setText("Recent");

        // 2. Handle Read/Unread Status (The Blue Dot)
        // boolean isRead = notification.isRead();
        boolean isRead = false; // Default for testing

        if (isRead) {
            // Grey dot for read
            holder.imgIcon.setColorFilter(ContextCompat.getColor(context, R.color.textInactive), PorterDuff.Mode.SRC_IN);
        } else {
            // Blue dot for new
            holder.imgIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }

        // 3. Click Listener
        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(notification);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvDate;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvDate = itemView.findViewById(R.id.tvNotifDate);
            imgIcon = itemView.findViewById(R.id.imgNotifIcon);
        }
    }

}
