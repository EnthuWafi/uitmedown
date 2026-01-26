package com.enth.uitmedown.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.enth.uitmedown.R;
import com.enth.uitmedown.model.User;
import com.enth.uitmedown.remote.RetrofitClient;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onStatusChanged(User user, int newStatus);
        void onUserLongClick(User user, View view);
    }

    private Context context;
    private List<User> userList;
    private OnUserActionListener listener;

    // Constructor accepts the Listener
    public UserAdapter(Context context, List<User> userList, OnUserActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        String displayName = (user.getUsername() != null && !user.getUsername().isEmpty())
                ? user.getUsername()
                : "Unknown";

        holder.tvUserName.setText(displayName);
        holder.tvUserEmail.setText(user.getEmail());

        // --- 2. Set Avatar ---
        if (user.getPictureFile() != null && user.getPictureFile().getFile() != null) {
            String fullUrl = RetrofitClient.BASE_URL + user.getPictureFile().getFile();
            Glide.with(context)
                    .load(fullUrl)
                    .circleCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.switchActive.setOnCheckedChangeListener(null);

        // Set the visual state (1 = Active/True, 0 = Banned/False)
        holder.switchActive.setChecked(user.getIsActive() == 1);

        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only trigger if the USER pressed it (avoids bugs during scroll/recycle)
            if (buttonView.isPressed()) {
                int newStatus = isChecked ? 1 : 0;

                user.setIsActive(newStatus);

                listener.onStatusChanged(user, newStatus);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onUserLongClick(user, v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail;
        ImageView imgAvatar;
        SwitchMaterial switchActive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            imgAvatar = itemView.findViewById(R.id.imgUserAvatar);
            switchActive = itemView.findViewById(R.id.switchActive);
        }
    }
}