package com.enth.uitmedown.presentation.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable; // Used for rounded corners on status
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.enth.uitmedown.R;
import com.enth.uitmedown.model.Transaction;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context context;
    private List<Transaction> orderList;
    private OnItemClickListener listener; // Listener Interface

    // 1. Interface for Click Handling
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

        // --- Data Binding ---
        // Safety Check: If item object is null (due to API limits), show ID
        if (transaction.getItem() != null) {
            holder.tvItemName.setText(transaction.getItem().getTitle());
        } else {
            holder.tvItemName.setText("Item #" + transaction.getItemId());
        }

        holder.tvPrice.setText("RM " + String.format("%.2f", transaction.getAmount()));

        // --- Status Logic (Color Coding) ---
        String status = transaction.getStatus() != null ? transaction.getStatus().toUpperCase() : "PENDING";
        holder.tvStatus.setText(status);

        int bgColor, textColor, sideBarColor;

        switch (status) {
            case "ACCEPTED":
                sideBarColor = Color.parseColor("#4CAF50"); // Green
                textColor = Color.parseColor("#1B5E20");    // Dark Green Text
                bgColor = Color.parseColor("#E8F5E9");      // Light Green BG
                break;
            case "REJECTED":
                sideBarColor = Color.parseColor("#F44336"); // Red
                textColor = Color.parseColor("#B71C1C");    // Dark Red Text
                bgColor = Color.parseColor("#FFEBEE");      // Light Red BG
                break;
            default: // PENDING
                sideBarColor = Color.parseColor("#FF9800"); // Orange
                textColor = Color.parseColor("#E65100");    // Dark Orange Text
                bgColor = Color.parseColor("#FFF3E0");      // Light Orange BG
                break;
        }

        // Apply Colors
        holder.statusColor.setBackgroundColor(sideBarColor);
        holder.tvStatus.setTextColor(textColor);

        // Make the status badge rounded programmatically
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(16f); // Rounded corners
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