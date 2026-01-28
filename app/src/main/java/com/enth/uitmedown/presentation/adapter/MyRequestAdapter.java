package com.enth.uitmedown.presentation.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.enth.uitmedown.R;
import com.enth.uitmedown.model.MyRequestResponse;
import java.util.List;

public class MyRequestAdapter extends RecyclerView.Adapter<MyRequestAdapter.ViewHolder> {

    private Context context;
    private List<MyRequestResponse> requestList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MyRequestResponse request);
    }

    public MyRequestAdapter(Context context, List<MyRequestResponse> requestList, OnItemClickListener listener) {
        this.context = context;
        this.requestList = requestList;
        this.listener = listener;
    }

    public void updateList(List<MyRequestResponse> list) {
        this.requestList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reusing the same row layout is fine
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyRequestResponse request = requestList.get(position);

        String itemName = (request.getItemTitle() != null) ? request.getItemTitle() : "Item #" + request.getItemId();

        String displayTitle = itemName;
        holder.tvItemName.setText(displayTitle);

        // 2. Price
        holder.tvPrice.setText("RM " + String.format("%.2f", request.getAmount()));

        // 3. Status Styling
        String status = request.getStatus() != null ? request.getStatus().toUpperCase() : "PENDING";
        holder.tvStatus.setText(status);

        int bgColor, textColor, sideBarColor;

        switch (status) {
            case "ACCEPTED":
                sideBarColor = ContextCompat.getColor(context, R.color.status_accepted_text);
                textColor    = ContextCompat.getColor(context, R.color.status_accepted_text);
                bgColor      = ContextCompat.getColor(context, R.color.status_accepted_bg);
                break;

            case "REJECTED":
                sideBarColor = ContextCompat.getColor(context, R.color.colorAccentTwo); // Using Red Accent
                textColor    = ContextCompat.getColor(context, R.color.status_rejected_text);
                bgColor      = ContextCompat.getColor(context, R.color.status_rejected_bg);
                break;

            default: // PENDING
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
                listener.onItemClick(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvPrice, tvStatus;
        View statusColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure these IDs match item_order_row.xml
            tvItemName = itemView.findViewById(R.id.tvOrderItemName);
            tvPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            statusColor = itemView.findViewById(R.id.viewStatusColor);
        }
    }
}