package com.enth.uitmedown.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class GridUtils {
    /**
     * Calculates the best number of columns for a grid based on screen width.
     * @param context Context to access resources
     * @param itemWidthDp The desired width of one grid item in DP (e.g., 160 or 180)
     * @return The optimal span count (minimum 1)
     */
    public static int calculateNoOfColumns(Context context, int itemWidthDp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        int noOfColumns = (int) (dpWidth / itemWidthDp);

        return Math.max(noOfColumns, 1);
    }
}
