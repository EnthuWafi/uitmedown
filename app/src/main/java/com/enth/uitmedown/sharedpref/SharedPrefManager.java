package com.enth.uitmedown.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

import com.enth.uitmedown.model.User;

public class SharedPrefManager {
    //the constants
    private static final String SHARED_PREF_NAME = "uitmedownsharedpref";
    private static final String KEY_ID = "keyid";
    private static final String KEY_USERNAME = "keyusername";
    private static final String KEY_EMAIL = "keyemail";
    private static final String KEY_TOKEN = "keytoken";
    private static final String KEY_ROLE = "keyrole";

    private final Context mCtx;

    public SharedPrefManager(Context context) {
        mCtx = context;
    }

    /**
     * method to let the user login
     * this method will store the user data in shared preferences
     * @param user
     */
    public void storeUser(User user) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_TOKEN, user.getToken());
        editor.putString(KEY_ROLE, user.getRole());
        editor.apply();
    }

    /**
     * this method will checker whether user is already logged in or not.
     * return True if already logged in
     */

    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null) != null;
    }

    /**
     * this method will give the information of logged in user, retrieved from SharedPreferences
     */
    public User getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        User user = new User();
        user.setId(sharedPreferences.getInt(KEY_ID, -1));
        user.setUsername(sharedPreferences.getString(KEY_USERNAME, null));
        user.setEmail(sharedPreferences.getString(KEY_EMAIL, null));
        user.setToken(sharedPreferences.getString(KEY_TOKEN, null));
        user.setRole(sharedPreferences.getString(KEY_ROLE, null));

        return user;
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
