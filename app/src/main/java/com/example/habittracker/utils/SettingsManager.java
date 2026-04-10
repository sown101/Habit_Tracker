package com.example.habittracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private static final String PREF_NAME = "habit_tracker_settings";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_SHAKE_ENABLED = "shake_enabled";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isNotificationEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    public static void setNotificationEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }

    public static boolean isShakeEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_SHAKE_ENABLED, true);
    }

    public static void setShakeEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_SHAKE_ENABLED, enabled).apply();
    }
}