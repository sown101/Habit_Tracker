package com.example.habittracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {
    private static final String PREF_NAME = "habit_tracker_session";
    private static final String KEY_USER_NAME = "local_user_name";
    private static final String KEY_USER_EMAIL = "local_user_email";

    private static final int LOCAL_USER_ID = 1;
    private static final String DEFAULT_USER_NAME = "Local User";
    private static final String DEFAULT_USER_EMAIL = "";

    private SessionManager() {
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveLoginSession(Context context, int userId, String userName, String userEmail) {
        getPreferences(context)
                .edit()
                .putString(KEY_USER_NAME, userName == null ? DEFAULT_USER_NAME : userName)
                .putString(KEY_USER_EMAIL, userEmail == null ? DEFAULT_USER_EMAIL : userEmail)
                .apply();
    }

    public static boolean isLoggedIn(Context context) {
        return true;
    }

    public static int getUserId(Context context) {
        return LOCAL_USER_ID;
    }

    public static String getUserName(Context context) {
        return getPreferences(context).getString(KEY_USER_NAME, DEFAULT_USER_NAME);
    }

    public static String getUserEmail(Context context) {
        return getPreferences(context).getString(KEY_USER_EMAIL, DEFAULT_USER_EMAIL);
    }

    public static void logout(Context context) {
        clearSession(context);
    }

    public static void clearSession(Context context) {
        getPreferences(context).edit().clear().apply();
    }
}