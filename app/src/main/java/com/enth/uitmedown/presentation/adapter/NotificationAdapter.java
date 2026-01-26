package com.enth.uitmedown.presentation.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.enth.uitmedown.R;
import com.enth.uitmedown.model.MyNotificationResponse;
import com.enth.uitmedown.model.Notification;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{
    private List<Notification> notificationList;
    private Context context;

    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(Notification notif, int position);
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
        Notification notif = notificationList.get(position);

        if (notif == null) return;

        String title = notif.getTitle();
        holder.tvTitle.setText(title != null ? title : "Notification");

        String finalMessage = "New notification received."; // Default fallback

        if (notif.getEvent() != null && notif.getEvent().getTemplateText() != null) {
            String template = notif.getEvent().getTemplateText();

            // Safe Username Fetch
            String username = "Unknown User";
            if (notif.getSender() != null && notif.getSender().getUsername() != null) {
                username = notif.getSender().getUsername();
            }

            // Safe Item Title Fetch
            String itemTitle = "Unknown Item";
            if (notif.getTransaction() != null &&
                    notif.getTransaction().getItem() != null &&
                    notif.getTransaction().getItem().getTitle() != null) {
                itemTitle = notif.getTransaction().getItem().getTitle();
            } else if (notif.getTransaction() != null) {
                // Fallback: If item is gone, maybe we have the ID?
                itemTitle = "Item #" + notif.getTransaction().getItemId();
            }

            // Replace
            finalMessage = template
                    .replace("{user}", username)
                    .replace("{item}", itemTitle);
        }

        holder.tvMessage.setText(finalMessage);

        if (notif.getCreatedAt() != null) {
            holder.tvDate.setText(getRelativeTime(notif.getCreatedAt()));
        } else {
            holder.tvDate.setText("Recently");
        }

        int colorRes = (notif.getIsRead() == 1) ? R.color.textInactive : R.color.colorPrimary;

        // Safety check for Context to avoid crash if view is detached
        if (holder.itemView.getContext() != null) {
            int color = ContextCompat.getColor(holder.itemView.getContext(), colorRes);
            holder.imgIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        holder.itemView.setOnClickListener(v -> {
            // Double check listener is not null
            if (listener != null) {
                int currentPos = holder.getBindingAdapterPosition();
                // Ensure position is valid (item wasn't just deleted/animating out)
                if (currentPos != RecyclerView.NO_POSITION && currentPos < notificationList.size()) {
                    listener.onItemClick(notificationList.get(currentPos), currentPos);
                }
            }
        });
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
