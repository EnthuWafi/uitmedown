package com.enth.uitmedown.presentation.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.enth.uitmedown.R;
import com.enth.uitmedown.model.Event;
import com.enth.uitmedown.model.MyNotificationResponse;
import com.enth.uitmedown.model.Notification;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.text.format.DateUtils;
public class MyNotificationAdapter extends RecyclerView.Adapter<MyNotificationAdapter.ViewHolder>{
    private List<MyNotificationResponse> notificationList;
    private Context context;

    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(MyNotificationResponse notif);
    }

    public MyNotificationAdapter(Context context, List<MyNotificationResponse> notificationList, OnItemClickListener listener) {
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
        MyNotificationResponse notif = notificationList.get(position);

        // 1. Title
        holder.tvTitle.setText(notif.getTitle());

        // 2. Message Logic (Replace Placeholders)
        String rawTemplate = notif.getTemplateText();
        String finalMessage = rawTemplate
                .replace("{user}", notif.getSenderName())
                .replace("{item}", notif.getItemTitle());

        holder.tvMessage.setText(finalMessage);

        holder.tvDate.setText(getRelativeTime(notif.getCreatedAt()));

        // 4. Read/Unread Status
        if (notif.isRead()) {
            holder.imgIcon.setColorFilter(ContextCompat.getColor(context, R.color.textInactive), PorterDuff.Mode.SRC_IN);
        } else {
            holder.imgIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        }

        // 5. Click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(notif));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    private String getRelativeTime(String dbDateString) {
        if (dbDateString == null) return "Just now";

        // MySQL format is usually "yyyy-MM-dd HH:mm:ss"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Assuming DB saves in UTC

        try {
            Date date = sdf.parse(dbDateString);
            long timeInMillis = date.getTime();
            long now = System.currentTimeMillis();

            // Returns "5 mins ago", "Yesterday", etc.
            return DateUtils.getRelativeTimeSpanString(timeInMillis, now, DateUtils.MINUTE_IN_MILLIS).toString();

        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown date";
        }
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
