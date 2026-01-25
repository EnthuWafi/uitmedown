package com.enth.uitmedown.presentation.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable; // Used for rounded corners on status
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.enth.uitmedown.R;
import com.enth.uitmedown.model.Transaction;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context context;
    private List<Transaction> orderList;
    private OnItemClickListener listener; // Listener Interface

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    // 2. Constructor accepts the listener
    public OrderAdapter(Context context, List<Transaction> orderList, OnItemClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = orderList.get(position);

        if (transaction.getItem() != null) {
            holder.tvItemName.setText(transaction.getItem().getTitle());
        } else {
            holder.tvItemName.setText("Item #" + transaction.getItemId());
        }

        holder.tvPrice.setText("RM " + String.format("%.2f", transaction.getAmount()));

        String status = transaction.getStatus() != null ? transaction.getStatus().toUpperCase() : "PENDING";
        holder.tvStatus.setText(status);

        int bgColor, textColor, sideBarColor;

        switch (status) {
            case "ACCEPTED":
                // Green Theme
                sideBarColor = ContextCompat.getColor(context, R.color.status_accepted_text);
                textColor    = ContextCompat.getColor(context, R.color.status_accepted_text);
                bgColor      = ContextCompat.getColor(context, R.color.status_accepted_bg);
                break;

            case "REJECTED":
                // Red Theme
                sideBarColor = ContextCompat.getColor(context, R.color.colorAccentTwo);
                textColor    = ContextCompat.getColor(context, R.color.status_rejected_text);
                bgColor      = ContextCompat.getColor(context, R.color.status_rejected_bg);
                break;

            default: // PENDING
                // Orange Theme
                sideBarColor = ContextCompat.getColor(context, R.color.status_pending_text);
                textColor    = ContextCompat.getColor(context, R.color.status_pending_text);
                bgColor      = ContextCompat.getColor(context, R.color.status_pending_bg);
                break;
        }

        holder.statusColor.setBackgroundColor(sideBarColor);
        holder.tvStatus.setTextColor(textColor);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(16f);
        shape.setColor(bgColor);
        holder.tvStatus.setBackground(shape);

        // --- Click Listener ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvPrice, tvStatus;
        View statusColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvOrderItemName);
            tvPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            statusColor = itemView.findViewById(R.id.viewStatusColor);
        }
    }
}