package com.enth.uitmedown.presentation.adapter;

import android.content.Context;
import android.graphics.Color;
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

    public OrderAdapter(Context context, List<Transaction> orderList) {
        this.context = context;
        this.orderList = orderList;
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

        // 1. Set Item Details
        // Note: Check if your Transaction model has the nested 'Item' object populated!
        // If pRESTige didn't send the full item, you might only have the ID.
        if (transaction.getItem() != null) {
            holder.tvItemName.setText(transaction.getItem().getTitle());
        } else {
            holder.tvItemName.setText("Item #" + transaction.getItemId());
        }

        holder.tvPrice.setText("RM " + String.format("%.2f", transaction.getAmount() ));

        // 2. Status Logic (Color Coding)
        String status = transaction.getStatus().toUpperCase(); // PENDING, ACCEPTED
        holder.tvStatus.setText(status);

        if (status.equals("ACCEPTED")) {
            holder.statusColor.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            holder.tvStatus.setTextColor(Color.parseColor("#E65100")); // Keep text orange or change to green
            holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light Green
        } else if (status.equals("REJECTED")) {
            holder.statusColor.setBackgroundColor(Color.parseColor("#F44336")); // Red
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
        } else {
            // Pending (Orange)
            holder.statusColor.setBackgroundColor(Color.parseColor("#FF9800"));
            holder.tvStatus.setTextColor(Color.parseColor("#E65100"));
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
        }
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