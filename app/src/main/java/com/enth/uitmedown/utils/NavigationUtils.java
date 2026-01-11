package com.enth.uitmedown.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
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
    }
}