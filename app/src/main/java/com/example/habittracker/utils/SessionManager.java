package com.example.habittracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {
    private static final String PREF_NAME = "habit_tracker_session";

    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private SessionManager() {
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveLoginSession(Context context, int userId, String userName, String userEmail) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, userName)
                .putString(KEY_USER_EMAIL, userEmail)
                .apply();
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static int getUserId(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public static String getUserName(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getString(KEY_USER_NAME, "");
    }

    public static String getUserEmail(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public static void logout(Context context) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().clear().apply();
    }
}
