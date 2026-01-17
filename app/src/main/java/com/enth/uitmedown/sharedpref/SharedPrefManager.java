package com.enth.uitmedown.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

import com.enth.uitmedown.model.User;
import com.google.gson.Gson;

public class SharedPrefManager {
    //the constants
    private static final String SHARED_PREF_NAME = "uitmedownsharedpref";
    private static final String KEY_USER_JSON = "key_user_json";

    private final Context mCtx;
    private final Gson gson;

    public SharedPrefManager(Context context) {
        mCtx = context;
        gson = new Gson();
    }

    /**
     * method to let the user login
     * this method will store the user data in shared preferences
     * @param user
     */
    public void storeUser(User user) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String userJson = gson.toJson(user);

        editor.putString(KEY_USER_JSON, userJson);
        editor.apply();
    }

    /**
     * this method will checker whether user is already logged in or not.
     * return True if already logged in
     */

    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_JSON, null) != null;
    }

    /**
     * this method will give the information of logged in user, retrieved from SharedPreferences
     */
    public User getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        String userJson = sharedPreferences.getString(KEY_USER_JSON, null);

        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    /**
     * this method will logout the user. clear the SharedPreferences
     */
    public void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
