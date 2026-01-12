package com.enth.uitmedown.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.enth.uitmedown.R;
import com.enth.uitmedown.presentation.CreateItemActivity;
import com.enth.uitmedown.presentation.MainActivity;
import com.enth.uitmedown.presentation.MyListingsActivity;
import com.enth.uitmedown.presentation.MyOrdersActivity;
import com.enth.uitmedown.presentation.ProfileActivity;
import com.enth.uitmedown.presentation.RequestsActivity;

public class NavigationUtils {

    public static void setupBottomNav(Activity activity) {
        // Find the buttons
        View navHome = activity.findViewById(R.id.navHome);
        View navRequest = activity.findViewById(R.id.navRequests);
        View navSell = activity.findViewById(R.id.navSell);
        View navMyItem = activity.findViewById(R.id.navMyItems);
        View navProfile = activity.findViewById(R.id.navProfile);

        // Set Listeners
        navHome.setOnClickListener(v -> {
            if (!(activity instanceof MainActivity)) { // Don't reload if already here
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.overridePendingTransition(0, 0); // Remove animation for smoother feel
            }
        });

        navRequest.setOnClickListener(v -> {
            if (!(activity instanceof RequestsActivity)) {
                activity.startActivity(new Intent(activity, RequestsActivity.class));
                activity.overridePendingTransition(0, 0);
            }
        });

        navSell.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, CreateItemActivity.class));
        });

        navMyItem.setOnClickListener(v -> {
            if (!(activity instanceof MyListingsActivity)) {
                activity.startActivity(new Intent(activity, MyListingsActivity.class));
                activity.overridePendingTransition(0, 0);
            }
        });

        navProfile.setOnClickListener(v -> {
            if (!(activity instanceof ProfileActivity)) {
                activity.startActivity(new Intent(activity, ProfileActivity.class));
                activity.overridePendingTransition(0, 0);
            }
        });

        highlightActiveTab(activity);
    }

    private static void highlightActiveTab(Activity activity) {
        if (activity instanceof MainActivity) {
            enableTab(activity, R.id.ivHome, R.id.tvHome);
        } else if (activity instanceof RequestsActivity) {
            enableTab(activity, R.id.ivRequests, R.id.tvRequests);
        } else if (activity instanceof MyListingsActivity) {
            enableTab(activity, R.id.ivMyItems, R.id.tvMyItems);
        } else if (activity instanceof ProfileActivity) {
            enableTab(activity, R.id.ivProfile, R.id.tvProfile);
        }
    }

    private static void enableTab(Activity activity, int imageId, int textId) {
        ImageView icon = activity.findViewById(imageId);
        TextView text = activity.findViewById(textId);

        int color = ContextCompat.getColor(activity, R.color.colorPrimary);

        if (icon != null && text != null) {
            icon.setColorFilter(color);
            text.setTextColor(color);
        }
    }
}