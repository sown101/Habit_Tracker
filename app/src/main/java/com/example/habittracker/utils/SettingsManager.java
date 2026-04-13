package com.example.habittracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private static final String PREF_NAME = "habit_tracker_settings";

    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_SHAKE_ENABLED = "shake_enabled";
    private static final String KEY_DAILY_SUMMARY_HOUR = "daily_summary_hour";
    private static final String KEY_DAILY_SUMMARY_MINUTE = "daily_summary_minute";
    private static final String KEY_DISPLAY_NAME = "display_name";

    private SettingsManager() {
    }

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

    public static int getDailySummaryHour(Context context) {
        return getPrefs(context).getInt(KEY_DAILY_SUMMARY_HOUR, 21);
    }

    public static int getDailySummaryMinute(Context context) {
        return getPrefs(context).getInt(KEY_DAILY_SUMMARY_MINUTE, 0);
    }

    public static void setDailySummaryTime(Context context, int hour, int minute) {
        getPrefs(context)
                .edit()
                .putInt(KEY_DAILY_SUMMARY_HOUR, hour)
                .putInt(KEY_DAILY_SUMMARY_MINUTE, minute)
                .apply();
    }

    public static String getDisplayName(Context context) {
        return getPrefs(context).getString(KEY_DISPLAY_NAME, "");
    }

    public static void setDisplayName(Context context, String name) {
        getPrefs(context).edit().putString(KEY_DISPLAY_NAME, name).apply();
    }
}